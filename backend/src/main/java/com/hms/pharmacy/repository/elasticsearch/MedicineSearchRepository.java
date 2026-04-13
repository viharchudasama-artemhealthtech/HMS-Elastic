package com.hms.pharmacy.repository.elasticsearch;

import com.hms.pharmacy.entity.MedicineSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineSearchRepository extends ElasticsearchRepository<MedicineSearch, Long> {

    List<MedicineSearch> findByIsActiveTrue();
}
