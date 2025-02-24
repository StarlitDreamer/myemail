package com.java.email.service;

import cn.hutool.core.util.IdUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.common.Result;
import com.java.email.dto.FilterCustomersDto;
import com.java.email.dto.FilterCustomersResponse;
import com.java.email.dto.SearchAllCustomersDto;
import com.java.email.dto.SearchAllCustomersResponse;
import com.java.email.entity.Commodity;
import com.java.email.entity.Customer;
import com.java.email.entity.Receiver;
import com.java.email.entity.Supplier;
import com.java.email.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.QueryBuilders;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private final ElasticsearchClient esClient;
    private final String INDEX_NAME = "customer";
    private final List<String> belongUserIds=new ArrayList<>();

    public CustomerService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    /**
     * 根据条件筛选客户
     *
     * @param belongUserId  所属用户ID
     * @param customerLevel 客户等级
     * @param customerName  客户名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param page          页码
     * @param size          每页大小
     * @return 符合条件的客户分页结果
     */
    public Result<Page<Customer>> findCustomersByCriteria(
            String belongUserId, Integer customerLevel, String customerName, Integer status, Integer tradeType,
            int page, int size,
            String currentUserId, int currentUserRole) { // 新增当前用户ID和角色参数
        try {
            Page<Customer> customers;

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 根据用户角色动态构建查询条件
            if (currentUserRole == 4) { // 普通用户
                // 只能查看 belongUserid 是自己的或公司的客户
                if (belongUserId != null && !belongUserId.equals(currentUserId) && !belongUserId.equals("1")) {
                    // 如果 belongUserId 不是当前用户也不是公司，返回空结果
                    return Result.success(Page.empty(pageable));
                }

                // 动态构建查询条件
                if (customerLevel != null) {
                    customers = customerRepository.findByCustomerLevelAndBelongUserIdIn(customerLevel, Arrays.asList(currentUserId, "1"), pageable);
                } else if (customerName != null) {
                    customers = customerRepository.findByCustomerNameAndBelongUserIdIn(customerName, Arrays.asList(currentUserId, "1"), pageable);
                } else if (status != null) {
                    customers = customerRepository.findByStatusAndBelongUserIdIn(status, Arrays.asList(currentUserId, "1"), pageable);
                } else if (tradeType != null) {
                    customers = customerRepository.findByTradeTypeAndBelongUserIdIn(tradeType, Arrays.asList(currentUserId, "1"), pageable);
                } else {
                    // 如果没有条件，返回 belongUserid 是自己的或公司的客户
                    customers = customerRepository.findByBelongUserIdIn(Arrays.asList(currentUserId, "1"), pageable);
                }
            } else if (currentUserRole == 2) { // 大管理
                // 可以查看所有客户
                if (belongUserId != null) {
                    customers = customerRepository.findByBelongUserId(belongUserId, pageable);
                } else if (customerLevel != null) {
                    customers = customerRepository.findByCustomerLevel(customerLevel, pageable);
                } else if (customerName != null) {
                    customers = customerRepository.findByCustomerName(customerName, pageable);
                } else if (status != null) {
                    customers = customerRepository.findByStatus(status, pageable);
                } else if (tradeType != null) {
                    customers = customerRepository.findByTradeType(tradeType, pageable);
                } else {
                    // 如果没有条件，返回所有客户
                    customers = customerRepository.findAll(pageable);
                }
            } else {
                // 其他角色，返回空结果
                return Result.success(Page.empty(pageable));
            }

            // 返回成功结果
            return Result.success(customers);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询客户失败: " + e.getMessage());
        }
    }
