package com.hms.billing.mapper;

import com.hms.appointment.entity.Appointment;
import com.hms.billing.dto.request.BillingItemRequestDTO;
import com.hms.billing.dto.request.BillingRequestDTO;
import com.hms.billing.dto.response.BillingItemResponseDTO;
import com.hms.billing.dto.response.BillingResponseDTO;
import com.hms.billing.entity.Billing;
import com.hms.billing.entity.BillingItem;
import com.hms.patient.entity.Patient;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-15T17:27:18+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class BillingMapperImpl implements BillingMapper {

    @Override
    public Billing toEntity(BillingRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Billing billing = new Billing();

        billing.setTotalAmount( dto.getTotalAmount() );
        billing.setTaxAmount( dto.getTaxAmount() );
        billing.setDiscountAmount( dto.getDiscountAmount() );
        billing.setNetAmount( dto.getNetAmount() );
        billing.setPaymentStatus( dto.getPaymentStatus() );
        billing.setPaymentMethod( dto.getPaymentMethod() );
        billing.setBillingDate( dto.getBillingDate() );
        billing.setDueDate( dto.getDueDate() );
        billing.setNotes( dto.getNotes() );
        billing.setInsuranceProvider( dto.getInsuranceProvider() );
        billing.setInsuranceClaimNumber( dto.getInsuranceClaimNumber() );
        billing.setInsuranceAmount( dto.getInsuranceAmount() );
        billing.setInsuranceStatus( dto.getInsuranceStatus() );
        billing.setItems( billingItemRequestDTOListToBillingItemList( dto.getItems() ) );

        return billing;
    }

    @Override
    public BillingResponseDTO toDto(Billing entity) {
        if ( entity == null ) {
            return null;
        }

        BillingResponseDTO billingResponseDTO = new BillingResponseDTO();

        billingResponseDTO.setPatientId( entityPatientId( entity ) );
        billingResponseDTO.setPatientName( entityPatientName( entity ) );
        billingResponseDTO.setAppointmentId( entityAppointmentId( entity ) );
        billingResponseDTO.setId( entity.getId() );
        billingResponseDTO.setInvoiceNumber( entity.getInvoiceNumber() );
        billingResponseDTO.setTotalAmount( entity.getTotalAmount() );
        billingResponseDTO.setTaxAmount( entity.getTaxAmount() );
        billingResponseDTO.setDiscountAmount( entity.getDiscountAmount() );
        billingResponseDTO.setNetAmount( entity.getNetAmount() );
        billingResponseDTO.setPaymentStatus( entity.getPaymentStatus() );
        billingResponseDTO.setPaymentMethod( entity.getPaymentMethod() );
        billingResponseDTO.setBillingDate( entity.getBillingDate() );
        billingResponseDTO.setDueDate( entity.getDueDate() );
        billingResponseDTO.setNotes( entity.getNotes() );
        billingResponseDTO.setItems( billingItemListToBillingItemResponseDTOList( entity.getItems() ) );
        billingResponseDTO.setCreatedAt( entity.getCreatedAt() );

        return billingResponseDTO;
    }

    @Override
    public List<BillingResponseDTO> toDtoList(List<Billing> entities) {
        if ( entities == null ) {
            return null;
        }

        List<BillingResponseDTO> list = new ArrayList<BillingResponseDTO>( entities.size() );
        for ( Billing billing : entities ) {
            list.add( toDto( billing ) );
        }

        return list;
    }

    @Override
    public BillingItemResponseDTO toItemDto(BillingItem entity) {
        if ( entity == null ) {
            return null;
        }

        BillingItemResponseDTO billingItemResponseDTO = new BillingItemResponseDTO();

        billingItemResponseDTO.setId( entity.getId() );
        billingItemResponseDTO.setItemName( entity.getItemName() );
        billingItemResponseDTO.setQuantity( entity.getQuantity() );
        billingItemResponseDTO.setUnitPrice( entity.getUnitPrice() );
        billingItemResponseDTO.setTotalValue( entity.getTotalValue() );

        return billingItemResponseDTO;
    }

    @Override
    public BillingItem toItemEntity(BillingItemRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BillingItem billingItem = new BillingItem();

        billingItem.setItemName( dto.getItemName() );
        billingItem.setQuantity( dto.getQuantity() );
        billingItem.setUnitPrice( dto.getUnitPrice() );
        billingItem.setTotalValue( dto.getTotalValue() );

        return billingItem;
    }

    protected List<BillingItem> billingItemRequestDTOListToBillingItemList(List<BillingItemRequestDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<BillingItem> list1 = new ArrayList<BillingItem>( list.size() );
        for ( BillingItemRequestDTO billingItemRequestDTO : list ) {
            list1.add( toItemEntity( billingItemRequestDTO ) );
        }

        return list1;
    }

    private Long entityPatientId(Billing billing) {
        if ( billing == null ) {
            return null;
        }
        Patient patient = billing.getPatient();
        if ( patient == null ) {
            return null;
        }
        Long id = patient.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityPatientName(Billing billing) {
        if ( billing == null ) {
            return null;
        }
        Patient patient = billing.getPatient();
        if ( patient == null ) {
            return null;
        }
        String name = patient.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long entityAppointmentId(Billing billing) {
        if ( billing == null ) {
            return null;
        }
        Appointment appointment = billing.getAppointment();
        if ( appointment == null ) {
            return null;
        }
        Long id = appointment.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<BillingItemResponseDTO> billingItemListToBillingItemResponseDTOList(List<BillingItem> list) {
        if ( list == null ) {
            return null;
        }

        List<BillingItemResponseDTO> list1 = new ArrayList<BillingItemResponseDTO>( list.size() );
        for ( BillingItem billingItem : list ) {
            list1.add( toItemDto( billingItem ) );
        }

        return list1;
    }
}
