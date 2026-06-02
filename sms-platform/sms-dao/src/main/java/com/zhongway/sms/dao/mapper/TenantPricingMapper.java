package com.zhongway.sms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongway.sms.dao.entity.TenantPricing;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 租户计费策略 Mapper
 */
public interface TenantPricingMapper extends BaseMapper<TenantPricing> {

    /**
     * 根据租户和通道查询计费策略 (通用)
     */
    @Select("SELECT * FROM sms_tenant_pricing " +
            "WHERE tenant_id = #{tenantId} AND channel_code = #{channelCode} " +
            "AND is_active = true ORDER BY priority ASC LIMIT 1")
    TenantPricing selectByTenantAndChannel(@Param("tenantId") String tenantId, 
                                           @Param("channelCode") String channelCode);

    /**
     * 根据租户、通道和签名查询计费策略 (精确匹配)
     */
    @Select("SELECT * FROM sms_tenant_pricing " +
            "WHERE tenant_id = #{tenantId} AND channel_code = #{channelCode} " +
            "AND signature_name = #{signatureName} AND is_active = true " +
            "ORDER BY priority ASC LIMIT 1")
    TenantPricing selectByTenantAndChannelAndSignature(@Param("tenantId") String tenantId,
                                                       @Param("channelCode") String channelCode,
                                                       @Param("signatureName") String signatureName);
}
