package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.NoticeRead;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告已读记录Mapper
 */
public interface NoticeReadMapper extends BaseMapper<NoticeRead> {

    /**
     * 查询用户对指定公告的已读记录
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 已读记录，未读返回null
     */
    NoticeRead selectByNoticeAndUser(@Param("noticeId") Long noticeId,
                                     @Param("userId") Long userId);

    /**
     * 查询用户已读公告ID列表
     * @param userId   用户ID
     * @param noticeIds 公告ID列表
     * @return 已读的公告ID列表
     */
    List<Long> selectReadNoticeIds(@Param("userId") Long userId,
                                   @Param("noticeIds") List<Long> noticeIds);

    /**
     * 统计用户未读公告数量
     * @param userId 用户ID
     * @return 未读公告数量
     */
    int countUnreadByUser(@Param("userId") Long userId);
}
