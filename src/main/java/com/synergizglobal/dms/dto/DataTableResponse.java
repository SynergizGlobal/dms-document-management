package com.synergizglobal.dms.dto;

import java.util.List;

import lombok.Data;

@Data
public class DataTableResponse<T> {
    private int draw;
    private long recordsTotal;
    private long recordsFiltered;
    private List<T> data;
}