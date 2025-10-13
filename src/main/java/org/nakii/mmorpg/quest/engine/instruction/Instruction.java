package org.nakii.mmorpg.quest.engine.instruction;

// Placeholder for future imports, we will create these classes next
import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.identifier.Identifier;
import org.nakii.mmorpg.quest.engine.variable.Variable;
import org.nakii.mmorpg.quest.model.QuestPackage;

import java.util.List;
import java.util.Locale;

// A more simplified Instruction for now. We will expand it later.
public class Instruction implements InstructionParts {
    private final QuestPackage pack;
    private final String instructionString;
    private final String[] parts;
    private int index = 0;

    public Instruction(QuestPackage pack, String instructionString) throws QuestException {
        this.pack = pack;
        this.instructionString = instructionString;
        this.parts = instructionString.split(" "); // Simple split for now TODO: Use tokenizer to handle quotes
    }

    public QuestPackage getPackage() {
        return pack;
    }

    public String getValue(String prefix) {
        for (String part : parts) {
            if (part.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT) + ":")) {
                return part.substring(prefix.length() + 1);
            }
        }
        return null;
    }

    public boolean hasArgument(String argument) {
        for (String part : parts) {
            if (part.equalsIgnoreCase(argument)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String next() throws QuestException {
        if (!hasNext()) throw new QuestException("Not enough arguments in instruction: " + instructionString);
        return parts[++index];
    }

    @Override
    public String current() {
        return parts[index];
    }

    @Override
    public boolean hasNext() {
        return index < parts.length - 1;
    }

    @Override
    public int size() {
        return parts.length;
    }

    @Override
    public String getPart(int i) throws QuestException {
        if (i < 0 || i >= parts.length) throw new QuestException("Index out of bounds");
        return parts[i];
    }

    @Override
    public List<String> getParts() {
        return List.of(parts);
    }

    @Override
    public String toString() {
        return instructionString;
    }
}