//    public Result<Page<Customer>> findCustomersByCriteria(String belongUserId, Integer customerLevel,
//                                                          String customerName, Integer status, Integer tradeType,
//                                                          int page, int size) {
//        try {
//            Page<Customer> customers;
//
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 动态构建查询条件
//            if (belongUserId != null) {
//                customers = customerRepository.findByBelongUserId(belongUserId, pageable);
//            } else if (customerLevel != null) {
//                customers = customerRepository.findByCustomerLevel(customerLevel, pageable);
//            } else if (customerName != null) {
//                customers = customerRepository.findByCustomerName(customerName, pageable);
//            } else if (status != null) {
//                customers = customerRepository.findByStatus(status, pageable);
//            } else if (tradeType != null) {
//                customers = customerRepository.findByTradeType(tradeType, pageable);
//            } else {
//                // 如果没有条件，返回所有客户（分页）
//                customers = customerRepository.findAll(pageable);
//            }
//
//            // 返回成功结果
//            return Result.success(customers);
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询客户失败: " + e.getMessage());
//        }
//    }

    /**
     * 根据条件筛选客户并存入redis中，返回redisKey
     *
     * @param belongUserId  所属用户ID
     * @param customerLevel 客户等级
     * @param customerName  客户名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param page          页码
     * @param size          每页大小
     * @return 符合条件的客户分页结果
     */
    public Result<String> findCustomersByCriteriaRedis(
            String belongUserId, Integer customerLevel, String customerName, Integer status, Integer tradeType,
            int page, int size,
            String currentUserId, int currentUserRole) { // 新增当前用户ID和角色参数
        try {
            Page<Customer> customers;

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 根据用户角色动态构建查询条件
            if (currentUserRole == 4) { // 普通用户
                // 只能查看 belongUserid 是自己的或公司的客户
                if (belongUserId != null && !belongUserId.equals(currentUserId) && !belongUserId.equals("1")) {
                    // 如果 belongUserId 不是当前用户也不是公司，返回空结果
                    return Result.success("customer:search:empty"); // 返回一个空的 Redis Key
                }

                // 动态构建查询条件
                if (customerLevel != null) {
                    customers = customerRepository.findByCustomerLevelAndBelongUserIdIn(customerLevel, Arrays.asList(currentUserId, "1"), pageable);
                } else if (customerName != null) {
                    customers = customerRepository.findByCustomerNameAndBelongUserIdIn(customerName, Arrays.asList(currentUserId, "1"), pageable);
                } else if (status != null) {
                    customers = customerRepository.findByStatusAndBelongUserIdIn(status, Arrays.asList(currentUserId, "1"), pageable);
                } else if (tradeType != null) {
                    customers = customerRepository.findByTradeTypeAndBelongUserIdIn(tradeType, Arrays.asList(currentUserId, "1"), pageable);
                } else {
                    // 如果没有条件，返回 belongUserid 是自己的或公司的客户
                    customers = customerRepository.findByBelongUserIdIn(Arrays.asList(currentUserId, "1"), pageable);
                }
            } else if (currentUserRole == 2) { // 大管理
                // 可以查看所有客户
                if (belongUserId != null) {
                    customers = customerRepository.findByBelongUserId(belongUserId, pageable);
                } else if (customerLevel != null) {
                    customers = customerRepository.findByCustomerLevel(customerLevel, pageable);
                } else if (customerName != null) {
                    customers = customerRepository.findByCustomerName(customerName, pageable);
                } else if (status != null) {
                    customers = customerRepository.findByStatus(status, pageable);
                } else if (tradeType != null) {
                    customers = customerRepository.findByTradeType(tradeType, pageable);
                } else {
                    // 如果没有条件，返回所有客户
                    customers = customerRepository.findAll(pageable);
                }
            } else {
                // 其他角色，返回空结果
                return Result.success("customer:search:empty"); // 返回一个空的 Redis Key
            }

            // 生成唯一的 Redis Key
            String redisKey = "customer:search:" + UUID.randomUUID().toString();

            // 设置 Redis 的 Value 序列化器为 JSON 格式
            redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

            // 将查询结果的内容（List<Customer>）存入 Redis
            redisTemplate.opsForValue().set(redisKey, customers.getContent());

            // 返回 Redis Key
            return Result.success(redisKey);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询客户失败: " + e.getMessage());
        }
    }
