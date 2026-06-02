package com.zhongway.sms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongway.sms.dao.entity.TenantAccount;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 租户账户 Mapper
 */
public interface TenantAccountMapper extends BaseMapper<TenantAccount> {

    /**
     * 根据租户 ID 查询账户
     */
    @Select("SELECT * FROM sms_tenant_account WHERE tenant_id = #{tenantId}")
    TenantAccount selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 根据租户 ID 查询账户 (加锁，用于扣费)
     * SELECT FOR UPDATE
     */
    @Select("SELECT * FROM sms_tenant_account WHERE tenant_id = #{tenantId} FOR UPDATE")
    TenantAccount selectByTenantIdForUpdate(@Param("tenantId") String tenantId);
}
