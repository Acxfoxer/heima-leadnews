package com.heima.feign.fallback;

import com.heima.feign.article.TaskInfoClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.schedule.pojos.Taskinfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author 18727
 */
@Slf4j
@Component
public class TaskInfoClientFallBack implements FallbackFactory<TaskInfoClient> {

    @Override
    public TaskInfoClient create(Throwable cause) {
        return new TaskInfoClient() {
            @Override
            public ResponseResult addTask(Taskinfo taskinfo) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,cause.getMessage());
            }

            @Override
            public ResponseResult updateTask(Taskinfo taskinfo) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,cause.getMessage());
            }

            @Override
            public Taskinfo selectTaskById(Long taskId) {
                return null;
            }
        };
    }
}
