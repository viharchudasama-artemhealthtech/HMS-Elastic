package com.hms.pharmacy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "medicines", createIndex = false)
public class MedicineSearch {

	@Id
	private Long id;

	@MultiField(
			mainField = @Field(type = FieldType.Text, analyzer = "standard"),
			otherFields = {
					@InnerField(suffix = "keyword", type = FieldType.Keyword),
					@InnerField(suffix = "auto", type = FieldType.Text, analyzer = "autocomplete_index", searchAnalyzer = "autocomplete_search"),
					@InnerField(suffix = "phonetic", type = FieldType.Text, analyzer = "phonetic_analyzer")
			}
	)
	private String name;

	@Field(type = FieldType.Keyword)
	private String brand;

	@Field(type = FieldType.Integer)
	private Integer stock;

	@Field(type = FieldType.Boolean)
	private Boolean isActive;
}
