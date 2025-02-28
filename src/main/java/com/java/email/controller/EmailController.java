package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.entity.Email;
import com.java.email.model.request.ResetTaskStatusRequest;
import com.java.email.model.request.UpdateTaskStatusRequest;
import com.java.email.model.response.ResetTaskStatusResponse;
import com.java.email.model.response.UpdateTaskStatusResponse;
import com.java.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emails")
public class EmailController {
    @Autowired
    private EmailService emailService;

    /**
     * 根据emailTaskId更新emailStatus（改变任务状态）
     *
     * @return 更新后的邮件实体
     */
    @PutMapping("/update-status")
    public Result<UpdateTaskStatusResponse> updateEmailStatus(@RequestBody UpdateTaskStatusRequest request) {
        try {
            // 调用服务层方法更新状态
            Email updatedEmail = emailService.updateEmailStatus(request);
            UpdateTaskStatusResponse response = new UpdateTaskStatusResponse();
            response.setEmailTaskId(updatedEmail.getEmailTaskId());
            response.setEmailStatus(String.valueOf(updatedEmail.getEmailStatus()));
            return Result.success(response);  // 返回成功响应，包含更新后的邮件
        } catch (Exception e) {
            return Result.error("更新邮件状态失败: " + e.getMessage());  // 返回错误响应
        }
    }

    /**
     * 根据email_task_id更新emailStatus为4（重置任务）
     *
     * @return 更新后的邮件实体
     */
    @PutMapping("/reset-status")
    public Result<ResetTaskStatusResponse> resetEmailStatus(@RequestBody ResetTaskStatusRequest request) {
        try {
            // 调用服务层方法根据emailTaskId更新状态为4
            Email updatedEmail = emailService.resetEmailStatus(request);
            ResetTaskStatusResponse response = new ResetTaskStatusResponse();
            response.setEmailTaskId(updatedEmail.getEmailTaskId());
            response.setEmailStatus(String.valueOf(updatedEmail.getEmailStatus()));
            return Result.success(response);  // 返回成功响应，包含更新后的邮件
        } catch (Exception e) {
            return Result.error("更新邮件状态失败: " + e.getMessage());  // 返回错误响应
        }
    }
}