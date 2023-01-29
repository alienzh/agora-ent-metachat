package io.agora.metachat.game.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class EnterSceneExtraInfo implements Serializable {
    private int sceneIndex;

    public int getSceneIndex() {
        return sceneIndex;
    }

    public void setSceneIndex(int sceneIndex) {
        this.sceneIndex = sceneIndex;
    }

    @NonNull
    @Override
    public String toString() {
        return "EnterSceneExtraInfo{" +
                "sceneIndex=" + sceneIndex +
                '}';
    }
}
