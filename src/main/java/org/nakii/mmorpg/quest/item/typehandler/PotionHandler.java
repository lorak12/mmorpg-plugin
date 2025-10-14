package org.nakii.mmorpg.quest.item.typehandler;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.util.Utils;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass"})
public class PotionHandler implements ItemMetaHandler<PotionMeta> {

    public static final String EXTENDED = "extended";
    public static final String UPGRADED = "upgraded";

    // This reflection is kept for compatibility with servers that might have weird API states.
    @Nullable
    private static Method methodHasBasePotionType;
    @Nullable
    private static Method methodGetBasePotionType;
    private static boolean methodsInit;

    private PotionType type = PotionType.WATER;
    private Existence typeE = Existence.WHATEVER;
    private boolean extended;
    private Existence extendedE = Existence.WHATEVER;
    private boolean upgraded;
    private Existence upgradedE = Existence.WHATEVER;
    private List<CustomEffectHandler> custom = new ArrayList<>();
    private Existence customE = Existence.WHATEVER;
    private boolean exact = true;

    public PotionHandler() {
    }

    // This reflection part can be simplified or removed depending on minimum supported version,
    // but keeping it provides wider compatibility. No changes needed here.
    private static void initReflection() throws NoSuchMethodException {
        if (!methodsInit) {
            methodsInit = true;
            methodHasBasePotionType = PotionMeta.class.getDeclaredMethod("hasBasePotionType");
            methodGetBasePotionType = PotionMeta.class.getDeclaredMethod("getBasePotionType");
        }
    }

    @Nullable
    private static String addCustomEffects(final PotionMeta potionMeta, @Nullable final String effects) {
        final List<PotionEffect> customEffects = potionMeta.getCustomEffects();
        if (customEffects.isEmpty()) {
            return effects;
        }
        final StringBuilder string = new StringBuilder();
        for (final PotionEffect effect : customEffects) {
            final int power = effect.getAmplifier() + 1;
            final int duration = (effect.getDuration() - (effect.getDuration() % 20)) / 20;
            // <-- FIX: Replace deprecated getName() with modern getKey().toString()
            string.append(effect.getType().getKey().toString()).append(':').append(power).append(':').append(duration).append(',');
        }
        return (effects == null ? "" : effects) + " effects:" + string.substring(0, string.length() - 1);
    }

    @Override
    public Class<PotionMeta> metaClass() {
        return PotionMeta.class;
    }

    @Override
    public Set<String> keys() {
        return Set.of("type", EXTENDED, UPGRADED, "effects", "effects-containing");
    }

    @Override
    @Nullable
    public String serializeToString(final PotionMeta potionMeta) {
        // The original reflection-based method is actually a good way to handle this
        // for modern versions, as it correctly deconstructs the new PotionType enums.
        final String baseEffect = getBasePotionEffects(potionMeta);
        return addCustomEffects(potionMeta, baseEffect);
    }

    @Nullable
    private String getBasePotionEffects(final PotionMeta potionMeta) {
        // This logic is fine, it uses reflection to call getBasePotionType() which is the modern method.
        final Keyed type;
        try {
            initReflection();
            if (methodHasBasePotionType == null || methodGetBasePotionType == null) {
                return null;
            }
            if (!(boolean) methodHasBasePotionType.invoke(potionMeta)) {
                return null;
            }
            type = (Keyed) methodGetBasePotionType.invoke(potionMeta);
        } catch (final ReflectiveOperationException e) {
            MMORPGCore.getInstance().getQuestModule().getLoggerFactory().create(PotionHandler.class)
                    .error("Could not initialize Methods to get Potion Data!", e);
            return null;
        }

        final String minimalString = type.getKey().getKey(); // Use getKey().getKey() for just the value part
        final String longPrefix = "long_";
        final String strongPrefix = "strong_";
        final String effects;
        if (minimalString.startsWith(longPrefix)) {
            effects = minimalString.substring(longPrefix.length()) + " extended";
        } else if (minimalString.startsWith(strongPrefix)) {
            effects = minimalString.substring(strongPrefix.length()) + " upgraded";
        } else {
            effects = minimalString;
        }
        return "type:" + effects;
    }

