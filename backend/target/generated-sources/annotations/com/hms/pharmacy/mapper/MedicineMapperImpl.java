package com.hms.pharmacy.mapper;

import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.entity.Medicine;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-27T10:59:19+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class MedicineMapperImpl implements MedicineMapper {

    @Override
    public Medicine toEntity(MedicineRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Medicine medicine = new Medicine();

        medicine.setName( dto.getName() );
        medicine.setMedicineCode( dto.getMedicineCode() );
        medicine.setDescription( dto.getDescription() );
        medicine.setCategory( dto.getCategory() );
        medicine.setManufacturer( dto.getManufacturer() );
        medicine.setExpiryDate( dto.getExpiryDate() );
        medicine.setQuantityInStock( dto.getQuantityInStock() );
        medicine.setUnitPrice( dto.getUnitPrice() );
        medicine.setReorderLevel( dto.getReorderLevel() );
        medicine.setDosage( dto.getDosage() );
        medicine.setIsActive( dto.getIsActive() );

        return medicine;
    }

    @Override
    public MedicineResponseDTO toDto(Medicine entity) {
        if ( entity == null ) {
            return null;
        }

        MedicineResponseDTO medicineResponseDTO = new MedicineResponseDTO();

        if ( entity.getId() != null ) {
            medicineResponseDTO.setId( String.valueOf( entity.getId() ) );
        }
        medicineResponseDTO.setName( entity.getName() );
        medicineResponseDTO.setMedicineCode( entity.getMedicineCode() );
        medicineResponseDTO.setDescription( entity.getDescription() );
        medicineResponseDTO.setCategory( entity.getCategory() );
        medicineResponseDTO.setManufacturer( entity.getManufacturer() );
        medicineResponseDTO.setExpiryDate( entity.getExpiryDate() );
        medicineResponseDTO.setQuantityInStock( entity.getQuantityInStock() );
        medicineResponseDTO.setUnitPrice( entity.getUnitPrice() );
        medicineResponseDTO.setReorderLevel( entity.getReorderLevel() );
        medicineResponseDTO.setDosage( entity.getDosage() );
        medicineResponseDTO.setIsActive( entity.getIsActive() );
        medicineResponseDTO.setCreatedAt( entity.getCreatedAt() );
        medicineResponseDTO.setUpdatedAt( entity.getUpdatedAt() );

        return medicineResponseDTO;
    }

    @Override
    public void updateEntityFromDto(MedicineRequestDTO dto, Medicine entity) {
        if ( dto == null ) {
            return;
        }

        entity.setName( dto.getName() );
        entity.setMedicineCode( dto.getMedicineCode() );
        entity.setDescription( dto.getDescription() );
        entity.setCategory( dto.getCategory() );
        entity.setManufacturer( dto.getManufacturer() );
        entity.setExpiryDate( dto.getExpiryDate() );
        entity.setQuantityInStock( dto.getQuantityInStock() );
        entity.setUnitPrice( dto.getUnitPrice() );
        entity.setReorderLevel( dto.getReorderLevel() );
        entity.setDosage( dto.getDosage() );
        entity.setIsActive( dto.getIsActive() );
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
