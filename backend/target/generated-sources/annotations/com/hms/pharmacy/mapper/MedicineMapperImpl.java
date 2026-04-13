package com.hms.pharmacy.mapper;

import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.entity.Medicine;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/*
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-12T21:53:38+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
*/
@Component
public class MedicineMapperImpl implements MedicineMapper {

    @Override
    public Medicine toEntity(MedicineRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Medicine medicine = new Medicine();

        medicine.setCategory( dto.getCategory() );
        medicine.setDescription( dto.getDescription() );
        medicine.setDosage( dto.getDosage() );
        medicine.setExpiryDate( dto.getExpiryDate() );
        medicine.setIsActive( dto.getIsActive() );
        medicine.setManufacturer( dto.getManufacturer() );
        medicine.setMedicineCode( dto.getMedicineCode() );
        medicine.setName( dto.getName() );
        medicine.setQuantityInStock( dto.getQuantityInStock() );
        medicine.setReorderLevel( dto.getReorderLevel() );
        medicine.setUnitPrice( dto.getUnitPrice() );

        return medicine;
    }

    @Override
    public MedicineResponseDTO toDto(Medicine entity) {
        if ( entity == null ) {
            return null;
        }

        MedicineResponseDTO medicineResponseDTO = new MedicineResponseDTO();

        medicineResponseDTO.setCategory( entity.getCategory() );
        medicineResponseDTO.setCreatedAt( entity.getCreatedAt() );
        medicineResponseDTO.setDescription( entity.getDescription() );
        medicineResponseDTO.setDosage( entity.getDosage() );
        medicineResponseDTO.setExpiryDate( entity.getExpiryDate() );
        if ( entity.getId() != null ) {
            medicineResponseDTO.setId( String.valueOf( entity.getId() ) );
        }
        medicineResponseDTO.setIsActive( entity.getIsActive() );
        medicineResponseDTO.setManufacturer( entity.getManufacturer() );
        medicineResponseDTO.setMedicineCode( entity.getMedicineCode() );
        medicineResponseDTO.setName( entity.getName() );
        medicineResponseDTO.setQuantityInStock( entity.getQuantityInStock() );
        medicineResponseDTO.setReorderLevel( entity.getReorderLevel() );
        medicineResponseDTO.setUnitPrice( entity.getUnitPrice() );
        medicineResponseDTO.setUpdatedAt( entity.getUpdatedAt() );

        return medicineResponseDTO;
    }

    @Override
    public void updateEntityFromDto(MedicineRequestDTO dto, Medicine entity) {
        if ( dto == null ) {
            return;
        }

        entity.setCategory( dto.getCategory() );
        entity.setDescription( dto.getDescription() );
        entity.setDosage( dto.getDosage() );
        entity.setExpiryDate( dto.getExpiryDate() );
        entity.setIsActive( dto.getIsActive() );
        entity.setManufacturer( dto.getManufacturer() );
        entity.setMedicineCode( dto.getMedicineCode() );
        entity.setName( dto.getName() );
        entity.setQuantityInStock( dto.getQuantityInStock() );
        entity.setReorderLevel( dto.getReorderLevel() );
        entity.setUnitPrice( dto.getUnitPrice() );
    }

    @Override
    public List<MedicineResponseDTO> toDtoList(List<Medicine> entities) {
        if ( entities == null ) {
            return null;
        }

        List<MedicineResponseDTO> list = new ArrayList<MedicineResponseDTO>( entities.size() );
        for ( Medicine medicine : entities ) {
            list.add( toDto( medicine ) );
        }

        return list;
    }
}
