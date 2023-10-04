package com.example.model;

import com.example.services.configuration.AppConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SketchData {
    private final int chainIntId;
    private final long[] sketch;

    private String jsonSketch;

    public SketchData(int chainIntId, long[] sketch) {
        this.chainIntId = chainIntId;
        this.sketch = sketch;
    }
    public int getChainIntId() {
        return chainIntId;
    }

    private String jsonKeyBasedOnSketchLength() {
        //todo why is configuration accessed so deep down in application code?
        int sketchLength = AppConfig.SKETCH_LEARNING_SKETCH_LENGTH;
        return switch (sketchLength) {
            case 192 -> "sk192_long";
            case 1024 -> "sk1024_long";
            default -> throw new Error("Unknown sketch length");
        };
    }

    private String createJsonString(String key, long[] vals) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode arrayNode = mapper.createArrayNode();

        for (long val : vals) {
            arrayNode.add(val);
        }

        rootNode.set(key, arrayNode);

        return mapper.writeValueAsString(rootNode);
    }

    public void convertSketch() {
        try {
            this.jsonSketch = createJsonString(jsonKeyBasedOnSketchLength(), sketch);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getJsonSketch() {
        if (jsonSketch == null) {
            throw new RuntimeException("sketches must be converted first");
        }
        return jsonSketch;
    }
}
