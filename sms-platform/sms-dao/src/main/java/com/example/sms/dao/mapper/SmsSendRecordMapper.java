package com.example.sms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sms.dao.entity.SmsSendRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 短信发送记录 Mapper 接口
 */
@Mapper
public interface SmsSendRecordMapper extends BaseMapper<SmsSendRecord> {

    /**
     * 根据消息 ID 查询发送记录
     *
     * @param msgId 消息 ID
     * @return 发送记录
     */
    @Select("SELECT * FROM sms_send_record WHERE msg_id = #{msgId} AND deleted = 0 LIMIT 1")
    SmsSendRecord selectByMsgId(@Param("msgId") String msgId);

    /**
     * 根据租户 ID 和手机号查询最近的发送记录
     *
     * @param tenantId 租户 ID
     * @param phoneNumber 手机号
     * @param limit 限制条数
     * @return 发送记录列表
     */
    @Select("SELECT * FROM sms_send_record " +
            "WHERE tenant_id = #{tenantId} AND phone_number = #{phoneNumber} AND deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    java.util.List<SmsSendRecord> selectRecentByPhone(@Param("tenantId") Long tenantId, 
                                                       @Param("phoneNumber") String phoneNumber,
                                                       @Param("limit") int limit);

    /**
     * 统计租户当日发送数量
     *
     * @param tenantId 租户 ID
     * @param date 日期 (yyyy-MM-dd)
     * @return 发送数量
     */
    @Select("SELECT COUNT(*) FROM sms_send_record " +
            "WHERE tenant_id = #{tenantId} AND DATE(create_time) = #{date} AND deleted = 0")
    int countTodaySent(@Param("tenantId") Long tenantId, @Param("date") String date);

    /**
     * 批量插入发送记录 (用于高性能场景)
     *
     * @param records 发送记录列表
     * @return 影响行数
     */
    int batchInsert(@Param("records") java.util.List<SmsSendRecord> records);
}
