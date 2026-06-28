package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 审批记录Mapper
 */
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    /**
     * 按业务单据查询审批记录（时间轴）
     * @param businessType 业务类型
     * @param businessId   业务单据ID
     * @return 审批记录列表（含审批人姓名），按时间正序
     */
    List<Map<String, Object>> selectTimeline(@Param("businessType") String businessType,
                                              @Param("businessId") Long businessId);

    /**
     * 查询用户的角色编码列表
     * @param userId 用户ID（sys_user.id）
     * @return 角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的可审批业务列表（按角色权限过滤）
     * 经理审批：LEAVE/OVERTIME/EXPENSE 状态为 PENDING 或 MANAGER_APPROVED（经理已批）
     * 财务审批：EXPENSE 状态为 MANAGER_APPROVED
     * @param roleCode 角色编码
     * @param status   审批状态
     * @return 审批列表
     */
    List<Map<String, Object>> selectApprovalList(@Param("roleCode") String roleCode,
                                                  @Param("status") String status);
}
