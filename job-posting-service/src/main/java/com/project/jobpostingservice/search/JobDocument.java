package com.project.jobpostingservice.search;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "jobs", createIndex = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "english")
    private String title;

    @Field(type = FieldType.Text, analyzer = "english")
    private String description;

    @Field(type = FieldType.Keyword)
    private String company;

    @Field(type = FieldType.Keyword)
    private String location;

    @Field(type = FieldType.Keyword)
    private String jobType;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String experienceLevel;

    @Field(type = FieldType.Double)
    private BigDecimal salaryMin;

    @Field(type = FieldType.Double)
    private BigDecimal salaryMax;

    @Field(type = FieldType.Boolean)
    private Boolean remote;

    @Field(type = FieldType.Text, analyzer = "english")
    private String requirements;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime expiresAt;
}