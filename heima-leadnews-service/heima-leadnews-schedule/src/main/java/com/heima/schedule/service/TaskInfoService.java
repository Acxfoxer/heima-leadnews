package com.heima.schedule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.schedule.pojos.Taskinfo;

/**
 * @author 18727
 */
public interface TaskInfoService extends IService<Taskinfo> {
    /**
     * 添加任务进数据库,持久化
     * @param taskinfo
     * @return
     */
    ResponseResult addTask(Taskinfo taskinfo);

    /**
     * 定时查询数据库,发送延迟消息
     */
    void refresh();

}
