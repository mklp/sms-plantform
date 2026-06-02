package com.example.sms.service.channel;

import com.example.sms.dao.entity.SmsChannel;
import com.example.sms.dao.mapper.SmsChannelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通道管理服务
 */
@Service
public class ChannelManagerService {
    private static final Logger logger = LoggerFactory.getLogger(ChannelManagerService.class);

    private final SmsChannelMapper channelMapper;
    
    /**
     * 通道权重轮询计数器
     */
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    public ChannelManagerService(SmsChannelMapper channelMapper) {
        this.channelMapper = channelMapper;
    }

    /**
     * 根据策略选择最优通道
     * 
     * @param tenantId 租户 ID
     * @param phoneNumber 手机号 (用于号段匹配)
     * @param operator 运营商 (可选)
     * @return 选中的通道
     */
    public SmsChannel selectChannel(Long tenantId, String phoneNumber, String operator) {
        // 获取所有可用通道
        List<SmsChannel> availableChannels = getAvailableChannels();
        
        if (availableChannels.isEmpty()) {
            logger.warn("没有可用的短信通道");
            return null;
        }

        // 如果只有一个通道，直接返回
        if (availableChannels.size() == 1) {
            return availableChannels.get(0);
        }

        // 按优先级和权重选择通道
        return selectByWeightRoundRobin(availableChannels);
    }

    /**
     * 获取所有可用通道
     */
    public List<SmsChannel> getAvailableChannels() {
        return channelMapper.selectAvailableChannels();
    }

    /**
     * 根据通道 ID 获取通道配置
     */
    public SmsChannel getChannelById(Long channelId) {
        return channelMapper.selectById(channelId);
    }

    /**
     * 更新通道状态
     */
    public void updateChannelStatus(Long channelId, String status) {
        SmsChannel channel = channelMapper.selectById(channelId);
        if (channel != null) {
            channel.setStatus(status);
            channelMapper.updateById(channel);
            logger.info("通道状态已更新，channelId={}, status={}", channelId, status);
        }
    }

    /**
     * 记录通道发送结果 (用于统计成功率)
     */
    public void recordChannelResult(Long channelId, boolean success) {
        // TODO: 将发送结果记录到 Redis，用于实时统计通道成功率
        // TODO: 当成功率低于阈值时自动熔断通道
        logger.debug("通道发送结果记录，channelId={}, success={}", channelId, success);
    }

    /**
     * 权重轮询选择通道
     */
    private SmsChannel selectByWeightRoundRobin(List<SmsChannel> channels) {
        // 计算总权重
        int totalWeight = channels.stream()
            .filter(ch -> ch.getWeight() != null && ch.getWeight() > 0)
            .mapToInt(SmsChannel::getWeight)
            .sum();
        
        if (totalWeight == 0) {
            // 如果都没有权重，随机选择一个
            return channels.get((int)(Math.random() * channels.size()));
        }

        // 获取当前轮询位置
        int position = Math.abs(roundRobinCounter.incrementAndGet() % totalWeight);
        
        // 根据权重区间选择通道
        int current = 0;
        for (SmsChannel channel : channels) {
            int weight = channel.getWeight() != null ? channel.getWeight() : 1;
            current += weight;
            if (position < current) {
                return channel;
            }
        }
        
        return channels.get(channels.size() - 1);
    }

    /**
     * 通道故障切换
     * 
     * @param failedChannelId 故障通道 ID
     * @return 备用通道
     */
    public SmsChannel failover(Long failedChannelId) {
        logger.warn("通道故障，准备切换，failedChannelId={}", failedChannelId);
        
        // 将故障通道标记为不可用
        updateChannelStatus(failedChannelId, "UNAVAILABLE");
        
        // 获取其他可用通道
        List<SmsChannel> backupChannels = getAvailableChannels();
        
        if (backupChannels.isEmpty()) {
            logger.error("没有可用的备用通道");
            return null;
        }
        
        // 选择优先级最高的备用通道
        SmsChannel backup = backupChannels.stream()
            .max((c1, c2) -> {
                int priorityCompare = Integer.compare(c1.getPriority(), c2.getPriority());
                if (priorityCompare != 0) return priorityCompare;
                return Integer.compare(c1.getWeight(), c2.getWeight());
            })
            .orElse(null);
        
        if (backup != null) {
            logger.info("切换到备用通道，channelId={}, channelName={}", 
                backup.getId(), backup.getChannelName());
        }
        
        return backup;
    }
}
