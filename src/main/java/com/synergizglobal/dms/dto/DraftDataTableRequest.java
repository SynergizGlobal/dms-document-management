package com.synergizglobal.dms.dto;

import lombok.Data;

@Data
public class DraftDataTableRequest {
    private int draw;
    private int start;
    private int length;
}
