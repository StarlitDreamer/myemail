//package com.java.email.service;
//
//
//import cn.hutool.core.util.IdUtil;
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch._types.FieldValue;
//import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
//import co.elastic.clients.elasticsearch.core.CountResponse;
//import co.elastic.clients.elasticsearch.core.SearchResponse;
//import co.elastic.clients.elasticsearch.core.search.Hit;
//import com.java.email.dto.FilterCustomersDto;
//import com.java.email.dto.FilterCustomersResponse;
//import com.java.email.dto.SearchAllCustomersDto;
//import com.java.email.dto.SearchAllCustomersResponse;
//import com.java.email.entity.Commodity;
//import com.java.email.entity.Customer;
//import com.java.email.entity.Receiver;
//import com.java.email.entity.Supplier;
//import com.java.email.repository.CustomerRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//@Service
//public class CustomerService {
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//    private final ElasticsearchClient esClient;
//    private final String INDEX_NAME = "customer";
//    private final List<String> belongUserIds = new ArrayList<>();
//
//    public CustomerService(ElasticsearchClient esClient) {
//        this.esClient = esClient;
//    }
//
//    /**
//     * 根据 customerId 数组查询对应的 emails，并去除重复的 emails。
//     *
//     * @param customerIds 客户 ID 数组
//     * @return 去重后的 emails 列表
//     */
//    public List<String> getUniqueEmailsByCustomerIds(List<String> customerIds) {
//        List<Customer> customers = customerRepository.findByCustomerIdIn(customerIds);
//
//        // 使用 HashSet 来去除重复的 emails
//        Set<String> uniqueEmails = new HashSet<>();
//
//        for (Customer customer : customers) {
//            uniqueEmails.addAll(customer.getEmails());
//        }
//
//        return uniqueEmails.stream().collect(Collectors.toList());
//    }
//
//
//
//
//public FilterCustomersResponse FilterFindCustomers(String currentUserId, int currentUserRole, FilterCustomersDto filterCustomersDto) throws IOException {
////        int num=Integer.parseInt(filterCustomersDto.getPage_num());
////    int size= Integer.parseInt(filterCustomersDto.getPage_size());
//    int num=filterCustomersDto.getPage_num();
//    int size=filterCustomersDto.getPage_size();
//    String commodityName = filterCustomersDto.commodity_name;
//    List<String> areaId = filterCustomersDto.area_id;
//    List<String> countryId = filterCustomersDto.customer_country_id;
//    Integer tradeType =filterCustomersDto.trade_type;
//    Integer customerLevel = filterCustomersDto.customer_level;
//    BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//    Map<String, Object> filters = new HashMap<>();
//
//    if(currentUserRole==4){
//        belongUserIds.add(currentUserId);
//        belongUserIds.add("1");
//        filters.put("belongUserId",belongUserIds);
//    }
//    if (commodityName!= null && !commodityName.isEmpty()) {
//        SearchResponse<Commodity> searchResponse = esClient.search(s -> s
//                .index("commodity")
//                .query(q -> q.bool(b -> b
//                        .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
//                )), Commodity.class);
//        List<String> CustomerIds = searchResponse.hits().hits().stream()
//                .map(hit -> hit.source().getCommodityId())
//                .toList();
//        filters.put("commodity_id", CustomerIds);
//    }
//    if (areaId != null && !areaId.isEmpty()) {
//        filters.put("area_id", areaId);
//    }
//    if (countryId != null && !countryId.isEmpty()) {
//        filters.put("country_id", countryId);
//    }
//    if (tradeType !=null) {
//        filters.put("trade_type", tradeType);
//    }
//    if (receiverLevel != null) {
//        filters.put("receiver_level", receiverLevel);
//    }
//
//    filters.forEach((key, value) -> {
//        if (value instanceof List<?> listValue && !listValue.isEmpty()) {
//            List<FieldValue> fieldValues = listValue.stream()
//                    .map(v -> FieldValue.of(v.toString()))
//                    .toList();
//            boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
//        }
//        if (value instanceof String stringValue && !stringValue.isEmpty()) {
//            boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
//        }
//        if (value instanceof Integer intValue) {
//            boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of( intValue))));
//        }
//
//    });
//    SearchResponse<Customer> searchResponse;
//    if (filters.isEmpty()) {
//        searchResponse = esClient.search(s -> s
//                .index(INDEX_NAME)
//                .query(q -> q.bool(boolQuery.build()))
//                .from((num- 1) * size)
//                .size(size), Customer.class);
//    } else {
//        searchResponse = esClient.search(s -> s
//                .index(INDEX_NAME)
//                .query(q -> q.bool(boolQuery.build()))
//                .from((num - 1) * size)
//                .size(size), Customer.class);
//    }
//
//    List<Receiver> receiverList = new ArrayList<>();
//    for(Hit<Customer> CustomerHit:searchResponse.hits().hits()){
//        Customer receiver = CustomerHit.source();
//        if(receiver==null){
//          continue;
//        }
//        receiverList.add(new Receiver(receiver.getCustomerId(),receiver.getCustomerName()));
//    }
//    return new FilterCustomersResponse(receiverList,receiverList.size(),num,size);
//}
//    public FilterCustomersResponse FilterFindSupplier(String currentUserId,int currentUserRole,FilterCustomersDto filterCustomersDto) throws IOException {
//        int num=filterCustomersDto.getPage_num();
//        int size=filterCustomersDto.getPage_size();
//        String commodityName = filterCustomersDto.commodity_name;
//        List<String> areaId = filterCustomersDto.area_id;
//        List<String> countryId = filterCustomersDto.customer_country_id;
//        Integer tradeType =filterCustomersDto.trade_type;
//        Integer customerLevel = filterCustomersDto.customer_level;
//        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//        Map<String, Object> filters = new HashMap<>();
//
//        if(currentUserRole==4){
//            belongUserIds.add(currentUserId);
//            belongUserIds.add("1");
//            filters.put("belongUserId",belongUserIds);
//        }
//        if (commodityName!= null && !commodityName.isEmpty()) {
//            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
//                    .index("commodity")
//                    .query(q -> q.bool(b -> b
//                            .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
//                    )), Commodity.class);
//            List<String> CustomerIds = searchResponse.hits().hits().stream()
//                    .map(hit -> hit.source().getCommodityId())
//                    .toList();
//            filters.put("commodity_id", CustomerIds);
//        }
//        if (areaId != null && !areaId.isEmpty()) {
//            filters.put("area_id", areaId);
//        }
//        if (countryId != null && !countryId.isEmpty()) {
//            filters.put("country_id", countryId);
//        }
//        if (tradeType !=null) {
//            filters.put("trade_type", tradeType);
//        }
//        if (receiverLevel != null) {
//            filters.put("receiver_level", receiverLevel);
//        }
//
//        filters.forEach((key, value) -> {
//            if (value instanceof List<?> listValue && !listValue.isEmpty()) {
//                List<FieldValue> fieldValues = listValue.stream()
//                        .map(v -> FieldValue.of(v.toString()))
//                        .toList();
//                boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
//            }
//            if (value instanceof String stringValue && !stringValue.isEmpty()) {
//                boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
//            }
//            if (value instanceof Integer intValue) {
//                boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of( intValue))));
//            }
//
//        });
//        SearchResponse<Supplier> searchResponse;
//        if (filters.isEmpty()) {
//            searchResponse = esClient.search(s -> s
//                    .index("supplier")
//                    .query(q -> q.bool(boolQuery.build()))
//                    .from((num- 1) * size)
//                    .size(size), Supplier.class);
//        } else {
//            searchResponse = esClient.search(s -> s
//                    .index("supplier")
//                    .query(q -> q.bool(boolQuery.build()))
//                    .from((num - 1) * size)
//                    .size(size), Supplier.class);
//        }
//
//        List<Receiver> receiverList = new ArrayList<>();
//        for(Hit<Supplier> CustomerHit:searchResponse.hits().hits()){
//            Supplier receiver = CustomerHit.source();
//            if(receiver==null){
//                continue;
//            }
//            receiverList.add(new Receiver(receiver.getSupplierId(),receiver.getSupplierName()));
//        }
//        return new FilterCustomersResponse(receiverList,receiverList.size(),num,size);
//    }
//public SearchAllCustomersResponse findCustomers(String currentUserId, int currentUserRole, SearchAllCustomersDto searchAllCustomersDto) throws IOException {
//    CountResponse countResponse = esClient.count(c -> c
//            .index(INDEX_NAME)  // 索引名称
//    );
//    int totalCount = (int)countResponse.count();
//    String commodityName = searchAllCustomersDto.commodity_name;
//    List<String> areaId = searchAllCustomersDto.area_id;
//    List<String> countryId = searchAllCustomersDto.country_id;
//    Integer tradeType =searchAllCustomersDto.trade_type;
//    Integer receiverLevel = searchAllCustomersDto.receiver_level;
//    BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//    Map<String, Object> filters = new HashMap<>();
//    if(currentUserRole==4){
//        belongUserIds.add(currentUserId);
//        belongUserIds.add("1");
//        filters.put("belongUserId",belongUserIds);
//    }
//    if (commodityName!= null && !commodityName.isEmpty()) {
//        SearchResponse<Commodity> searchResponse = esClient.search(s -> s
//                .index("commodity")
//                .query(q -> q.bool(b -> b
//                        .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
//                )), Commodity.class);
//        List<String> CustomerIds = searchResponse.hits().hits().stream()
//                .map(hit -> hit.source().getCommodityId())
//                .toList();
//        filters.put("commodity_id", CustomerIds);
//    }
//    if (areaId != null && !areaId.isEmpty()) {
//        filters.put("area_id", areaId);
//    }
//    if (countryId != null && !countryId.isEmpty()) {
//        filters.put("country_id", countryId);
//    }
//    if (tradeType !=null) {
//        filters.put("trade_type", tradeType);
//    }
//    if (receiverLevel != null) {
//        filters.put("receiver_level", receiverLevel);
//    }
//    filters.forEach((key, value) -> {
//        if (value instanceof List<?> listValue && !listValue.isEmpty()) {
//            List<FieldValue> fieldValues = listValue.stream()
//                    .map(v -> FieldValue.of(v.toString()))
//                    .toList();
//            boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
//        }
//        if (value instanceof String stringValue && !stringValue.isEmpty()) {
//            boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
//        }
//        if (value instanceof Integer intValue) {
//            boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of(intValue))));
//        }
//
//    });
//    SearchResponse<Customer> searchResponse;
//    if (filters.isEmpty()) {
//        searchResponse = esClient.search(s -> s
//                .index(INDEX_NAME)
//                .query(q -> q.bool(boolQuery.build()))
//                .from(0)
//                .size(totalCount), Customer.class);
//    } else {
//        searchResponse = esClient.search(s -> s
//                .index(INDEX_NAME)
//                .query(q -> q.bool(boolQuery.build()))
//                .size(totalCount), Customer.class);
//    }
//
//    List<Receiver> receiverList = new ArrayList<>();
//    for(Hit<Customer> CustomerHit:searchResponse.hits().hits()){
//        Customer receiver = CustomerHit.source();
//        if(receiver==null){
//            continue;
//        }
//        receiverList.add(new Receiver(receiver.getCustomerId(),receiver.getCustomerName()));
//    }
//    String receiver_key = "receiver_list:" + IdUtil.fastUUID();
//    ValueOperations<String, Object> operations = redisTemplate.opsForValue();
//    operations.set(receiver_key, receiverList, 10, TimeUnit.MINUTES);
//    return new SearchAllCustomersResponse(receiverList.size(),receiver_key);
//}
//    public SearchAllCustomersResponse FindSupplier(String currentUserId,int currentUserRole,SearchAllCustomersDto searchAllCustomersDto) throws IOException {
//        CountResponse countResponse = esClient.count(c -> c
//                .index("supplier")  // 索引名称
//        );
//        int totalCount = (int)countResponse.count();
//        String commodityName = searchAllCustomersDto.commodity_name;
//        List<String> areaId = searchAllCustomersDto.area_id;
//        List<String> countryId = searchAllCustomersDto.country_id;
//        Integer tradeType =searchAllCustomersDto.trade_type;
//        Integer receiverLevel = searchAllCustomersDto.receiver_level;
//        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//        Map<String, Object> filters = new HashMap<>();
//
//        if(currentUserRole==4){
//            belongUserIds.add(currentUserId);
//            belongUserIds.add("1");
//            filters.put("belongUserId",belongUserIds);
//        }
//        if (commodityName!= null && !commodityName.isEmpty()) {
//            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
//                    .index("commodity")
//                    .query(q -> q.bool(b -> b
//                            .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
//                    )), Commodity.class);
//            List<String> CustomerIds = searchResponse.hits().hits().stream()
//                    .map(hit -> hit.source().getCommodityId())
//                    .toList();
//            filters.put("commodity_id", CustomerIds);
//        }
//        if (areaId != null && !areaId.isEmpty()) {
//            filters.put("area_id", areaId);
//        }
//        if (countryId != null && !countryId.isEmpty()) {
//            filters.put("country_id", countryId);
//        }
//        if (tradeType !=null) {
//            filters.put("trade_type", tradeType);
//        }
//        if (receiverLevel != null) {
//            filters.put("receiver_level", receiverLevel);
//        }
//
//        filters.forEach((key, value) -> {
//            if (value instanceof List<?> listValue && !listValue.isEmpty()) {
//                List<FieldValue> fieldValues = listValue.stream()
//                        .map(v -> FieldValue.of(v.toString()))
//                        .toList();
//                boolQuery.should(q -> q.terms(t -> t.field(key).terms(v -> v.value(fieldValues))));
//            }
//            if (value instanceof String stringValue && !stringValue.isEmpty()) {
//                boolQuery.should(q -> q.match(m -> m.field(key).query(stringValue)));
//            }
//            if (value instanceof Integer intValue) {
//                boolQuery.must(m -> m.term(t -> t.field(key).value(FieldValue.of( intValue))));
//            }
//
//        });
//        SearchResponse<Supplier> searchResponse;
//        if (filters.isEmpty()) {
//            searchResponse = esClient.search(s -> s
//                    .index("supplier")
//                    .query(q -> q.bool(boolQuery.build()))
//                    .from(0)
//                    .size(totalCount), Supplier.class);
//        } else {
//            searchResponse = esClient.search(s -> s
//                    .index("supplier")
//                    .query(q -> q.bool(boolQuery.build()))
//                    .from(0)
//                    .size(totalCount), Supplier.class);
//        }
//
//        List<Receiver> receiverList = new ArrayList<>();
//        for(Hit<Supplier> CustomerHit:searchResponse.hits().hits()){
//            Supplier receiver = CustomerHit.source();
//            if(receiver==null){
//                continue;
//            }
//            receiverList.add(new Receiver(receiver.getSupplierId(),receiver.getSupplierName()));
//        }
//        String supplier_key = "supplier_list:" + IdUtil.fastUUID();
//        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
//        operations.set(supplier_key, receiverList, 10, TimeUnit.MINUTES);
//        return new SearchAllCustomersResponse(receiverList.size(),supplier_key);
//    }
//}