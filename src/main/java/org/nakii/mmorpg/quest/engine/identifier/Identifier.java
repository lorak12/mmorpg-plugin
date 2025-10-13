package org.nakii.mmorpg.quest.engine.identifier;

import org.nakii.mmorpg.quest.model.QuestPackage;
import java.util.Objects;

public abstract class Identifier {
    protected final QuestPackage pack;
    protected final String identifier;

    protected Identifier(QuestPackage pack, String identifier) {
        this.pack = pack;
        this.identifier = identifier;
    }

    public QuestPackage getPackage() {
        return pack;
    }

    public String get() {
        return identifier;
    }

    public String getFull() {
        return pack.getName() + "." + get();
    }

    @Override
    public String toString() {
        return getFull();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return Objects.equals(pack.getName(), that.pack.getName()) && Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pack.getName(), identifier);
    }
}