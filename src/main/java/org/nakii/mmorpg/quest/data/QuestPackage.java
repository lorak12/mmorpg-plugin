package org.nakii.mmorpg.quest.data;

// This class will eventually hold all the parsed data for a single quest package (folder).
// For now, it's a placeholder to establish the structure.
public class QuestPackage {
    private final String packageName;

    public QuestPackage(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }
}