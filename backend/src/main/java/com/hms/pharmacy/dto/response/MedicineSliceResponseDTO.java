package com.hms.pharmacy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineSliceResponseDTO {

    private List<MedicineResponseDTO> content;
    private int page;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
    private long total;
    private long inStockCount;
    private long outOfStockCount;
}
