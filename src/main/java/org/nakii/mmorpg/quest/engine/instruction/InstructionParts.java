package org.nakii.mmorpg.quest.engine.instruction;

import org.nakii.mmorpg.quest.engine.QuestException;
import java.util.List;

public interface InstructionParts {
    String next() throws QuestException;
    String current();
    boolean hasNext();
    int size();
    String getPart(int index) throws QuestException;
    List<String> getParts();
    default List<String> getValueParts() {
        return getParts().subList(1, size());
    }
}