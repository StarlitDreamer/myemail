package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.Customer;
import com.java.email.entity.Supplier;
import com.java.email.service.RecipientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipients")
public class RecipientController {

    @Autowired
    private RecipientService recipientService;

    /**
     * 根据条件筛选客户或供应商
     *
     * @param type       类型（customer 或 supplier）
     * @param ownerUserId 所属用户ID
     * @param level      等级（客户等级或供应商等级）
     * @param name       名称（客户名称或供应商名称）
     * @param status     分配状态
     * @param tradeType  贸易类型
     * @param page       页码
     * @param size       每页大小
     * @return 符合条件的客户或供应商分页结果
     */
    @GetMapping("/search")
    public Result<Page<?>> findRecipientsByCriteria(
            @RequestParam(required = false, defaultValue = "customer") String type,
            @RequestParam(required = false) String ownerUserId,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return recipientService.findRecipientsByCriteria(
                type, ownerUserId, level, name, status, tradeType, page, size);
    }
//    /**
//     * 根据条件筛选客户
//     *
//     * @param ownerUserId   所属用户ID
//     * @param customerLevel 客户等级
//     * @param customerName  客户名称
//     * @param status        分配状态
//     * @param tradeType     贸易类型
//     * @param page          页码
//     * @param size          每页大小
//     * @return 符合条件的客户分页结果
//     */
//    @GetMapping("/customers/search")
//    public Result<Page<Customer>> findCustomersByCriteria(
//            @RequestParam(required = false) String ownerUserId,
//            @RequestParam(required = false) Integer customerLevel,
//            @RequestParam(required = false) String customerName,
//            @RequestParam(required = false) Integer status,
//            @RequestParam(required = false) Integer tradeType,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        return recipientService.findCustomersByCriteria(
//                ownerUserId, customerLevel, customerName, status, tradeType, page, size);
//    }
//
//    /**
//     * 根据条件筛选供应商
//     *
//     * @param ownerUserId   所属用户ID
//     * @param supplierLevel 供应商等级
//     * @param supplierName  供应商名称
//     * @param status        分配状态
//     * @param tradeType     贸易类型
//     * @param page          页码
//     * @param size          每页大小
//     * @return 符合条件的供应商分页结果
//     */
//    @GetMapping("/suppliers/search")
//    public Result<Page<Supplier>> findSuppliersByCriteria(
//            @RequestParam(required = false) String ownerUserId,
//            @RequestParam(required = false) Integer supplierLevel,
//            @RequestParam(required = false) String supplierName,
//            @RequestParam(required = false) Integer status,
//            @RequestParam(required = false) Integer tradeType,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        return recipientService.findSuppliersByCriteria(
//                ownerUserId, supplierLevel, supplierName, status, tradeType, page, size);
//    }
}
