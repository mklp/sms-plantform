package com.zhongway.sms.service.billing;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 扣费结果 DTO
 */
@Data
public class DeductionResult {
    
    /**
     * 是否扣费成功
     */
    private boolean success;
    
    /**
     * 扣费金额 (元)
     */
    private BigDecimal amount;
    
    /**
     * 扣费类型：CASH-现金，GIFT-赠送，PACKAGE-套餐
     */
    private String deductionType;
    
    /**
     * 扣费后余额
     */
    private BigDecimal remainingBalance;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    public static DeductionResult success(BigDecimal amount, String type, BigDecimal remaining) {
        DeductionResult result = new DeductionResult();
        result.setSuccess(true);
        result.setAmount(amount);
        result.setDeductionType(type);
        result.setRemainingBalance(remaining);
        return result;
    }
    
    public static DeductionResult fail(String errorCode, String errorMsg) {
        DeductionResult result = new DeductionResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMsg(errorMsg);
        return result;
    }
}
