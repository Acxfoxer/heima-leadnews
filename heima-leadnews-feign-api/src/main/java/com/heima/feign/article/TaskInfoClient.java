package com.heima.feign.article;

import com.heima.feign.config.FeignClientsConfigurationCustom;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.schedule.pojos.Taskinfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 18727
 */
@FeignClient(name = "schedule-service",configuration = FeignClientsConfigurationCustom.class)
public interface TaskInfoClient {
    /**
     * 添加延迟发布任务
     * @param taskinfo
     * @return
     */
    @PostMapping("/api/v1/schedule/addTask")
    public ResponseResult addTask(@RequestBody Taskinfo taskinfo);

    /**
     * 更新延迟发布任务
     * @param taskinfo
     * @return
     */
    @PostMapping("/api/v1/schedule/updateTask")
    public ResponseResult updateTask(@RequestBody Taskinfo taskinfo);

    /**
     * 选择延迟发布任务
     * @param taskId
     * @return
     */
    @GetMapping("/api/v1/schedule/selectTaskById")
    public Taskinfo selectTaskById(@RequestParam("taskId") Long taskId);
}
