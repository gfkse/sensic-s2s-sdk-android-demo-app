package com.gfk.s2s;

public enum Endpoint {
    DEMO(0), PREPROD(1);

    private int id;

    public int getId() {
        return id;
    }

    Endpoint(int id) {
        this.id = id;
    }

    public static Endpoint fromId(int id) {
        for (Endpoint type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
