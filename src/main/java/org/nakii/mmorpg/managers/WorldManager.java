package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.world.CustomWorld;
import org.nakii.mmorpg.world.WorldFlags;
import org.nakii.mmorpg.zone.Zone;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.nakii.mmorpg.zone.BlockNode;
import org.nakii.mmorpg.zone.ZoneBounds;
import org.nakii.mmorpg.zone.ZoneLoader;

/**
 * Manages all custom worlds ("islands") for the server.
 * This class is responsible for loading world configurations and providing a central
 * point for looking up zones based on location.
 */
public class WorldManager {

    private final MMORPGCore plugin;
    private final Map<String, CustomWorld> loadedWorlds = new HashMap<>();
    private final ZoneLoader zoneParser; // The refactored ZoneManager is now a parser utility

    public WorldManager(MMORPGCore plugin) {
        this.plugin = plugin;
        this.zoneParser = new ZoneLoader(plugin); // We reuse the old ZoneManager for its parsing logic
    }

    /**
     * Clears existing worlds and loads all world configurations from the /worlds/ directory.
     * This should be called on plugin startup and reload.
     */
    public void loadWorlds() {
        loadedWorlds.clear();
        plugin.getLogger().info("--- [MMORPGCore] Starting Custom World Loading ---");
        File worldsConfigDir = new File(plugin.getDataFolder(), "worlds");
        if (!worldsConfigDir.exists() || !worldsConfigDir.isDirectory()) {
            plugin.getLogger().warning("'worlds' directory not found. Please create it. No worlds will be loaded.");
            return;
        }

        File[] worldConfigFiles = worldsConfigDir.listFiles(File::isDirectory);
        if (worldConfigFiles == null || worldConfigFiles.length == 0) {
            plugin.getLogger().info("No world configuration folders found in '" + worldsConfigDir.getPath() + "'.");
            return;
        }

        plugin.getLogger().info("Found " + worldConfigFiles.length + " potential world configuration folder(s).");

        for (File worldConfigDir : worldConfigFiles) {
            plugin.getLogger().info("Processing config directory: " + worldConfigDir.getName());
            File worldConfigFile = new File(worldConfigDir, "world.yml");
            if (!worldConfigFile.exists()) {
                plugin.getLogger().warning("  └ SKIPPING: Missing a world.yml file.");
                continue;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(worldConfigFile);
            String worldName = config.getString("world-name");

            if (worldName == null || worldName.isEmpty()) {
                plugin.getLogger().severe("  └ FAILED: 'world-name' key is missing or empty in " + worldConfigFile.getPath());
                continue;
            }
            plugin.getLogger().info("  ├ Found 'world-name': \"" + worldName + "\"");

            World bukkitWorld = Bukkit.getWorld(worldName);

            if (bukkitWorld == null) {
                plugin.getLogger().severe("  └ FAILED: Bukkit.getWorld(\"" + worldName + "\") returned null. The world is not loaded by the server when the plugin enabled.");
                plugin.getLogger().severe("    (Check that the name is spelled EXACTLY right and that Multiverse has loaded it.)");
                continue;
            }
            plugin.getLogger().info("  ├ Successfully found loaded Bukkit world: " + bukkitWorld.getName());

            WorldFlags flags = parseWorldFlags(config);
            String displayName = config.getString("display-name", "A Custom World");

            File zonesConfigFile = new File(worldConfigDir, "zones.yml");
            List<Zone> zones = zoneParser.loadZonesFromFile(zonesConfigFile, bukkitWorld);
            plugin.getLogger().info("  ├ Loaded " + zones.size() + " zones from zones.yml.");

            Location spawnPoint = parseLocation(bukkitWorld, config.getString("spawn-point"));


            CustomWorld customWorld = new CustomWorld(bukkitWorld, displayName, flags, zones, spawnPoint);
            loadedWorlds.put(worldName.toLowerCase(), customWorld);
            plugin.getLogger().info("  └ SUCCESS: Successfully created and loaded CustomWorld '" + worldName + "'.");
        }

        plugin.getLogger().info("--- [MMORPGCore] Custom World Loading Finished. Total loaded: " + loadedWorlds.size() + " ---");
    }

    /**
     * The new central method for finding which zone a location is in.
     * It first finds the CustomWorld and then asks it to find the zone.
     *
     * @param location The location to check.
     * @return The specific Zone, or null if not in any defined zone.
     */
    @Nullable
    public Zone getZoneForLocation(Location location) {
        World bukkitWorld = location.getWorld();
        if (bukkitWorld == null) return null;

        CustomWorld customWorld = loadedWorlds.get(bukkitWorld.getName().toLowerCase());
        if (customWorld != null) {
            return customWorld.getZoneForLocation(location);
        }

        return null; // In a Bukkit world that is not a managed CustomWorld
    }

    /**
     * Retrieves a CustomWorld by its name.
     * @param worldName The name of the world.
     * @return The CustomWorld object, or null if not found.
     */
    @Nullable
    public CustomWorld getCustomWorld(String worldName) {
        return loadedWorlds.get(worldName.toLowerCase());
    }

    // Helper method to parse world.yml
    private WorldFlags parseWorldFlags(YamlConfiguration config) {
        try {
            boolean canPlace = config.getBoolean("flags.block-place", false);
            boolean canBreak = config.getBoolean("flags.block-break", false);
            String pvp = config.getString("flags.pvp", "false");
            boolean peaceful = config.getBoolean("flags.force-peaceful", false);
            List<String> entryReqs = config.getStringList("flags.entry-requirements");
            return new WorldFlags(canPlace, canBreak, pvp, peaceful, entryReqs);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to parse flags in world.yml for " + config.getString("world-name"), e);
            return WorldFlags.EMPTY;
        }
    }

    public void populateZone(CommandSender sender, String worldName, String zoneId, Material maskMaterial, String primaryArg, String defaultNodeId) {
        CustomWorld world = getCustomWorld(worldName);
        if (world == null) {
            sender.sendMessage(Component.text("World '" + worldName + "' not found.", NamedTextColor.RED));
            return;
        }

        Zone zone = world.getZones().stream().filter(z -> z.getId().equalsIgnoreCase(zoneId)).findFirst().orElse(null);
        if (zone == null) {
            sender.sendMessage(Component.text("Zone '" + zoneId + "' not found in world '" + worldName + "'.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Starting population task for zone '" + zoneId + "'. This may take a while...", NamedTextColor.YELLOW));

        new BukkitRunnable() {
            @Override
            public void run() {
                // --- Step 1: Find all mask blocks (Async) ---
                List<Block> maskBlocks = new ArrayList<>();
                ZoneBounds bounds = zone.getBounds();
                for (int x = (int) bounds.getMinX(); x <= (int) bounds.getMaxX(); x++) {
                    for (int z = (int) bounds.getMinZ(); z <= (int) bounds.getMaxZ(); z++) {
                        // Quick check against the polygon before checking Y levels
                        if (!bounds.containsXZ(x, z)) continue;

                        for (int y = (int) bounds.getMinY(); y <= (int) bounds.getMaxY(); y++) {
                            Block block = world.getBukkitWorld().getBlockAt(x, y, z);
                            if (block.getType() == maskMaterial) {
                                maskBlocks.add(block);
                            }
                        }
                    }
                }

                if (maskBlocks.isEmpty()) {
                    sender.sendMessage(Component.text("Population task finished. No '" + maskMaterial.name() + "' blocks were found in the zone.", NamedTextColor.GOLD));
                    return;
                }

                // --- Step 2: Calculate counts and get node materials (Async) ---
                String[] primaryParts = primaryArg.split(":");
                String primaryNodeId = primaryParts[0];
                int primaryCount;
                if (primaryParts[1].contains("%")) {
                    double percentage = Double.parseDouble(primaryParts[1].replace("%", "")) / 100.0;
                    primaryCount = (int) (maskBlocks.size() * percentage);
                } else {
                    primaryCount = Integer.parseInt(primaryParts[1]);
                }

                final int finalPrimaryCount = Math.min(primaryCount, maskBlocks.size());

                // Get the materials for the nodes
                BlockNode primaryNode = findNode(zone, primaryNodeId);
                BlockNode defaultNode = findNode(zone, defaultNodeId);

                if (primaryNode == null || defaultNode == null) {
                    sender.sendMessage(Component.text("Error: Could not find node definitions for '" + (primaryNode == null ? primaryNodeId : defaultNodeId) + "'. Aborting.", NamedTextColor.RED));
                    return;
                }

                sender.sendMessage(Component.text("Found " + maskBlocks.size() + " mask blocks. Preparing to place " + finalPrimaryCount + " primary nodes.", NamedTextColor.GREEN));

                // --- Step 3: Shuffle and schedule the replacement (Sync) ---
                Collections.shuffle(maskBlocks);
                Iterator<Block> blockIterator = maskBlocks.iterator();

                new BukkitRunnable() {
                    private int primaryPlaced = 0;
                    @Override
                    public void run() {
                        int blocksThisTick = 0;
                        while (blockIterator.hasNext() && blocksThisTick < 1000) { // Process up to 1000 blocks per tick
                            Block blockToChange = blockIterator.next();
                            if (primaryPlaced < finalPrimaryCount) {
                                blockToChange.setType(primaryNode.material(), false); // 'false' prevents block physics updates
                                primaryPlaced++;
                            } else {
                                blockToChange.setType(defaultNode.material(), false);
                            }
                            blocksThisTick++;
                        }

                        if (!blockIterator.hasNext()) {
                            sender.sendMessage(Component.text("Population task complete! Successfully replaced all " + maskBlocks.size() + " blocks.", NamedTextColor.GOLD));
                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L); // Run every tick
            }
        }.runTaskAsynchronously(plugin);
    }

    @Nullable
    private Location parseLocation(World world, String locationString) {
        if (locationString == null || locationString.isEmpty()) return null;
        String[] parts = locationString.split(",");
        if (parts.length < 3) return null;
        try {
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            double z = Double.parseDouble(parts[2].trim());
            float yaw = (parts.length > 3) ? Float.parseFloat(parts[3].trim()) : 0.0f;
            float pitch = (parts.length > 4) ? Float.parseFloat(parts[4].trim()) : 0.0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid location format: '" + locationString + "'");
            return null;
        }
    }

    // Helper method to find a BlockNode definition within a zone's flags.
    private BlockNode findNode(Zone zone, String nodeId) {
        if (zone.getFlags().blockBreakingFlags() == null) return null;
        return zone.getFlags().blockBreakingFlags().definitions().get(nodeId);
    }

    public Collection<CustomWorld> getLoadedWorlds() {
        return loadedWorlds.values();
    }
}