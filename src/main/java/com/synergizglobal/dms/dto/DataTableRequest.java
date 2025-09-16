package com.synergizglobal.dms.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DataTableRequest {
    private int draw;
    private int start;
    private int length;
    private Map<Integer, List<String>> columnFilters;
    // You can also include `order`, `columns` if needed
}
