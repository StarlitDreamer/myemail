package com.java.email.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterAllReceiverResponse {
    public Integer total_items;
    public String  receiver_key;
}