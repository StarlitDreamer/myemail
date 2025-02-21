package com.java.email.controller;

import com.java.email.Dto.RequestData;
import com.java.email.Dto.ResponseData;
import com.java.email.common.Response;
import com.java.email.common.Result;
import com.java.email.entity.Email;
import com.java.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emails")
//@RequestMapping("/api")
public class EmailController {
    @Autowired
    private EmailService emailService;

    /**
     * 根据emailTaskId更新emailStatus
     *
     * @param emailTaskId 邮件任务ID
     * @param emailStatus 新的邮件状态
     * @return 更新后的邮件实体
     */
    @PutMapping("/update-status")
    public Result<Email> updateEmailStatus(@RequestParam String emailTaskId,
                                           @RequestParam Integer emailStatus) {
        try {
            // 调用服务层方法更新状态
            Email updatedEmail = emailService.updateEmailStatus(emailTaskId, emailStatus);
            return Result.success(updatedEmail);  // 返回成功响应，包含更新后的邮件
        } catch (Exception e) {
            return Result.error("更新邮件状态失败: " + e.getMessage());  // 返回错误响应
        }
    }

    /**
     * 根据email_task_id更新emailStatus为4
     *
     * @param emailTaskId 邮件任务ID
     * @return 更新后的邮件实体
     */
    @PutMapping("/reset-status")
    public Result<Email> resetEmailStatus(@RequestParam String emailTaskId) {
        try {
            // 调用服务层方法根据emailTaskId更新状态为4
            Email updatedEmail = emailService.updateEmailStatus(emailTaskId, 4);
            return Result.success(updatedEmail);  // 返回成功响应，包含更新后的邮件
        } catch (Exception e) {
            return Result.error("更新邮件状态失败: " + e.getMessage());  // 返回错误响应
        }
    }
//    /**
//     * 根据emailTaskId更新emailStatus
//     *
//     * @param emailTaskId 邮件任务ID
//     * @param emailStatus 新的邮件状态
//     * @return 更新后的邮件实体
//     */
//    @PutMapping("/update-status/{emailTaskId}")
//    public Email updateEmailStatus(@PathVariable String emailTaskId,
//                                   @RequestParam Integer emailStatus) {
//        return emailService.updateEmailStatus(emailTaskId, emailStatus);
//    }

//    @PostMapping("/send-conditions")
//    public Response getSendConditions(@RequestBody RequestData requestData) {
//        // 构建响应数据
//        Response response = new Response();
//        response.setCode(200);
//        response.setMsg("成功");
//        response.setData(new ResponseData(
//                requestData.getEmailType(),
//                requestData.getArea(),
//                requestData.getCountry(),
//                requestData.getReceiverInfo()
//        ));
//
//        return response;
//    }
}