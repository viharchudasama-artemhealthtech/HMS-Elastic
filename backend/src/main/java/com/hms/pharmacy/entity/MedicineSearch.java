package com.hms.pharmacy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "medicines", createIndex = false)
public class MedicineSearch {

	@Id
	private Long id;

	// Field use to define how each field is stored in the ElasticSearch
	@Field(type = FieldType.Text, analyzer = "autocomplete_index", searchAnalyzer = "autocomplete_search")
	private String name;

	@Field(type = FieldType.Keyword)
	private List<String> phoneticCodes;

	@Field(type = FieldType.Keyword)
	private String brand;

	@Field(type = FieldType.Integer)
	private Integer stock;

	@Field(type = FieldType.Keyword)
	private Boolean isActive;
}
