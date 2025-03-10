package com.java.email.controller;


import com.java.email.common.Result;
import com.java.email.model.entity.Customer;
import com.java.email.model.entity.EmailReport;
import com.java.email.model.entity.Supplier;
import com.java.email.repository.CustomerRepository;
import com.java.email.repository.SupplierRepository;
import com.java.email.service.EmailReportService;
import com.java.email.service.EmailTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/email-report")
public class EmailReportController {

    @Autowired
    private EmailReportService emailReportService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailTaskService emailTaskService;

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * 用户点击退订链接，根据email_task_id增加退订数量。
     *
     * @param emailTaskId 邮件任务ID
     * @return 返回更新结果
     */
    @GetMapping("/unsubscribe")
    public String updateUnsubscribeAmount(@RequestParam String emailTaskId, @RequestParam String receiverEmail) {
        String emailTypeId = emailTaskService.getEmailTypeId(emailTaskId);

        Customer customer = customerRepository.findByEmails(receiverEmail);

        if (customer != null) {
            List<String> customerNoAcceptEmailTypeId = customer.getNoAcceptEmailTypeId();
            // 将emailTypeId添加到noAcceptEmailTypeId
            if (customerNoAcceptEmailTypeId == null) {
                customer.setNoAcceptEmailTypeId(new ArrayList<>()); // 如果noAcceptEmailTypeId为空，初始化为空列表
            }

            // 只有当 emailTypeId 不在 customerNoAcceptEmailTypeId 里时，才执行添加和更新
            if (!customerNoAcceptEmailTypeId.contains(emailTypeId)) {
                customerNoAcceptEmailTypeId.add(emailTypeId);
                customer.setNoAcceptEmailTypeId(customerNoAcceptEmailTypeId);
                emailReportService.updateUnsubscribeAmount(emailTaskId);
            }

            customerRepository.save(customer);  // 保存更新后的Customer实体
        }

        Supplier supplier = supplierRepository.findByEmails(receiverEmail);

        if (supplier != null) {
            List<String> supplierNoAcceptEmailTypeId = supplier.getNoAcceptEmailTypeId();
            // 将emailTypeId添加到noAcceptEmailTypeId
            if (supplierNoAcceptEmailTypeId == null) {
                supplier.setNoAcceptEmailTypeId(new ArrayList<>()); // 如果noAcceptEmailTypeId为空，初始化为空列表
            }

            if (!supplierNoAcceptEmailTypeId.contains(emailTypeId)){
                supplierNoAcceptEmailTypeId.add(emailTypeId);
                supplier.setNoAcceptEmailTypeId(supplierNoAcceptEmailTypeId);
                emailReportService.updateUnsubscribeAmount(emailTaskId);
            }

            supplierRepository.save(supplier);  // 保存更新后的supplier实体
        }
        return "退订成功";
    }

    /**
     * 根据email_task_id更新打开数量
     *
     * @return 更新后的EmailReport实体
     */
    @GetMapping("/open-email")
    public Result updateOpenAmount(@RequestParam String emailTaskId, @RequestParam String receiverEmail) {
        try {
            EmailReport updatedEmailReport = emailReportService.updateOpenAmount(emailTaskId, receiverEmail);
            return Result.success(updatedEmailReport);
        } catch (Exception e) {
            return Result.error("更新打开数量失败: " + e.getMessage());
        }
    }
}