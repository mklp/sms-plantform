package com.zhongway.sms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongway.sms.dao.entity.TenantPackage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 租户套餐包 Mapper
 */
public interface TenantPackageMapper extends BaseMapper<TenantPackage> {

    /**
     * 查询租户有效的套餐包 (按过期时间排序，优先使用即将过期的)
     */
    @Select("SELECT * FROM sms_tenant_package " +
            "WHERE tenant_id = #{tenantId} AND status = 1 AND expire_time > NOW() " +
            "AND remaining_count > 0 " +
            "ORDER BY expire_time ASC")
    List<TenantPackage> selectValidByTenantId(@Param("tenantId") String tenantId);
}
