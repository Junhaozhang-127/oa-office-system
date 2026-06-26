package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.service.EmpEmployeeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 员工档案Controller
 * 提供员工列表查询和详细信息接口
 */
@RestController
@RequestMapping("/api/employee")
public class EmpEmployeeController {

    private final EmpEmployeeService empEmployeeService;

    public EmpEmployeeController(EmpEmployeeService empEmployeeService) {
        this.empEmployeeService = empEmployeeService;
    }

    /**
     * 获取全部员工列表
     * @return 包含total和rows的员工数据
     */
    @GetMapping("/list")
    public R<Map<String, Object>> getList() {
        return R.success(empEmployeeService.getEmployeeList());
    }

    /**
     * 获取员工详细信息
     * @param empId 员工ID
     * @return 员工基本信息+本月考勤统计+近期打卡记录，不存在时返回错误
     */
    @GetMapping("/detail")
    public R<Map<String, Object>> getDetail(@RequestParam Long empId) {
        Map<String, Object> data = empEmployeeService.getEmployeeDetail(empId);
        if (data == null) {
            return R.fail("员工不存在");
        }
        return R.success(data);
    }
}
