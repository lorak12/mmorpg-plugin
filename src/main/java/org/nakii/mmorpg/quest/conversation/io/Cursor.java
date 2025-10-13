package org.nakii.mmorpg.quest.conversation.io;

public class Cursor {

    private final int min;
    private final int max;
    private int current;

    public Cursor(int min, int max, int current) {
        this.min = min;
        this.max = max;
        this.current = Math.max(min, Math.min(max, current));
    }

    public int get() {
        return current;
    }

    public void set(int current) {
        this.current = Math.min(max, Math.max(min, current));
    }

    public void modify(int modification) {
        set(current + modification);
    }

    public boolean isValid(int value) {
        return value >= min && value <= max;
    }
}