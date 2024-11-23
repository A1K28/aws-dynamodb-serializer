package com.github.a1k28.dynamodbparser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class RequestItem {
    @JsonProperty("ID")
    private String id;

    @JsonProperty("WEB_URL")
    private String webUrl;

    @JsonProperty("HTML_DATA")
    private String htmlData;

    @JsonProperty("USER_ID")
    private String userId;

    @JsonProperty("TRY_COUNT")
    private Integer tryCount;

    @JsonProperty("STATUS")
    private Status status;

    @JsonProperty("INITIALIZED")
    private Boolean initialized;

    @JsonProperty("PAGE")
    private Integer page;

    @JsonProperty("N_PAGES")
    private Integer nPages;

    @JsonProperty("PROPERTIES")
    private List<ItemProperty> properties;

    // 2024-11-17T21:52:45.087714
    @JsonProperty("CREATED_AT")
    private String createdAt;

    @JsonProperty("LAST_UPDATED_AT")
    private String lastUpdatedAt;
}
