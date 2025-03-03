package com.java.email.service;

import cn.hutool.core.util.IdUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.model.dto.FilterSupplierDto;
import com.java.email.model.dto.SearchAllSupplierDto;
import com.java.email.model.entity.Area;
import com.java.email.model.entity.Commodity;
import com.java.email.model.entity.ReceiverSupplier;
import com.java.email.model.entity.Supplier;
import com.java.email.model.response.FilterSupplierResponse;
import com.java.email.model.response.SearchAllCustomerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    // 注入 RedisTemplate，用于与 Redis 进行数据交互
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Elasticsearch 客户端
    private final ElasticsearchClient esClient;

    // Elasticsearch 中存储客户信息的索引名称
    private final String INDEX_NAME = "supplier";

    // 用于存储属于用户的ID列表
    private final List<String> belongUserIds = new ArrayList<>();

    // 构造函数，注入 ElasticsearchClient
    public SupplierService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public FilterSupplierResponse FilterFindSupplier(String currentUserId, int currentUserRole, FilterSupplierDto filterSupplierDto) throws IOException {
        int num=filterSupplierDto.getPage_num();
        int size=filterSupplierDto.getPage_size();
        String commodityName = filterSupplierDto.commodity_name;
        List<String> areaId = filterSupplierDto.area_id;
        List<String> supplierCountryId = filterSupplierDto.supplier_country_id;
        Integer tradeType = filterSupplierDto.trade_type;
        Integer supplierLevel = filterSupplierDto.supplier_level;

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        Map<String, Object> filters = new HashMap<>();


        if (currentUserRole == 4) {
            belongUserIds.add(currentUserId);
            belongUserIds.add("1");
            filters.put("belong_user_id", belongUserIds);
            System.out.println(belongUserIds);
        }

        if (commodityName != null && !commodityName.isEmpty()) {
            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
                    .index("commodity")
                    .query(q -> q.bool(b -> b
//                            .must(m -> m.term(t -> t.field("commodity_name").value(commodityName)))
                                    .must(m -> m.match(t -> t.field("commodity_name").query(commodityName)))
                    )), Commodity.class);
            List<String> CustomerIds = searchResponse.hits().hits().stream()
                    .map(hit -> hit.source().getCommodityId())
                    .toList();
            filters.put("commodity_id", CustomerIds);
        }

        List<String> areaCountryIds = null;

        if (areaId != null && !areaId.isEmpty()) {
            List<FieldValue> fieldValues = areaId.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());

            // 查询area索引，获取与areaId匹配的area_country
            SearchResponse<Area> areaSearchResponse = esClient.search(s -> s
                    .index("area")  // 查询的索引为 "area"
                    .query(q -> q.bool(b -> b
                            // 使用terms查询area_id字段，查询提供的areaId
                            .must(m -> m.terms(t -> t.field("area_id").terms(v -> v.value(fieldValues))))
                    )), Area.class);

            // 提取area_country，进行flatten操作以获得所有国家ID
            areaCountryIds = areaSearchResponse.hits().hits().stream()
                    .flatMap(hit -> hit.source().getAreaCountry().stream())  // 展开areaCountry中的List<String>
                    .filter(Objects::nonNull)  // 排除空值
                    .distinct()  // 去重
                    .collect(Collectors.toList());  // 转换为List<String>


            // 将area_country作为过滤条件
            if (!areaCountryIds.isEmpty()) {
                filters.put("supplier_country_id", areaCountryIds);  // 将area_country作为筛选条件放入filters
            }
        }

        // 如果提供了 customerCountryId，则合并并去重
        if (supplierCountryId != null && !supplierCountryId.isEmpty()) {
            // 合并并去重
            Set<String> mergedCountryIds = new HashSet<>(areaCountryIds);
            mergedCountryIds.addAll(supplierCountryId);

            // 将最终的 mergedCountryIds 作为过滤条件加入 filters
            filters.put("supplier_country_id", new ArrayList<>(mergedCountryIds));  // ArrayList<String> 类型
        } else {
            // 如果没有提供 customerCountryId，则直接使用 areaCountryIds 作为过滤条件
            filters.put("supplier_country_id", areaCountryIds);  // List<String> 类型
        }

        if (tradeType != null) {
            filters.put("trade_type", tradeType);
        }
        if (supplierLevel != null) {
            filters.put("supplier_level", supplierLevel);
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

        SearchResponse<Supplier> searchResponse;
        if (filters.isEmpty()) {
            searchResponse = esClient.search(s -> s
                    .index("supplier")
                    .query(q -> q.bool(boolQuery.build()))
                    .from((num - 1) * size)
                    .size(size), Supplier.class);
        } else {
            searchResponse = esClient.search(s -> s
                    .index("supplier")
                    .query(q -> q.bool(boolQuery.build()))
                    .from((num - 1) * size)
                    .size(size), Supplier.class);
        }

        List<ReceiverSupplier> receiverList = new ArrayList<>();
        for (Hit<Supplier> CustomerHit : searchResponse.hits().hits()) {
            Supplier receiver = CustomerHit.source();
            if (receiver == null) {
                continue;
            }
            receiverList.add(new ReceiverSupplier(receiver.getSupplierId(), receiver.getSupplierName()));
        }
        belongUserIds.clear();
        return new FilterSupplierResponse(receiverList, receiverList.size(), num, size);
    }


    public SearchAllCustomerResponse FindFindAllSupplier(String currentUserId, int currentUserRole, SearchAllSupplierDto searchAllSupplierDto) throws IOException {
        CountResponse countResponse = esClient.count(c -> c
                .index("supplier")  // 索引名称
        );
        int totalCount = (int) countResponse.count();
        String commodityName = searchAllSupplierDto.commodity_name;
        List<String> areaId = searchAllSupplierDto.area_id;
        List<String> supplierCountryId = searchAllSupplierDto.supplier_country_id;
        Integer tradeType = searchAllSupplierDto.trade_type;
        Integer supplierLevel = searchAllSupplierDto.supplier_level;

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        Map<String, Object> filters = new HashMap<>();

        // 用于存储属于用户的ID列表
        final List<String> belongUserIds = new ArrayList<>();

        if (currentUserRole == 4) {
            belongUserIds.add(currentUserId);
            belongUserIds.add("1");
            filters.put("belong_user_d", belongUserIds);
            System.out.println(belongUserIds);
        }
        if (commodityName != null && !commodityName.isEmpty()) {
            SearchResponse<Commodity> searchResponse = esClient.search(s -> s
                    .index("commodity")
                    .query(q -> q.bool(b -> b
                            .must(m -> m.match(t -> t.field("commodity_name").query(commodityName)))
                    )), Commodity.class);
            List<String> CustomerIds = searchResponse.hits().hits().stream()
                    .map(hit -> hit.source().getCommodityId())
                    .toList();
            filters.put("commodity_id", CustomerIds);
        }

        List<String> areaCountryIds = null;//存储国家id

        if (areaId != null && !areaId.isEmpty()) {
            List<FieldValue> fieldValues = areaId.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());

            // 查询area索引，获取与areaId匹配的area_country
            SearchResponse<Area> areaSearchResponse = esClient.search(s -> s
                    .index("area")  // 查询的索引为 "area"
                    .query(q -> q.bool(b -> b
                            // 使用terms查询area_id字段，查询提供的areaId
                            .must(m -> m.terms(t -> t.field("area_id").terms(v -> v.value(fieldValues))))
                    )), Area.class);

            // 提取area_country，进行flatten操作以获得所有国家ID
            areaCountryIds = areaSearchResponse.hits().hits().stream()
                    .flatMap(hit -> hit.source().getAreaCountry().stream())  // 展开areaCountry中的List<String>
                    .filter(Objects::nonNull)  // 排除空值
                    .distinct()  // 去重
                    .collect(Collectors.toList());  // 转换为List<String>


            // 将area_country作为过滤条件
            if (!areaCountryIds.isEmpty()) {
                filters.put("supplier_country_id", areaCountryIds);  // 将area_country作为筛选条件放入filters
            }
        }

        // 如果提供了 customerCountryId，则合并并去重
        if (supplierCountryId != null && !supplierCountryId.isEmpty()) {
            // 合并并去重
            Set<String> mergedCountryIds = new HashSet<>(areaCountryIds);
            mergedCountryIds.addAll(supplierCountryId);

            // 将最终的 mergedCountryIds 作为过滤条件加入 filters
            filters.put("supplier_country_id", new ArrayList<>(mergedCountryIds));  // ArrayList<String> 类型
        } else {
            // 如果没有提供 customerCountryId，则直接使用 areaCountryIds 作为过滤条件
            filters.put("supplier_country_id", areaCountryIds);  // List<String> 类型
        }

        if (tradeType != null) {
            filters.put("trade_type", tradeType);
        }
        if (supplierLevel != null) {
            filters.put("supplier_level", supplierLevel);
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

        List<ReceiverSupplier> receiverList = new ArrayList<>();
        for (Hit<Supplier> CustomerHit : searchResponse.hits().hits()) {
            Supplier receiver = CustomerHit.source();
            if (receiver == null) {
                continue;
            }
            receiverList.add(new ReceiverSupplier(receiver.getSupplierId(), receiver.getSupplierName()));
        }
        String supplier_key = "supplier_list:" + IdUtil.fastUUID();
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(supplier_key, receiverList, 1, TimeUnit.DAYS);
        System.out.println(receiverList);
        belongUserIds.clear();
        return new SearchAllCustomerResponse(receiverList.size(), supplier_key);
    }
}
