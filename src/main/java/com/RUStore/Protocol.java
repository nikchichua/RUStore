package com.RUStore;

public enum Protocol {
    LIST(0),
    GET(1),
    REMOVE(2),
    PUT(3);

    private final int methodNumber;

    Protocol(int num) {
        this.methodNumber = num;
    }

    public int methodNumber() {
        return methodNumber;
    }

    public boolean equals(int methodNumber) {
        return this.methodNumber() == methodNumber;
    }
}