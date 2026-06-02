package com.zhongway.sms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongway.sms.dao.entity.SmsChannel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 短信通道 Mapper 接口
 */
@Mapper
public interface SmsChannelMapper extends BaseMapper<SmsChannel> {

    /**
     * 根据通道编码查询通道
     * 
     * @param channelCode 通道编码
     * @return 通道信息
     */
    @Select("SELECT * FROM sms_channel WHERE channel_code = #{channelCode} AND deleted = 0 LIMIT 1")
    SmsChannel selectByCode(@Param("channelCode") String channelCode);

    /**
     * 选择一个可用的通道
     * 按优先级升序、权重降序排序，选择第一个状态为 ACTIVE 且连续失败次数小于阈值的通道
     * 
     * @return 可用通道
     */
    @Select("SELECT * FROM sms_channel " +
            "WHERE status = 'ACTIVE' " +
            "  AND deleted = 0 " +
            "  AND (consecutive_failures IS NULL OR consecutive_failures < 5) " +
            "ORDER BY priority ASC, weight DESC " +
            "LIMIT 1")
    SmsChannel selectAvailableChannel();

    /**
     * 根据协议类型和运营商选择可用通道
     * 
     * @param protocolType 协议类型
     * @param operator 运营商
     * @return 可用通道
     */
    @Select("SELECT * FROM sms_channel " +
            "WHERE status = 'ACTIVE' " +
            "  AND deleted = 0 " +
            "  AND protocol_type = #{protocolType} " +
            "  AND (operator = #{operator} OR operator = 'ALL') " +
            "  AND (consecutive_failures IS NULL OR consecutive_failures < 5) " +
            "ORDER BY priority ASC, weight DESC " +
            "LIMIT 1")
    SmsChannel selectByProtocolAndOperator(@Param("protocolType") String protocolType,
                                           @Param("operator") String operator);

    /**
     * 更新通道连续失败次数
     * 
     * @param channelId 通道 ID
     * @param failures 失败次数
     */
    @Update("UPDATE sms_channel SET consecutive_failures = #{failures}, last_failure_time = NOW() WHERE id = #{channelId}")
    void updateFailureCount(@Param("channelId") Long channelId, @Param("failures") int failures);

    /**
     * 重置通道失败计数 (发送成功后调用)
     * 
     * @param channelId 通道 ID
     */
    @Update("UPDATE sms_channel SET consecutive_failures = 0, last_heartbeat_time = NOW() WHERE id = #{channelId}")
    void resetFailureCount(@Param("channelId") Long channelId);

    /**
     * 更新通道今日发送计数
     * 
     * @param channelId 通道 ID
     * @param count 增加的数量
     */
    @Update("UPDATE sms_channel SET today_sent_count = today_sent_count + #{count}, update_time = NOW() WHERE id = #{channelId}")
    void incrementTodayCount(@Param("channelId") Long channelId, @Param("count") int count);

    /**
     * 查询所有可用通道列表
     *
     * @return 可用通道列表
     */
    @Select("SELECT * FROM sms_channel " +
            "WHERE status = 'ACTIVE' " +
            "  AND deleted = 0 " +
            "  AND (consecutive_failures IS NULL OR consecutive_failures < 5) " +
            "ORDER BY priority ASC, weight DESC")
    java.util.List<SmsChannel> selectAvailableChannels();
}
