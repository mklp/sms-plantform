package com.zhongway.sms.web.controller.billing;

import com.zhongway.sms.service.billing.BillingService;
import com.zhongway.sms.dao.entity.TenantAccount;
import com.zhongway.sms.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 计费管理 Controller
 */
@Tag(name = "计费管理", description = "租户账户、充值、扣费相关接口")
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    /**
     * 查询租户账户余额
     */
    @Operation(summary = "查询账户余额", description = "获取租户的现金余额、赠送余额、套餐包余量等")
    @GetMapping("/account/{tenantId}")
    public Result<TenantAccount> getAccount(@PathVariable String tenantId) {
        TenantAccount account = billingService.getTenantAccount(tenantId);
        if (account == null) {
            return Result.fail("租户账户不存在");
        }
        return Result.success(account);
    }

    /**
     * 充值接口
     */
    @Operation(summary = "账户充值", description = "为租户充值现金或赠送余额")
    @PostMapping("/recharge")
    public Result<Void> recharge(@RequestParam String tenantId,
                                 @RequestParam BigDecimal amount,
                                 @RequestParam(defaultValue = "false") Boolean isGift) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail("充值金额必须大于 0");
        }
        billingService.recharge(tenantId, amount, isGift);
        return Result.success();
    }

    /**
     * 计算短信费用
     */
    @Operation(summary = "试算短信费用", description = "根据手机号数量和内容长度预估发送费用")
    @GetMapping("/calculate-fee")
    public Result<BigDecimal> calculateFee(@RequestParam String tenantId,
                                           @RequestParam String channelCode,
                                           @RequestParam Integer mobileCount,
                                           @RequestParam Integer contentLength) {
        BigDecimal fee = billingService.calculateFee(tenantId, channelCode, mobileCount, contentLength);
        return Result.success(fee);
    }

    /**
     * 检查余额是否充足
     */
    @Operation(summary = "检查余额", description = "检查租户余额是否足够支付指定金额")
    @GetMapping("/check-balance")
    public Result<Boolean> checkBalance(@RequestParam String tenantId,
                                        @RequestParam BigDecimal amount) {
        boolean sufficient = billingService.checkBalance(tenantId, amount);
        return Result.success(sufficient);
    }
}
