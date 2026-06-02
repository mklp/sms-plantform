package com.sms.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sms.platform.entity.SmsSendRecordHot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信发送记录热表 Mapper
 */
@Mapper
public interface SmsSendRecordHotMapper extends BaseMapper<SmsSendRecordHot> {
    // 通用 CRUD 操作由 BaseMapper 提供
}