    @Override
    public void set(final String key, final String data) throws QuestException {
        // No changes needed in this method's logic.
        switch (key) {
            case "type" -> setType(data);
            case EXTENDED -> {
                if (EXTENDED.equals(data)) {
                    extendedE = Existence.REQUIRED;
                    this.extended = true;
                } else {
                    extendedE = Existence.REQUIRED;
                    this.extended = Boolean.parseBoolean(data);
                }
            }
            case UPGRADED -> {
                if (UPGRADED.equals(data)) {
                    upgradedE = Existence.REQUIRED;
                    this.upgraded = true;
                } else {
                    upgradedE = Existence.REQUIRED;
                    this.upgraded = Boolean.parseBoolean(data);
                }
            }
            case "effects" -> setCustom(data);
            case "effects-containing" -> exact = false;
            default -> throw new QuestException("Unknown potion key: " + key);
        }
    }

    @Override
    public void populate(final PotionMeta potionMeta) {
        // <-- FIX: This entire method is modernized to remove PotionData.
        PotionType targetPotionType = determineFinalPotionType();
        potionMeta.setBasePotionType(targetPotionType);

        for (final PotionEffect effect : getCustom()) {
            potionMeta.addCustomEffect(effect, true);
        }
    }

    @Override
    public boolean check(final PotionMeta meta) {
        // <-- FIX: This now calls the modernized checkBase method.
        return checkBase(meta) && checkCustom(meta.getCustomEffects());
    }

    private void setType(final String type) throws QuestException {
        typeE = Existence.REQUIRED;
        try {
            this.type = PotionType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException e) {
            throw new QuestException("No such potion type: " + type, e);
        }
    }

    private List<PotionEffect> getCustom() {
        final List<PotionEffect> effects = new LinkedList<>();
        if (customE == Existence.FORBIDDEN) {
            return effects;
        }
        for (final CustomEffectHandler checker : custom) {
            if (checker.customTypeE != Existence.FORBIDDEN) {
                effects.add(new PotionEffect(checker.customType, checker.duration, checker.power));
            }
        }
        return effects;
    }

    private void setCustom(final String custom) throws QuestException {
        // No changes needed here.
        final String[] parts = HandlerUtil.getNNSplit(custom, "Potion is null!", ",");
        if (Existence.NONE_KEY.equalsIgnoreCase(parts[0])) {
            customE = Existence.FORBIDDEN;
            return;
        }
        this.custom = new ArrayList<>(parts.length);
        for (final String part : parts) {
            final CustomEffectHandler checker = new CustomEffectHandler(part);
            this.custom.add(checker);
        }
        customE = Existence.REQUIRED;
    }

    /**
     * Builds the final PotionType enum based on the handler's fields.
     * This logic is central to replacing PotionData.
     * @return The determined PotionType.
     */
    private PotionType determineFinalPotionType() {
        if (typeE != Existence.REQUIRED) {
            return PotionType.WATER; // Default if no type is specified.
        }
        try {
            String targetEnumName;
            if (upgraded) {
                targetEnumName = "STRONG_" + type.name();
            } else if (extended) {
                targetEnumName = "LONG_" + type.name();
            } else {
                targetEnumName = type.name();
            }
            return PotionType.valueOf(targetEnumName);
        } catch (IllegalArgumentException e) {
            // Fallback for invalid combinations (e.g., strong luck potion)
            return type;
        }
    }

