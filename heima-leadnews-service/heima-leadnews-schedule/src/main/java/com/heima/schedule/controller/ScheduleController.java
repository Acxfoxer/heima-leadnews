package com.heima.schedule.controller;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.schedule.service.TaskInfoService;
import org.springframework.web.bind.annotation.*;

/**
 * @author lee
 */
@RestController
@RequestMapping("/api/v1")
public class ScheduleController {
    private final TaskInfoService service;
    public ScheduleController(TaskInfoService service) {
        this.service = service;
    }

    /**
     * 添加任务
     * @param taskinfo   任务对象
     * @return       任务id
     */
    @PostMapping("/schedule/addTask")
    public ResponseResult addTask(@RequestBody Taskinfo taskinfo){
        return service.addTask(taskinfo);
    }

    /**
     * 更新延迟发布任务
     * @param taskinfo
     * @return
     */
    @PostMapping("/schedule/updateTask")
    public ResponseResult updateTask(@RequestBody Taskinfo taskinfo) {
        service.updateById(taskinfo);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 选择延迟发布任务
     * @param taskId
     * @return
     */
    @GetMapping("/schedule/selectTaskById")
    public Taskinfo selectTaskById(@RequestParam("taskId") Long taskId) {
        return service.getById(taskId);
    }
}
