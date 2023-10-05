package com.RUStore;

public enum Protocol {
    LIST(0),
    PUT(1),
    GET(3);
    private final int methodNumber;

    Protocol(int num) {
        this.methodNumber = num;
    }

    public int methodNumber() {
        return methodNumber;
    }
}
