package org.nakii.mmorpg.quest.engine.api;

import org.nakii.mmorpg.quest.engine.instruction.Instruction;
import org.nakii.mmorpg.quest.engine.profile.Profile;

//TODO: This is a simplified base class for now. We will add more functionality later.
public abstract class Objective {
    protected final Instruction instruction;

    public Objective(Instruction instruction) {
        this.instruction = instruction;
    }

    public abstract void start(Profile profile);
    public abstract void stop(Profile profile);
}