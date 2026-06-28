package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.Notification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息通知Mapper
 */
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询用户通知列表
     * @param receiverId  接收人ID
     * @param businessType 业务类型，可选
     * @param status      消息状态，可选
     * @param offset      偏移量
     * @param size        每页条数
     * @return 通知列表
     */
    List<Notification> selectMyNotifications(@Param("receiverId") Long receiverId,
                                             @Param("businessType") String businessType,
                                             @Param("status") String status,
                                             @Param("offset") int offset,
                                             @Param("size") int size);

    /**
     * 统计用户通知总数
     */
    int countMyNotifications(@Param("receiverId") Long receiverId,
                             @Param("businessType") String businessType,
                             @Param("status") String status);

    /**
     * 查询用户未读消息数量
     * @param receiverId 接收人ID
     * @return 未读数量
     */
    int countUnread(@Param("receiverId") Long receiverId);

    /**
     * 按业务类型统计未读数
     * @param receiverId 接收人ID
     * @return [{businessType, count}]
     */
    List<java.util.Map<String, Object>> countUnreadByBusinessType(@Param("receiverId") Long receiverId);

    /**
     * 批量标记通知为已读
     * @param ids 通知ID列表
     * @return 影响行数
     */
    int batchMarkRead(@Param("ids") List<Long> ids);

    /**
     * 全部标记已读
     * @param receiverId 接收人ID
     * @return 影响行数
     */
    int markAllRead(@Param("receiverId") Long receiverId);

    /**
     * 检查同业务是否已有通知（防重复）
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @param receiverId   接收人ID
     * @return 已存在的通知数量
     */
    int countByBusiness(@Param("businessType") String businessType,
                        @Param("businessId") Long businessId,
                        @Param("receiverId") Long receiverId);
}