//    public Result<String> findCustomersByCriteriaRedis(String belongUserId, Integer customerLevel,
//                                                       String customerName, Integer status, Integer tradeType,
//                                                       int page, int size) {
//        try {
//            Page<Customer> customers;
//
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 动态构建查询条件
//            if (belongUserId != null) {
//                customers = customerRepository.findByBelongUserId(belongUserId, pageable);
//            } else if (customerLevel != null) {
//                customers = customerRepository.findByCustomerLevel(customerLevel, pageable);
//            } else if (customerName != null) {
//                customers = customerRepository.findByCustomerName(customerName, pageable);
//            } else if (status != null) {
//                customers = customerRepository.findByStatus(status, pageable);
//            } else if (tradeType != null) {
//                customers = customerRepository.findByTradeType(tradeType, pageable);
//            } else {
//                // 如果没有条件，返回所有客户（分页）
//                customers = customerRepository.findAll(pageable);
//            }
//
//            // 生成唯一的 Redis Key
//            String redisKey = "customer:search:" + UUID.randomUUID().toString();
//
//            // 设置 Redis 的 Value 序列化器为 JSON 格式
//            redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//
//            // 将查询结果的内容（List<Customer>）存入 Redis，设置过期时间为 10 分钟
//            redisTemplate.opsForValue().set(redisKey, customers.getContent());
//
//            // 返回 Redis Key
//            return Result.success(redisKey);
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询客户失败: " + e.getMessage());
//        }
//    }

public FilterCustomersResponse FilterFindCustomers(String currentUserId,int currentUserRole,FilterCustomersDto filterCustomersDto) throws IOException {
        int num=Integer.parseInt(filterCustomersDto.getPage_num());
    int size= Integer.parseInt(filterCustomersDto.getPage_size());
    String commodityName = filterCustomersDto.commodity_name;
    List<String> areaId = filterCustomersDto.area_id;
    List<String> countryId = filterCustomersDto.country_id;
    Integer tradeType =filterCustomersDto.trade_type;
    Integer receiverLevel = filterCustomersDto.receiver_level;
    BoolQuery.Builder boolQuery = new BoolQuery.Builder();
    Map<String, Object> filters = new HashMap<>();

    if(currentUserRole==4){
        belongUserIds.add(currentUserId);
        belongUserIds.add("1");
        filters.put("belongUserId",belongUserIds);
    }
    if (commodityName!= null && !commodityName.isEmpty()) {
        SearchResponse<Commodity> searchResponse = esClient.search(s -> s
                .index("commodity")
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
                )), Commodity.class);
        List<String> CustomerIds = searchResponse.hits().hits().stream()
                .map(hit -> hit.source().getCommodityId())
                .toList();
        filters.put("commodity_id", CustomerIds);
    }
    if (areaId != null && !areaId.isEmpty()) {
        filters.put("area_id", areaId);
    }
    if (countryId != null && !countryId.isEmpty()) {
        filters.put("country_id", countryId);
    }
    if (tradeType !=null) {
        filters.put("trade_type", tradeType);
    }
    if (receiverLevel != null) {
        filters.put("receiver_level", receiverLevel);
    }

    filters.forEach((key, value) -> {
        if (value instanceof List<?> listValue && !listValue.isEmpty()) {
            List<FieldValue> fieldValues = listValue.stream()
                    .map(v -> FieldValue.of(v.toString()))
                    .toList();
            boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
        }
        if (value instanceof String stringValue && !stringValue.isEmpty()) {
            boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
        }
        if (value instanceof Integer intValue) {
            boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of( intValue))));
        }

    });
    SearchResponse<Customer> searchResponse;
    if (filters.isEmpty()) {
        searchResponse = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(boolQuery.build()))
                .from((num- 1) * size)
                .size(size), Customer.class);
    } else {
        searchResponse = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(boolQuery.build()))
                .from((num - 1) * size)
                .size(size), Customer.class);
    }

    List<Receiver> receiverList = new ArrayList<>();
    for(Hit<Customer> CustomerHit:searchResponse.hits().hits()){
        Customer receiver = CustomerHit.source();
        if(receiver==null){
          continue;
        }
        receiverList.add(new Receiver(receiver.getCustomerId(),receiver.getCustomerName()));
    }
    return new FilterCustomersResponse(receiverList,receiverList.size(),num,size);
}
    public FilterCustomersResponse FilterFindSupplier(String currentUserId,int currentUserRole,FilterCustomersDto filterCustomersDto) throws IOException {
        int num=Integer.parseInt(filterCustomersDto.getPage_num());
        int size= Integer.parseInt(filterCustomersDto.getPage_size());
        String commodityName = filterCustomersDto.commodity_name;
        List<String> areaId = filterCustomersDto.area_id;
        List<String> countryId = filterCustomersDto.country_id;
        Integer tradeType =filterCustomersDto.trade_type;
        Integer receiverLevel = filterCustomersDto.receiver_level;
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        Map<String, Object> filters = new HashMap<>();

        if(currentUserRole==4){
            belongUserIds.add(currentUserId);
            belongUserIds.add("1");
            filters.put("belongUserId",belongUserIds);
        }
        if (commodityName!= null && !commodityName.isEmpty()) {
            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
                    .index("commodity")
                    .query(q -> q.bool(b -> b
                            .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
                    )), Commodity.class);
            List<String> CustomerIds = searchResponse.hits().hits().stream()
                    .map(hit -> hit.source().getCommodityId())
                    .toList();
            filters.put("commodity_id", CustomerIds);
        }
        if (areaId != null && !areaId.isEmpty()) {
            filters.put("area_id", areaId);
        }
        if (countryId != null && !countryId.isEmpty()) {
            filters.put("country_id", countryId);
        }
        if (tradeType !=null) {
            filters.put("trade_type", tradeType);
        }
        if (receiverLevel != null) {
            filters.put("receiver_level", receiverLevel);
        }

        filters.forEach((key, value) -> {
            if (value instanceof List<?> listValue && !listValue.isEmpty()) {
                List<FieldValue> fieldValues = listValue.stream()
                        .map(v -> FieldValue.of(v.toString()))
                        .toList();
                boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
            }
            if (value instanceof String stringValue && !stringValue.isEmpty()) {
                boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
            }
            if (value instanceof Integer intValue) {
                boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of( intValue))));
            }

        });
        SearchResponse<Supplier> searchResponse;
        if (filters.isEmpty()) {
            searchResponse = esClient.search(s -> s
                    .index("supplier")
                    .query(q -> q.bool(boolQuery.build()))
                    .from((num- 1) * size)
                    .size(size), Supplier.class);
        } else {
            searchResponse = esClient.search(s -> s
                    .index("supplier")
                    .query(q -> q.bool(boolQuery.build()))
                    .from((num - 1) * size)
                    .size(size), Supplier.class);
        }

        List<Receiver> receiverList = new ArrayList<>();
        for(Hit<Supplier> CustomerHit:searchResponse.hits().hits()){
            Supplier receiver = CustomerHit.source();
            if(receiver==null){
                continue;
            }
            receiverList.add(new Receiver(receiver.getSupplierId(),receiver.getSupplierName()));
        }
        return new FilterCustomersResponse(receiverList,receiverList.size(),num,size);
    }
