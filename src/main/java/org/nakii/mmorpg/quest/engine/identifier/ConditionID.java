package org.nakii.mmorpg.quest.engine.identifier;

import org.nakii.mmorpg.quest.model.QuestPackage;

public class ConditionID extends Identifier {
    private final boolean inverted;

    public ConditionID(QuestPackage pack, String identifier) {
        super(pack, identifier.startsWith("!") ? identifier.substring(1) : identifier);
        this.inverted = identifier.startsWith("!");
    }

    public boolean isInverted() {
        return inverted;
    }
}