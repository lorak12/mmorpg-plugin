package org.nakii.mmorpg.quest.id;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.identifier.SectionIdentifier;
import org.nakii.mmorpg.quest.api.instruction.argument.PackageArgument;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a quest compass ID.
 */
public class CompassID extends SectionIdentifier {

    /**
     * Creates new QuestCompassID instance.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the package where the identifier was used in
     * @param identifier  the identifier of the quest compass
     * @throws QuestException if the instruction could not be created or
     *                        when the quest compass could not be resolved with the given identifier
     */
    public CompassID(final QuestPackageManager packManager, @Nullable final QuestPackage pack, final String identifier) throws QuestException {
        super(packManager, pack, identifier, "compass", "Compass");
    }

    /**
     * Get the full path of the tag to indicate a quest compass should be shown.
     *
     * @return the compass tag
     */
    public String getTag() {
        return PackageArgument.IDENTIFIER.apply(getPackage(), "compass-" + get());
    }
}
