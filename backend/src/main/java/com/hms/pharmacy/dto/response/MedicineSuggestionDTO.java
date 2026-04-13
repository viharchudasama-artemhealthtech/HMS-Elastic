package com.hms.pharmacy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineSuggestionDTO {

    private Long id;
    private String name;
    private String brand;
    private Integer stock;
    private boolean inStock;
}
