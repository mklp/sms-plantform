package com.zhongway.sms.service.billing;

import com.zhongway.sms.dao.entity.TenantAccount;
import com.zhongway.sms.dao.entity.TenantPricing;
import com.zhongway.sms.dao.entity.TenantPackage;
import com.zhongway.sms.dao.mapper.TenantAccountMapper;
import com.zhongway.sms.dao.mapper.TenantPricingMapper;
import com.zhongway.sms.dao.mapper.TenantPackageMapper;
import com.zhongway.sms.dao.mapper.AccountFlowMapper;
import com.zhongway.sms.dao.entity.AccountFlow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 计费服务 - 实现租户额度控制
 * 支持：现金余额、赠送余额、套餐包三种扣费方式
 * 扣费优先级：套餐包 > 赠送余额 > 现金余额 (可配置)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final TenantAccountMapper tenantAccountMapper;
    private final TenantPricingMapper tenantPricingMapper;
    private final TenantPackageMapper tenantPackageMapper;
    private final AccountFlowMapper accountFlowMapper;

    /**
     * 获取租户可用余额信息
     */
    public TenantAccount getTenantAccount(String tenantId) {
        return tenantAccountMapper.selectByTenantId(tenantId);
    }

    /**
     * 查询租户的计费策略
     * @param tenantId 租户 ID
     * @param channelCode 通道代码
     * @param signatureName 签名名称 (可选)
     * @return 最优计费策略
     */
    public TenantPricing getPricingStrategy(String tenantId, String channelCode, String signatureName) {
        // 优先匹配带签名的策略
        if (signatureName != null) {
            TenantPricing pricing = tenantPricingMapper.selectByTenantAndChannelAndSignature(
                tenantId, channelCode, signatureName);
            if (pricing != null && pricing.getIsActive()) {
                return pricing;
            }
        }
        
        //  fallback 到通用策略
        return tenantPricingMapper.selectByTenantAndChannel(tenantId, channelCode);
    }

    /**
     * 计算短信发送费用
     * @param tenantId 租户 ID
     * @param channelCode 通道代码
     * @param mobileCount 手机号数量
     * @param contentLength 内容长度 (用于判断是否超条)
     * @return 总费用 (元)
     */
    public BigDecimal calculateFee(String tenantId, String channelCode, int mobileCount, int contentLength) {
        TenantPricing pricing = getPricingStrategy(tenantId, channelCode, null);
        if (pricing == null) {
            throw new IllegalArgumentException("未找到计费策略");
        }

        // 判断是否长短信 (超过 70 字按多条计算，这里简化处理)
        int smsCount = calculateSmsCount(contentLength);
        
        return pricing.getPricePerSms().multiply(BigDecimal.valueOf(mobileCount * smsCount));
    }

    /**
     * 计算短信条数 (简单规则：70 字一条，超过按倍数)
     */
    private int calculateSmsCount(int contentLength) {
        if (contentLength <= 70) {
            return 1;
        }
        return (int) Math.ceil((double) contentLength / 67); // 长短信每条 67 字
    }

    /**
     * 执行扣费操作 (事务性)
     * 扣费优先级：PACKAGE -> GIFT -> CASH
     * @param tenantId 租户 ID
     * @param amount 扣费金额
     * @param businessType 业务类型
     * @param relatedId 关联业务 ID
     * @return 扣费结果
     */
    @Transactional(rollbackFor = Exception.class)
    public DeductionResult deductBalance(String tenantId, BigDecimal amount, 
                                         String businessType, String relatedId) {
        
        // 1. 获取账户并加锁 (SELECT FOR UPDATE)
        TenantAccount account = tenantAccountMapper.selectByTenantIdForUpdate(tenantId);
        if (account == null) {
            return DeductionResult.fail("ACCOUNT_NOT_FOUND", "租户账户不存在");
        }
        
        if (account.getStatus() != 1) {
            return DeductionResult.fail("ACCOUNT_DISABLED", "租户账户已停用");
        }

        // 2. 获取扣费优先级配置 (默认：PACKAGE,GIFT,CASH)
        List<String> priorityOrder = Arrays.asList("PACKAGE", "GIFT", "CASH");
        
        BigDecimal remainingAmount = amount;
        String lastDeductionType = null;
        
        try {
            // 3. 按优先级依次扣费
            for (String deductionType : priorityOrder) {
                if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                
                switch (deductionType) {
                    case "PACKAGE":
                        remainingAmount = deductFromPackage(tenantId, remainingAmount);
                        if (remainingAmount.compareTo(amount) < 0) {
                            lastDeductionType = "PACKAGE";
                        }
                        break;
                    case "GIFT":
                        if (account.getGiftBalance().compareTo(remainingAmount) >= 0) {
                            account.setGiftBalance(account.getGiftBalance().subtract(remainingAmount));
                            remainingAmount = BigDecimal.ZERO;
                            lastDeductionType = "GIFT";
                        } else {
                            remainingAmount = remainingAmount.subtract(account.getGiftBalance());
                            account.setGiftBalance(BigDecimal.ZERO);
                            lastDeductionType = "GIFT";
                        }
                        break;
                    case "CASH":
                        if (account.getCashBalance().compareTo(remainingAmount) >= 0) {
                            account.setCashBalance(account.getCashBalance().subtract(remainingAmount));
                            remainingAmount = BigDecimal.ZERO;
                            lastDeductionType = "CASH";
                        } else {
                            // 余额不足
                            return DeductionResult.fail("INSUFFICIENT_BALANCE", 
                                String.format("余额不足，需要:%.4f 元，可用现金:%.4f 元", 
                                    amount, account.getCashBalance()));
                        }
                        break;
                }
            }
            
            // 4. 更新账户
            if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                return DeductionResult.fail("INSUFFICIENT_BALANCE", "所有账户余额不足");
            }
            
            account.setTotalConsume(account.getTotalConsume().add(amount));
            account.setVersion(account.getVersion() + 1);
            tenantAccountMapper.updateById(account);
            
            // 5. 记录流水
            recordFlow(tenantId, businessType, amount.negate(), 
                      getCashOrGiftBalance(account, lastDeductionType), 
                      relatedId, "短信发送扣费");
            
            return DeductionResult.success(amount, lastDeductionType, 
                getCashOrGiftBalance(account, lastDeductionType));
                
        } catch (Exception e) {
            log.error("扣费失败：tenantId={}, amount={}", tenantId, amount, e);
            throw e;
        }
    }

    /**
     * 从套餐包扣费
     */
    private BigDecimal deductFromPackage(String tenantId, BigDecimal remainingAmount) {
        // 查询有效的套餐包 (按过期时间排序，优先用过期的)
        List<TenantPackage> packages = tenantPackageMapper.selectValidByTenantId(tenantId);
        
        for (TenantPackage pkg : packages) {
            if (pkg.getRemainingCount() <= 0) {
                continue;
            }
            
            // 套餐包按条数扣，需要换算
            // 这里简化：假设 1 条短信 = 1 单位套餐
            // 实际可能需要根据套餐定义的价格换算
            long deductCount = Math.min(pkg.getRemainingCount(), 1L); // 每次扣 1 条
            
            pkg.setRemainingCount(pkg.getRemainingCount() - deductCount);
            if (pkg.getRemainingCount() == 0) {
                pkg.setStatus((short) 0); // 标记为用完
            }
            tenantPackageMapper.updateById(pkg);
            
            // 套餐包扣费成功，返回剩余需要扣的金额 (套餐包抵扣了 1 条，金额为 0)
            // 这里简化处理，实际应该根据套餐单价计算
            return remainingAmount.subtract(BigDecimal.ZERO); 
        }
        
        return remainingAmount; // 没有可用套餐包
    }

    /**
     * 获取当前主要余额 (现金或赠送)
     */
    private BigDecimal getCashOrGiftBalance(TenantAccount account, String deductionType) {
        if ("GIFT".equals(deductionType)) {
            return account.getGiftBalance();
        }
        return account.getCashBalance();
    }

    /**
     * 记录资金流水
     */
    private void recordFlow(String tenantId, String businessType, BigDecimal amount,
                           BigDecimal balanceSnapshot, String relatedId, String remark) {
        AccountFlow flow = new AccountFlow();
        flow.setFlowNo(UUID.randomUUID().toString().replace("-", ""));
        flow.setTenantId(tenantId);
        flow.setBusinessType(businessType);
        flow.setAmount(amount);
        flow.setBalanceSnapshot(balanceSnapshot);
        flow.setRelatedId(relatedId);
        flow.setRemark(remark);
        flow.setCreatedAt(LocalDateTime.now());
        
        accountFlowMapper.insert(flow);
    }

    /**
     * 充值操作
     */
    @Transactional(rollbackFor = Exception.class)
    public void recharge(String tenantId, BigDecimal amount, boolean isGift) {
        TenantAccount account = tenantAccountMapper.selectByTenantIdForUpdate(tenantId);
        if (account == null) {
            throw new IllegalArgumentException("租户账户不存在");
        }
        
        if (isGift) {
            account.setGiftBalance(account.getGiftBalance().add(amount));
        } else {
            account.setCashBalance(account.getCashBalance().add(amount));
            account.setTotalRecharge(account.getTotalRecharge().add(amount));
        }
        
        account.setVersion(account.getVersion() + 1);
        tenantAccountMapper.updateById(account);
        
        recordFlow(tenantId, "RECHARGE", amount, 
            isGift ? account.getGiftBalance() : account.getCashBalance(), 
            null, isGift ? "赠送余额充值" : "现金充值");
    }

    /**
     * 检查余额是否充足 (不扣费，仅检查)
     */
    public boolean checkBalance(String tenantId, BigDecimal amount) {
        TenantAccount account = tenantAccountMapper.selectByTenantId(tenantId);
        if (account == null || account.getStatus() != 1) {
            return false;
        }
        
        // 检查套餐包
        List<TenantPackage> packages = tenantPackageMapper.selectValidByTenantId(tenantId);
        long totalPackageCount = packages.stream()
            .mapToLong(TenantPackage::getRemainingCount)
            .sum();
        
        if (totalPackageCount > 0) {
            return true; // 有套餐包
        }
        
        // 检查赠送余额 + 现金余额
        BigDecimal totalBalance = account.getGiftBalance().add(account.getCashBalance());
        return totalBalance.compareTo(amount) >= 0;
    }
}
