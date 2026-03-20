package com.vutran.expensetracker.modules.category.dto;

import lombok.Data;
import lombok.Builder;
import java.util.UUID;
@Data 
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
    private String type;
}