    private boolean checkBase(final PotionMeta meta) {
        // <-- FIX: This entire method is rewritten to check against modern PotionType enums.
        if (typeE == Existence.WHATEVER) {
            return true;
        }

        PotionType actualType = meta.getBasePotionType();
        if (actualType == null) {
            // No base potion type on the item, so it can't match a REQUIRED check.
            return false;
        }

        // Deconstruct the actual potion type from the item
        String actualName = actualType.name();
        boolean actualIsExtended = actualName.startsWith("LONG_");
        boolean actualIsUpgraded = actualName.startsWith("STRONG_");
        String actualBaseName = actualName.replace("LONG_", "").replace("STRONG_", "");

        // Compare with the handler's required state
        if (!actualBaseName.equals(type.name())) {
            return false; // Base type doesn't match
        }
        if (extendedE == Existence.REQUIRED && actualIsExtended != this.extended) {
            return false; // Extended state doesn't match requirement
        }
        if (upgradedE == Existence.REQUIRED && actualIsUpgraded != this.upgraded) {
            return false; // Upgraded state doesn't match requirement
        }

        return true;
    }

    private boolean checkCustom(final List<PotionEffect> custom) {
        if (customE == Existence.WHATEVER) return true;
        if (custom.isEmpty()) return customE == Existence.FORBIDDEN;
        if (exact && custom.size() != this.custom.size()) return false;

        for (final CustomEffectHandler checker : this.custom) {
            boolean foundAndValid = false;
            for (final PotionEffect e : custom) {
                if (e.getType().equals(checker.customType)) {
                    if (checker.check(e)) {
                        foundAndValid = true;
                        break;
                    }
                }
            }
            if (!foundAndValid && checker.customTypeE == Existence.REQUIRED) return false;
        }
        return true;
    }

    private static class CustomEffectHandler {
        private static final int INSTRUCTION_FORMAT_LENGTH = 3;
        private final PotionEffectType customType;
        private final Existence customTypeE;
        private final Number durationE;
        private final int duration;
        private final int power;
        private final Number powerE;

        public CustomEffectHandler(final String custom) throws QuestException {
            final String[] parts = HandlerUtil.getNNSplit(custom, "Potion is null!", ":");
            if (parts[0].startsWith("none-")) {
                customTypeE = Existence.FORBIDDEN;
                customType = getType(parts[0].substring("none-".length()));
                powerE = Number.WHATEVER;
                power = 1;
                durationE = Number.WHATEVER;
                duration = 60 * 20;
                return;
            }
            customType = getType(parts[0]);
            customTypeE = Existence.REQUIRED;
            if (parts.length != INSTRUCTION_FORMAT_LENGTH) {
                throw new QuestException("Wrong effect format");
            }
            final Map.Entry<Number, Integer> effectPower = HandlerUtil.getNumberValue(parts[1], "effect power");
            powerE = effectPower.getKey();
            power = effectPower.getValue() - 1;
            if (power < 0) {
                throw new QuestException("Effect power must be a positive integer");
            }
            final Map.Entry<Number, Integer> effectDuration = HandlerUtil.getNumberValue(parts[2], "effect duration");
            durationE = effectDuration.getKey();
            duration = effectDuration.getValue() * 20;
        }

        private PotionEffectType getType(final String name) throws QuestException {
            // <-- FIX: Modernized this method to be future-proof and backward-compatible.
            String lowerName = name.toLowerCase(Locale.ROOT);
            NamespacedKey key = NamespacedKey.fromString(lowerName);

            PotionEffectType effectType = (key != null) ? Registry.POTION_EFFECT_TYPE.get(key) : null;

            // Fallback for old enum names like "SPEED" from legacy configs
            if (effectType == null) {
                effectType = PotionEffectType.getByName(name.toUpperCase(Locale.ROOT));
            }

            return Utils.getNN(effectType, "Unknown effect type: " + name);
        }

        private boolean check(@Nullable final PotionEffect effect) {
            // This logic is sound and uses the correct Number enum now.
            return switch (customTypeE) {
                case WHATEVER -> true;
                case REQUIRED -> effect != null && effect.getType().equals(customType)
                        && durationE.isValid(effect.getDuration(), duration)
                        && powerE.isValid(effect.getAmplifier(), power);
                case FORBIDDEN -> effect == null;
            };
        }
    }
}