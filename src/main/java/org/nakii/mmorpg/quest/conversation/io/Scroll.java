package org.nakii.mmorpg.quest.conversation.io;

public enum Scroll {
    UP(-1),
    NONE(0),
    DOWN(1);

    private final int modification;

    Scroll(int modification) {
        this.modification = modification;
    }

    public int getModification() {
        return modification;
    }
}