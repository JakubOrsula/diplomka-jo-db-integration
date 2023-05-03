package com.example.model;

public class SimpleProtein {
    private final int intId;
    private final String gesamtId;

    public SimpleProtein(int intId, String gesamtId) {
        this.intId = intId;
        this.gesamtId = gesamtId;
        System.out.println(gesamtId);
    }

    public int getIntId() {
        return intId;
    }

    public String getGesamtId() {
        return gesamtId;
    }
}

