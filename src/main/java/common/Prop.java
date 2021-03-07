package common;

import java.util.NoSuchElementException;

public enum Prop {
    THREADS("threads"),
    CYCLES("cycles"),
    PRINT("print"),
    PAYLOAD("payload");
    String value;

    Prop(String value) {
        this.value = value;
    }

    public static Prop fromValue(String value) {
        for (Prop prop : Prop.values()) {
            if (prop.value.equals(value)) {
                return prop;
            }
        }
        throw new NoSuchElementException("");
    }
}
