package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.Supplier;
import com.java.email.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    /**
     * 根据条件筛选供应商
     *
     * @param ownerUserId   所属用户ID
     * @param supplierLevel 供应商等级
     * @param supplierName  供应商名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param pageNumber          页码
     * @param pageSize          每页大小
     * @return 符合条件的供应商分页结果
     */
    @GetMapping("/search")
    public Result<Page<Supplier>> findSuppliersByCriteria(
            @RequestParam(required = false) String ownerUserId,
            @RequestParam(required = false) Integer supplierLevel,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("currentUserId") String currentUserId, // 从请求头中获取当前用户ID
            @RequestHeader("currentUserRole") int currentUserRole) { // 从请求头中获取当前用户角色

        // 调用服务层方法，传递当前用户ID和角色
        return supplierService.findSuppliersByCriteria(
                ownerUserId, supplierLevel, supplierName, status, tradeType,
                pageNumber-1, pageSize, currentUserId, currentUserRole);
    }
//    @GetMapping("/search")
//    public Result<Page<Supplier>> findSuppliersByCriteria(
//            @RequestParam(required = false) String ownerUserId,
//            @RequestParam(required = false) Integer supplierLevel,
//            @RequestParam(required = false) String supplierName,
//            @RequestParam(required = false) Integer status,
//            @RequestParam(required = false) Integer tradeType,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        return supplierService.findSuppliersByCriteria(
//                ownerUserId, supplierLevel, supplierName, status, tradeType, page, size);
//    }

    /**
     * 根据条件筛选供应商,并存入redis返回rediskey
     *
     * @param ownerUserId   所属用户ID
     * @param supplierLevel 供应商等级
     * @param supplierName  供应商名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param pageNumber          页码
     * @param size          每页大小
     * @return 符合条件的供应商分页结果
     */
    @GetMapping("/search-all")
    public Result<String> findSuppliersByCriteriaRedis(
            @RequestParam(required = false) String ownerUserId,
            @RequestParam(required = false) Integer supplierLevel,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("currentUserId") String currentUserId,
            @RequestHeader("currentUserRole") int currentUserRole) {

        return supplierService.findSuppliersByCriteriaRedis(
                ownerUserId, supplierLevel, supplierName, status, tradeType,
                pageNumber, size, currentUserId, currentUserRole);
    }
//    @GetMapping("/search-redis")
//    public Result<String> findSuppliersByCriteriaRedis(
//            @RequestParam(required = false) String ownerUserId,
//            @RequestParam(required = false) Integer supplierLevel,
//            @RequestParam(required = false) String supplierName,
//            @RequestParam(required = false) Integer status,
//            @RequestParam(required = false) Integer tradeType,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        return supplierService.findSuppliersByCriteriaRedis(
//                ownerUserId, supplierLevel, supplierName, status, tradeType, page, size);
//    }
}
