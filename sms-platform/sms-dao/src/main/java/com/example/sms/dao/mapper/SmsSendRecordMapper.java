package com.example.sms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sms.dao.entity.SmsChannel;
import com.example.sms.dao.entity.SmsSendRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 短信发送记录 Mapper 接口
 */
@Mapper
public interface SmsSendRecordMapper extends BaseMapper<SmsSendRecord> {
    
}
