package com.vutran.expensetracker.modules.category.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CategoryRequest {
    private String name;
    private String type;
    private UUID userId; // ID of the owner of this category
}