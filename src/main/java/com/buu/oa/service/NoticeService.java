package com.buu.oa.service;

import com.buu.oa.entity.Notice;

import java.util.Map;

/**
 * 公告Service
 * 提供公告创建、编辑、发布、撤回、查询、已读管理
 */
public interface NoticeService {

    /**
     * 创建公告（草稿状态）
     * @param params {title, content, type, publisherId}
     * @return 创建结果
     */
    Map<String, Object> createNotice(Map<String, Object> params);

    /**
     * 编辑公告
     * @param id     公告ID
     * @param params {title, content, type}
     * @return 更新结果
     */
    Map<String, Object> updateNotice(Long id, Map<String, Object> params);

    /**
     * 发布公告，并生成通知给所有用户
     * @param id 公告ID
     * @return 发布结果
     */
    Map<String, Object> publishNotice(Long id);

    /**
     * 撤回公告
     * @param id 公告ID
     * @return 撤回结果
     */
    Map<String, Object> withdrawNotice(Long id);

    /**
     * 分页查询公告
     * @param page   页码
     * @param size   每页条数
     * @param type   类型，可选
     * @param status 状态，可选
     * @return {rows, total}
     */
    Map<String, Object> getNoticePage(int page, int size, Integer type, Integer status);

    /**
     * 查询公告详情
     * @param id 公告ID
     * @return 公告实体
     */
    Notice getNoticeDetail(Long id);

    /**
     * 标记公告已读
     * @param noticeId 公告ID
     * @param userId   用户ID
     */
    void markRead(Long noticeId, Long userId);

    /**
     * 查询用户公告未读数量
     * @param userId 用户ID
     * @return 未读数
     */
    int getUnreadCount(Long userId);

    /**
     * 查询用户对指定公告列表的已读状态
     * @param userId    用户ID
     * @param noticeIds 公告ID列表
     * @return 已读的公告ID集合
     */
    java.util.Set<Long> getReadNoticeIds(Long userId, java.util.List<Long> noticeIds);
}
