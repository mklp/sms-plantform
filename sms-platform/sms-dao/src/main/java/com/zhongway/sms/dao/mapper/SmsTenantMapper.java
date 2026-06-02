package com.zhongway.sms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongway.sms.dao.entity.SmsTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 租户 Mapper 接口
 */
@Mapper
public interface SmsTenantMapper extends BaseMapper<SmsTenant> {
    
    /**
     * 根据 API Key 查询租户
     * 
     * @param apiKey API Key
     * @return 租户信息
     */
    @Select("SELECT * FROM sms_tenant WHERE api_key = #{apiKey} AND deleted = 0 LIMIT 1")
    SmsTenant selectByApiKey(@Param("apiKey") String apiKey);
    
    /**
     * 根据租户编码查询租户
     * 
     * @param tenantCode 租户编码
     * @return 租户信息
     */
    @Select("SELECT * FROM sms_tenant WHERE tenant_code = #{tenantCode} AND deleted = 0 LIMIT 1")
    SmsTenant selectByTenantCode(@Param("tenantCode") String tenantCode);
    
}
