//package com.java.email.service;
//
//import com.java.email.common.Result;
//import com.java.email.entity.Customer;
//import com.java.email.entity.Supplier;
//import com.java.email.repository.CustomerRepository;
//import com.java.email.repository.SupplierRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//public class RecipientService {
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    @Autowired
//    private SupplierRepository supplierRepository;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    /**
//     * 根据条件筛选客户或供应商
//     *
//     * @param type        类型（customer 或 supplier）
//     * @param ownerUserId 所属用户ID
//     * @param level       等级（客户等级或供应商等级）
//     * @param name        名称（客户名称或供应商名称）
//     * @param status      分配状态
//     * @param tradeType   贸易类型
//     * @param page        页码
//     * @param size        每页大小
//     * @return 符合条件的客户或供应商分页结果
//     */
//    public Result<Page<?>> findRecipientsByCriteria(
//            String currentUserId, // 当前用户的ID
//            int currentUserRole,  // 当前用户的角色
//            String type,
//            String ownerUserId,
//            Integer level,
//            String name,
//            Integer status,
//            Integer tradeType,
//            int page,
//            int size) {
//
//        try {
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 根据用户角色决定查询条件
//            if (currentUserRole == 1) { // 公司角色，可以查看所有客户或供应商
//                return searchRecipients(type, null, level, name, status, tradeType, pageable);
//            } else if (currentUserRole == 2) { // 大管理角色，可以查看所有客户或供应商
//                return searchRecipients(type, null, level, name, status, tradeType, pageable);
//            } else if (currentUserRole == 3) { // 小管理角色，可以查看公司、自己、下属用户的客户或供应商
//                List<String> allowedUserIds = new ArrayList<>();
//                allowedUserIds.add("1"); // 公司用户ID
//                allowedUserIds.add(currentUserId); // 自己
//                // 假设有一个方法可以获取当前用户的下属用户ID列表
//                allowedUserIds.addAll(getSubordinateUserIds(currentUserId));
//                return searchRecipients(type, allowedUserIds, level, name, status, tradeType, pageable);
//            } else if (currentUserRole == 4) { // 普通用户，只能查看自己的客户或供应商
//                return searchRecipients(type, Collections.singletonList(currentUserId), level, name, status, tradeType, pageable);
//            } else {
//                // 如果没有匹配的角色，返回空结果
//                return Result.success(new PageImpl<>(Collections.emptyList()));
//            }
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询失败: " + e.getMessage());
//        }
//    }
//
//    // 根据类型和条件查询客户或供应商
//    private Result<Page<?>> searchRecipients(
//            String type,
//            List<String> allowedUserIds,
//            Integer level,
//            String name,
//            Integer status,
//            Integer tradeType,
//            Pageable pageable) {
//
//        if ("customer".equalsIgnoreCase(type)) {
//            // 查询客户
//            Page<Customer> customers;
//
//            if (allowedUserIds != null) {
//                customers = customerRepository.findByBelongUserIdIn(allowedUserIds, pageable);
//            } else if (level != null) {
//                customers = customerRepository.findByCustomerLevel(level, pageable);
//            } else if (name != null) {
//                customers = customerRepository.findByCustomerName(name, pageable);
//            } else if (status != null) {
//                customers = customerRepository.findByStatus(status, pageable);
//            } else if (tradeType != null) {
//                customers = customerRepository.findByTradeType(tradeType, pageable);
//            } else {
//                customers = customerRepository.findAll(pageable);
//            }
//
//            return Result.success(customers);
//        } else if ("supplier".equalsIgnoreCase(type)) {
//            // 查询供应商
//            Page<Supplier> suppliers;
//
//            if (allowedUserIds != null) {
//                suppliers = supplierRepository.findByBelongUseridIn(allowedUserIds, pageable);
//            } else if (level != null) {
//                suppliers = supplierRepository.findBySupplierLevel(level, pageable);
//            } else if (name != null) {
//                suppliers = supplierRepository.findBySupplierName(name, pageable);
//            } else if (status != null) {
//                suppliers = supplierRepository.findByStatus(status, pageable);
//            } else if (tradeType != null) {
//                suppliers = supplierRepository.findByTradeType(tradeType, pageable);
//            } else {
//                suppliers = supplierRepository.findAll(pageable);
//            }
//
//            return Result.success(suppliers);
//        } else {
//            return Result.error("类型参数无效，请指定 'customer' 或 'supplier'");
//        }
//    }
//
//    // 假设有一个方法可以获取当前用户的下属用户ID列表
//    private List<String> getSubordinateUserIds(String userId) {
//        // 这里实现获取下属用户ID的逻辑
//        // 例如：从数据库或ES中查询
//        return userService.getSubordinateUserIds(userId);
//    }
//
////    public Result<Page<?>> findRecipientsByCriteria(String type, String ownerUserId, Integer level,
////                                                    String name, Integer status, Integer tradeType,
////                                                    int page, int size) {
////        try {
////            Pageable pageable = PageRequest.of(page, size);
////
////            if ("customer".equalsIgnoreCase(type)) {
////                // 查询客户
////                Page<Customer> customers;
////
////                if (ownerUserId != null) {
////                    customers = customerRepository.findByBelongUserId(ownerUserId, pageable);
////                } else if (level != null) {
////                    customers = customerRepository.findByCustomerLevel(level, pageable);
////                } else if (name != null) {
////                    customers = customerRepository.findByCustomerName(name, pageable);
////                } else if (status != null) {
////                    customers = customerRepository.findByStatus(status, pageable);
////                } else if (tradeType != null) {
////                    customers = customerRepository.findByTradeType(tradeType, pageable);
////                } else {
////                    customers = customerRepository.findAll(pageable);
////                }
////
////                return Result.success(customers);
////            } else if ("supplier".equalsIgnoreCase(type)) {
////                // 查询供应商
////                Page<Supplier> suppliers;
////
////                if (ownerUserId != null) {
////                    suppliers = supplierRepository.findByBelongUserid(ownerUserId, pageable);
////                } else if (level != null) {
////                    suppliers = supplierRepository.findBySupplierLevel(level, pageable);
////                } else if (name != null) {
////                    suppliers = supplierRepository.findBySupplierName(name, pageable);
////                } else if (status != null) {
////                    suppliers = supplierRepository.findByStatus(status, pageable);
////                } else if (tradeType != null) {
////                    suppliers = supplierRepository.findByTradeType(tradeType, pageable);
////                } else {
////                    suppliers = supplierRepository.findAll(pageable);
////                }
////
////                return Result.success(suppliers);
////            } else {
////                return Result.error("类型参数无效，请指定 'customer' 或 'supplier'");
////            }
////        } catch (Exception e) {
////            return Result.error("查询失败: " + e.getMessage());
////        }
////    }
//
//    /**
//     * 根据条件筛选客户或供应商，并将结果存储到 Redis 中
//     *
//     * @param type        类型（customer 或 supplier）
//     * @param ownerUserId 所属用户ID
//     * @param level       等级（客户等级或供应商等级）
//     * @param name        名称（客户名称或供应商名称）
//     * @param status      分配状态
//     * @param tradeType   贸易类型
//     * @param page        页码
//     * @param size        每页大小
//     * @return Redis Key，用于获取存储的数据
//     */
//    public Result<String> findRecipientsAndStoreInRedis(String type, String ownerUserId, Integer level,
//                                                        String name, Integer status, Integer tradeType,
//                                                        int page, int size) {
//        try {
//            Pageable pageable = PageRequest.of(page, size);
//            Page<?> resultPage;
//
//            if ("customer".equalsIgnoreCase(type)) {
//                // 查询客户
//                if (ownerUserId != null) {
//                    resultPage = customerRepository.findByBelongUserId(ownerUserId, pageable);
//                } else if (level != null) {
//                    resultPage = customerRepository.findByCustomerLevel(level, pageable);
//                } else if (name != null) {
//                    resultPage = customerRepository.findByCustomerName(name, pageable);
//                } else if (status != null) {
//                    resultPage = customerRepository.findByStatus(status, pageable);
//                } else if (tradeType != null) {
//                    resultPage = customerRepository.findByTradeType(tradeType, pageable);
//                } else {
//                    resultPage = customerRepository.findAll(pageable);
//                }
//            } else if ("supplier".equalsIgnoreCase(type)) {
//                // 查询供应商
//                if (ownerUserId != null) {
//                    resultPage = supplierRepository.findByBelongUserid(ownerUserId, pageable);
//                } else if (level != null) {
//                    resultPage = supplierRepository.findBySupplierLevel(level, pageable);
//                } else if (name != null) {
//                    resultPage = supplierRepository.findBySupplierName(name, pageable);
//                } else if (status != null) {
//                    resultPage = supplierRepository.findByStatus(status, pageable);
//                } else if (tradeType != null) {
//                    resultPage = supplierRepository.findByTradeType(tradeType, pageable);
//                } else {
//                    resultPage = supplierRepository.findAll(pageable);
//                }
//            } else {
//                return Result.error("类型参数无效，请指定 'customer' 或 'supplier'");
//            }
//
//            // 生成 Redis Key
//            String redisKey = "recipient:" + UUID.randomUUID().toString();
//
//            redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//            // 将分页结果存储到 Redis 中
//            redisTemplate.opsForValue().set(redisKey, resultPage.getContent());
//
//            // 返回 Redis Key
//            return Result.success(redisKey);
//        } catch (Exception e) {
//            return Result.error("查询失败: " + e.getMessage());
//        }
//    }
//
//
////    /**
////     * 根据条件筛选客户和供应商，并将结果存储到 Redis
////     *
////     * @param acceptedEmailTypeId 接受的邮件类型 ID 列表
////     * @param tradeType           贸易类型
////     * @param customerLevel       客户等级
////     * @param supplierLevel       供应商等级
////     * @param status              分配状态
////     * @return Redis 的 key
////     */
////    public String filterAndStoreRecipients(List<String> acceptedEmailTypeId, Integer tradeType, Integer customerLevel, Integer supplierLevel, Integer status) {
////        // 筛选客户
////        List<Customer> customers = customerRepository.findByCriteria(acceptedEmailTypeId, tradeType, customerLevel, status);
////
////        // 筛选供应商
////        List<Supplier> suppliers = supplierRepository.findByCriteria(acceptedEmailTypeId, tradeType, supplierLevel, status);
////
////        // 合并客户和供应商
////        List<Object> recipients = List.of(customers, suppliers);
////
////        // 生成 Redis key
////        String redisKey = "recipients:" + UUID.randomUUID().toString();
////
////        // 存储到 Redis，设置过期时间为 1 小时
////        redisTemplate.opsForValue().set(redisKey, recipients, 1, TimeUnit.DAYS);
////
////        return redisKey;
////    }
//}