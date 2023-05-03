package com.example.model.json;

// Converter.java

// To use this code, add the following Maven dependency to your project:
//
//
//     com.fasterxml.jackson.core     : jackson-databind          : 2.9.0
//     com.fasterxml.jackson.datatype : jackson-datatype-jsr310   : 2.9.0
//
// Import this package:
//
//     import io.quicktype.Converter;
//
// Then you can deserialize a JSON string with
//
//     DistsMetadata data = Converter.fromJsonString(jsonString);


import com.fasterxml.jackson.annotation.*;
import java.util.Map;

// DistsMetadata.java
public class DistsMetadata {
    private Map<String, Double> dists;

    @JsonProperty("dists")
    public Map<String, Double> getDists() { return dists; }
    @JsonProperty("dists")
    public void setDists(Map<String, Double> value) { this.dists = value; }
}

