package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告Mapper
 * 提供公告分页查询、按类型/状态筛选等数据操作
 */
public interface NoticeMapper extends BaseMapper<Notice> {

    /**
     * 分页查询公告列表（含发布人姓名）
     * @param offset 偏移量
     * @param size   每页条数
     * @param type   公告类型，可选
     * @param status 公告状态，可选
     * @return 公告列表
     */
    List<Notice> selectNoticePage(@Param("offset") int offset,
                                  @Param("size") int size,
                                  @Param("type") Integer type,
                                  @Param("status") Integer status);

    /**
     * 统计公告总数
     * @param type   公告类型，可选
     * @param status 公告状态，可选
     * @return 总数
     */
    int countNotice(@Param("type") Integer type,
                    @Param("status") Integer status);
}
