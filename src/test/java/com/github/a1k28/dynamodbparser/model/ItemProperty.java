package com.github.a1k28.dynamodbparser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class ItemProperty {
    @JsonProperty("NAME")
    private String name;

    @JsonProperty("DESCRIPTION")
    private String description;

//    @JsonProperty("TYPE")
//    private PropertyType type;

    @JsonProperty("XPATHS")
    private List<String> xpaths;

    @JsonProperty("TRY_COUNT")
    private Integer tryCount;

    @JsonProperty("ACTIVE")
    private Boolean active;
}
