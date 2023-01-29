package io.agora.metachat.game.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class UnityMessage implements Serializable {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return "UnityMessage{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