public SearchAllCustomersResponse findCustomers(String currentUserId,int currentUserRole,SearchAllCustomersDto searchAllCustomersDto) throws IOException {
    CountResponse countResponse = esClient.count(c -> c
            .index(INDEX_NAME)  // 索引名称
    );
    int totalCount = (int)countResponse.count();
    String commodityName = searchAllCustomersDto.commodity_name;
    List<String> areaId = searchAllCustomersDto.area_id;
    List<String> countryId = searchAllCustomersDto.country_id;
    Integer tradeType =searchAllCustomersDto.trade_type;
    Integer receiverLevel = searchAllCustomersDto.receiver_level;
    BoolQuery.Builder boolQuery = new BoolQuery.Builder();
    Map<String, Object> filters = new HashMap<>();
    if(currentUserRole==4){
        belongUserIds.add(currentUserId);
        belongUserIds.add("1");
        filters.put("belongUserId",belongUserIds);
    }
    if (commodityName!= null && !commodityName.isEmpty()) {
        SearchResponse<Commodity> searchResponse = esClient.search(s -> s
                .index("commodity")
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
                )), Commodity.class);
        List<String> CustomerIds = searchResponse.hits().hits().stream()
                .map(hit -> hit.source().getCommodityId())
                .toList();
        filters.put("commodity_id", CustomerIds);
    }
    if (areaId != null && !areaId.isEmpty()) {
        filters.put("area_id", areaId);
    }
    if (countryId != null && !countryId.isEmpty()) {
        filters.put("country_id", countryId);
    }
    if (tradeType !=null) {
        filters.put("trade_type", tradeType);
    }
    if (receiverLevel != null) {
        filters.put("receiver_level", receiverLevel);
    }
    filters.forEach((key, value) -> {
        if (value instanceof List<?> listValue && !listValue.isEmpty()) {
            List<FieldValue> fieldValues = listValue.stream()
                    .map(v -> FieldValue.of(v.toString()))
                    .toList();
            boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
        }
        if (value instanceof String stringValue && !stringValue.isEmpty()) {
            boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
        }
        if (value instanceof Integer intValue) {
            boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of(intValue))));
        }

    });
    SearchResponse<Customer> searchResponse;
    if (filters.isEmpty()) {
        searchResponse = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(boolQuery.build()))
                .from(0)
                .size(totalCount), Customer.class);
    } else {
        searchResponse = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(boolQuery.build()))
                .size(totalCount), Customer.class);
    }

    List<Receiver> receiverList = new ArrayList<>();
    for(Hit<Customer> CustomerHit:searchResponse.hits().hits()){
        Customer receiver = CustomerHit.source();
        if(receiver==null){
            continue;
        }
        receiverList.add(new Receiver(receiver.getCustomerId(),receiver.getCustomerName()));
    }
    String receiver_key = "receiver_list:" + IdUtil.fastUUID();
    ValueOperations<String, Object> operations = redisTemplate.opsForValue();
    operations.set(receiver_key, receiverList, 10, TimeUnit.MINUTES);
    return new SearchAllCustomersResponse(receiverList.size(),receiver_key);
}
    public SearchAllCustomersResponse FindSupplier(String currentUserId,int currentUserRole,SearchAllCustomersDto searchAllCustomersDto) throws IOException {
        CountResponse countResponse = esClient.count(c -> c
                .index("supplier")  // 索引名称
        );
        int totalCount = (int)countResponse.count();
        String commodityName = searchAllCustomersDto.commodity_name;
        List<String> areaId = searchAllCustomersDto.area_id;
        List<String> countryId = searchAllCustomersDto.country_id;
        Integer tradeType =searchAllCustomersDto.trade_type;
        Integer receiverLevel = searchAllCustomersDto.receiver_level;
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        Map<String, Object> filters = new HashMap<>();

        if(currentUserRole==4){
            belongUserIds.add(currentUserId);
            belongUserIds.add("1");
            filters.put("belongUserId",belongUserIds);
        }
        if (commodityName!= null && !commodityName.isEmpty()) {
            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
                    .index("commodity")
                    .query(q -> q.bool(b -> b
                            .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
                    )), Commodity.class);
            List<String> CustomerIds = searchResponse.hits().hits().stream()
                    .map(hit -> hit.source().getCommodityId())
                    .toList();
            filters.put("commodity_id", CustomerIds);
        }
        if (areaId != null && !areaId.isEmpty()) {
            filters.put("area_id", areaId);
        }
        if (countryId != null && !countryId.isEmpty()) {
            filters.put("country_id", countryId);
        }
        if (tradeType !=null) {
            filters.put("trade_type", tradeType);
        }
        if (receiverLevel != null) {
            filters.put("receiver_level", receiverLevel);
        }

        filters.forEach((key, value) -> {
            if (value instanceof List<?> listValue && !listValue.isEmpty()) {
                List<FieldValue> fieldValues = listValue.stream()
                        .map(v -> FieldValue.of(v.toString()))
                        .toList();
                boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
            }
            if (value instanceof String stringValue && !stringValue.isEmpty()) {
                boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
            }
            if (value instanceof Integer intValue) {
                boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of( intValue))));
            }

        });
        SearchResponse<Supplier> searchResponse;
        if (filters.isEmpty()) {
            searchResponse = esClient.search(s -> s
                    .index("supplier")
                    .query(q -> q.bool(boolQuery.build()))
                    .from(0)
                    .size(totalCount), Supplier.class);
        } else {
            searchResponse = esClient.search(s -> s
                    .index("supplier")
                    .query(q -> q.bool(boolQuery.build()))
                    .from(0)
                    .size(totalCount), Supplier.class);
        }

        List<Receiver> receiverList = new ArrayList<>();
        for(Hit<Supplier> CustomerHit:searchResponse.hits().hits()){
            Supplier receiver = CustomerHit.source();
            if(receiver==null){
                continue;
            }
            receiverList.add(new Receiver(receiver.getSupplierId(),receiver.getSupplierName()));
        }
        String supplier_key = "supplier_list:" + IdUtil.fastUUID();
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(supplier_key, receiverList, 10, TimeUnit.MINUTES);
        return new SearchAllCustomersResponse(receiverList.size(),supplier_key);
    }
}