package com.java.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchAllCustomersDto {
    public String commodity_name;
    public List<String> area_id;
    public List<String>  customer_country_id;
    public Integer  trade_type;
    public Integer  customer_level;
}
