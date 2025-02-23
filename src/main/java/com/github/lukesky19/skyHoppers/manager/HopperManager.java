/*
    SkyHoppers adds upgradable hoppers that can suction items, transfer items wirelessly to linked containers.
    Copyright (C) 2025  lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skyHoppers.manager;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.manager.SettingsManager;
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.config.record.Settings;
import com.github.lukesky19.skyHoppers.gui.OutputFilterGUI;
import com.github.lukesky19.skyHoppers.hopper.*;
import com.github.lukesky19.skyHoppers.util.PluginUtils;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import com.jeff_media.morepersistentdatatypes.DataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class HopperManager {
    private final SkyHoppers plugin;
    private final DataManager dataManager;
    private final SettingsManager settingsManager;
    private final LocaleManager localeManager;

    private final List<Location> hopperLocations = new ArrayList<>();
    private final Map<Location, SkyHopper> skyHopperCache = new HashMap<>();
    private final HashMap<Location, HashMap<UUID, ChestGUI>> guiData = new HashMap<>();

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin.
     * @param dataManager A DataManager instance.
     * @param settingsManager A SettingsManager instance.
     * @param localeManager A LocaleManager instance.
     */
    public HopperManager(SkyHoppers plugin, DataManager dataManager, SettingsManager settingsManager, LocaleManager localeManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
    }

    /**
     * Get a cached SkyHopper at a given location.
     * @param location The Location of the SkyHopper
     * @return The SkyHopper or null if there is no SkyHopper at that Location.
     */
    @Nullable
    public SkyHopper getSkyHopper(@NotNull Location location) {
        return skyHopperCache.get(location);
    }

    /**
     * Get all cached SkyHoppers.
     * @return A List of SkyHoppers.
     */
    public List<SkyHopper> getSkyHoppers() {
        return new ArrayList<>(skyHopperCache.values());
    }

    /**
     * Reloads all SkyHopper locations and caches all SkyHoppers in loaded chunks.
     */
    public void reload() {
        hopperLocations.clear();
        skyHopperCache.clear();

        // Migrates the old database to the new
        dataManager.migrateSkyHoppers();

        // Gets all SkyHopper locations from the database
        hopperLocations.clear();
        hopperLocations.addAll(dataManager.loadHoppers());

        // Load SkyHoppers in loaded chunks
        for(World world : plugin.getServer().getWorlds()) {
            for(Chunk chunk : world.getLoadedChunks()) {
                loadSkyHoppersInChunk(chunk);
            }
        }
    }

    /**
     * Loads all SkyHoppers in a chunk.
     * @param chunk The chunk to check for SkyHoppers to load.
     */
    public void loadSkyHoppersInChunk(@NotNull Chunk chunk) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        for(Location location : hopperLocations) {
            // Change location X and Z to chunk locations.
            int locX = location.getBlockX() >> 4;
            int locZ = location.getBlockZ() >> 4;

            // Check if SkyHopper Chunk Location matches Chunk location.
            if(locX == chunkX && locZ == chunkZ) {
                if(skyHopperCache.containsKey(location)) return; // SkyHopper is already loaded

                // Check if the block at the location is a hopper
                if (location.getBlock().getState(false) instanceof Hopper hopper) {
                    // Get the PersistentDataContainer
                    final PersistentDataContainer pdc = hopper.getPersistentDataContainer();

                    // Get the SkyHopper from the given Hopper
                    SkyHopper skyHopper = getSkyHopperFromPDC(location, pdc);

                    // Check if the SkyHopper is valid or remove the Location from the database.
                    if(skyHopper != null) {
                        // Save any updated SkyHopper data to the Hopper PDC
                        this.saveSkyHopperToBlockPDC(skyHopper, hopper);

                        cacheSkyHopper(location, skyHopper);
                    }
                }
            }
        }
    }

    /**
     * Loads a SkyHopper at a given Location.
     * This method has performance costs because it loads the chunk if it isn't already loaded.
     * @param location The Location of the SkyHopper.
     */
    public void loadSkyHopperAtLocation(@NotNull Location location) {
        Chunk chunk = location.getChunk();

        boolean currentState = false;
        boolean loadedState = false;
        if(chunk.isLoaded()) {
            currentState = true;
        } else {
            loadedState = chunk.load();
        }

        if(currentState || loadedState) {
            // Check if the block at the location is a hopper
            if (location.getBlock().getState(false) instanceof Hopper hopper) {
                // Get the PersistentDataContainer
                final PersistentDataContainer pdc = hopper.getPersistentDataContainer();

                // Get the SkyHopper from the given Hopper
                SkyHopper skyHopper = getSkyHopperFromPDC(location, pdc);

                // Check if the SkyHopper is valid or remove the Location from the database.
                if(skyHopper != null) {
                    // Save any updated SkyHopper data to the Hopper PDC
                    this.saveSkyHopperToPDC(skyHopper, hopper.getPersistentDataContainer());

                    hopper.update();

                    // Cache SkyHopper
                    cacheSkyHopper(location, skyHopper);
                }
            }

            // Unload the chunk now that we are done with it (if it was force loaded)
            if(loadedState) chunk.unload();
        }
    }

    /**
     * Loads all SkyHoppers.
     * @param force If true, will load all SkyHoppers, regardless if they are already cached. If false, will only load SkyHoppers not already cached.
     */
    public void loadSkyHoppers(boolean force) {
        plugin.pauseSkyHoppers();

        closeOpenGuis(false);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if(!force) {
                for (Location location : hopperLocations) {
                    if (!skyHopperCache.containsKey(location)) {
                        loadSkyHopperAtLocation(location);
                    }
                }
            } else {
                skyHopperCache.clear();

                for (Location location : hopperLocations) {
                    loadSkyHopperAtLocation(location);
                }
            }

            plugin.unPauseSkyHoppers();
        }, 1L);
    }

    /**
     * Caches a SkyHopper.
     * @param location The Location of the SkyHopper.
     * @param skyHopper The SkyHopper.
     */
    public void cacheSkyHopper(@NotNull Location location, @NotNull SkyHopper skyHopper) {
        dataManager.saveLocationIfDoesNotExit(location);

        skyHopperCache.put(location, skyHopper);
    }

    /**
     * Removes a SkyHopper from the cache, database, and closes any open GUIs for the SkyHopper.
     * @param location The Location of the SkyHopper.
     */
    public void removeSkyHopper(@NotNull Location location) {
        dataManager.removeHopper(location);

        skyHopperCache.remove(location);

        HashMap<UUID, ChestGUI> uuidGuiMap = guiData.get(location);
        if(uuidGuiMap == null) return;

        if(uuidGuiMap.isEmpty()) {
            guiData.remove(location);
            return;
        }

        for(Map.Entry<UUID, ChestGUI> entry : uuidGuiMap.entrySet()) {
            UUID uuid = entry.getKey();
            ChestGUI gui = entry.getValue();

            Player player = plugin.getServer().getPlayer(uuid);
            if(player != null && player.isOnline() && player.isConnected()) {
                gui.unload(plugin, player, false);
            }
        }
    }

    /**
     * Checks if an ItemStack is a SkyHopper.
     * @param itemStack The ItemStack to check.
     * @return true if a SkyHopper, false if not.
     */
    public boolean isItemStackSkyHopper(@NotNull ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer()
                .get(HopperKeys.ENABLED.getKey(), PersistentDataType.INTEGER) != null;
    }

    /**
     * Creates an ItemStack for a SkyHopper.
     * @param skyHopper The SkyHopper.
     * @param amount The amount to set the ItemStack amount to.
     * @return An ItemStack for the SkyHopper or null if ItemStack creation failed.
     */
    @Nullable
    public ItemStack createItemStackFromSkyHopper(SkyHopper skyHopper, int amount) {
        final Settings settings = settingsManager.getSettings();
        if(settings == null) return null;

        final Settings.Item item = settings.skyhopper().item();

        final ItemStack itemStack = new ItemStack(Material.HOPPER);
        final ItemMeta itemMeta = itemStack.getItemMeta();

        // Set the item name
        if(item.name() != null) {
            itemMeta.displayName(FormatUtil.format(item.name()));
        }

        // Create a list of placeholders for the lore of the ItemStack.
        List<TagResolver.Single> placeholders = new ArrayList<>();
        if(item.placeholders().enabled() != null && item.placeholders().disabled() != null) {
            if(skyHopper.enabled()) {
                placeholders.add(Placeholder.parsed("status", item.placeholders().enabled()));
            } else {
                placeholders.add(Placeholder.parsed("status", item.placeholders().disabled()));
            }
        }

        if (skyHopper.owner() != null) {
            String playerName = plugin.getServer().getOfflinePlayer(skyHopper.owner()).getName();
            placeholders.add(Placeholder.parsed("name", String.valueOf(playerName)));
        } else {
            placeholders.add(Placeholder.parsed("name", "none"));
        }

        placeholders.add(Placeholder.parsed("member_count", String.valueOf(skyHopper.members().size())));
        placeholders.add(Placeholder.parsed("filter_type", skyHopper.filterType().name()));
        placeholders.add(Placeholder.parsed("current_links", String.valueOf(skyHopper.containers().size())));
        placeholders.add(Placeholder.parsed("max_links", String.valueOf(skyHopper.maxContainers())));
        placeholders.add(Placeholder.parsed("transfer_amount", String.valueOf(skyHopper.transferAmount())));
        placeholders.add(Placeholder.parsed("transfer_speed", String.valueOf(skyHopper.transferSpeed())));
        placeholders.add(Placeholder.parsed("suction_amount", String.valueOf(skyHopper.suctionAmount())));
        placeholders.add(Placeholder.parsed("suction_speed", String.valueOf(skyHopper.suctionSpeed())));
        placeholders.add(Placeholder.parsed("suction_range", String.valueOf(skyHopper.suctionRange())));

        List<Component> loreList = item.lore().stream().map(line -> FormatUtil.format(line, placeholders)).toList();

        itemMeta.lore(loreList);

        itemMeta.setEnchantmentGlintOverride(item.glow());

        saveSkyHopperToPDC(skyHopper, itemMeta.getPersistentDataContainer());

        itemStack.setItemMeta(itemMeta);

        itemStack.setAmount(amount);

        return itemStack;
    }

    /**
     * Get a SkyHopper from a PersistentDataContainer.
     * @param location The Location of the PersistentDataContainer or null if an ItemStack's PersistentDataContainer.
     * @param pdc The PersistentDataContainer.
     * @return A SkyHopper or null if SkyHopper creation failed.
     */
    @Nullable
    public SkyHopper getSkyHopperFromPDC(@Nullable Location location, @NotNull PersistentDataContainer pdc) {
        Locale locale = localeManager.getLocale();
        // Get the plugin's settings
        Settings settings = settingsManager.getSettings();
        if(settings == null) {
            plugin.getComponentLogger().info(FormatUtil.format(locale.prefix() + locale.failedSkyHopperLoad()));
            return null;
        }

        // Get the SkyHopper's settings from the PDC
        boolean enabled;
        boolean suctionParticles;
        FilterType inputFilterType;
        List<Material> filterItems = new ArrayList<>();
        UUID owner = null;
        List<UUID> members = new ArrayList<>();
        List<SkyContainer> containers = new ArrayList<>();
        double transferSpeed;
        double maxTransferSpeed;
        int transferAmount;
        int maxTransferAmount;
        double suctionSpeed;
        double maxSuctionSpeed;
        int suctionAmount;
        int maxSuctionAmount;
        int suctionRange;
        int maxSuctionRange;
        int maxContainers;
        long nextSuction;
        long nextTransfer;

        // If the hopper doesn't contain the ENABLED key, this Hopper or ItemStack is not a SkyHopper so we return null.
        Integer hopperStatus = pdc.get(HopperKeys.ENABLED.getKey(), PersistentDataType.INTEGER);
        if (hopperStatus == null) return null;

        // Set the SkyHopper's status
        enabled = hopperStatus == 1;

        // Set the suction particles status
        Integer particlesStatus = pdc.get(HopperKeys.PARTICLES.getKey(), PersistentDataType.INTEGER);
        if (particlesStatus != null) {
            suctionParticles = particlesStatus == 1;
        } else {
            suctionParticles = true;
        }

        // Get the SkyHopper's filter type
        inputFilterType = FilterType.getType(pdc.get(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING));

        // Get the input filter items
        // First handle the modern storage of the filter items.
        if(pdc.has(HopperKeys.FILTER_ITEMS.getKey(), PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING))) {
            List<String> modernMaterialNames = pdc.get(HopperKeys.FILTER_ITEMS.getKey(),
                    PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING));
            if (modernMaterialNames != null) {
                modernMaterialNames.stream().map(Material::getMaterial).filter(Objects::nonNull).forEach(filterItems::add);
            }
        }

        // Then handle the legacy storage of the filter items.
        if(pdc.has(HopperKeys.FILTER_ITEMS.getKey(), PersistentDataType.STRING)) {
            String materialsString = pdc.get(HopperKeys.FILTER_ITEMS.getKey(), PersistentDataType.STRING);
            List<Material> legacyMaterials = PluginUtils.deserializeMaterials(materialsString);
            filterItems.addAll(legacyMaterials);
        }

        // Get the owner of the SkyHopper
        // Get the modern storage of the owner otherwise try to get the legacy owner.
        if(pdc.has(HopperKeys.OWNER.getKey(), DataType.UUID)) {
            owner = pdc.get(HopperKeys.OWNER.getKey(), DataType.UUID);
        } else if(pdc.has(HopperKeys.OWNER.getKey(), PersistentDataType.STRING)) {
            String ownerString = pdc.get(HopperKeys.OWNER.getKey(), PersistentDataType.STRING);
            if(ownerString != null) {
                owner = UUID.fromString(ownerString);
            }
        }

        // Get the SkyHopper's members
        List<UUID> pdcMembers = pdc.get(HopperKeys.MEMBERS.getKey(), PersistentDataType.LIST.listTypeFrom(DataType.UUID));
        if(pdcMembers != null && !pdcMembers.isEmpty()) {
            members.addAll(pdcMembers);
        }

        // Get the legacy linked container
        if(pdc.has(HopperKeys.LINKED.getKey())) {
            final String serializedLocation = pdc.get(HopperKeys.LINKED.getKey(), PersistentDataType.STRING);

            if (serializedLocation != null) {
                BlockState linkedBlockState = PluginUtils.deserializeLocation(serializedLocation).getBlock().getState(false);

                if (linkedBlockState instanceof Container linkedContainer) {
                    containers.add(new SkyContainer(linkedContainer.getLocation(), FilterType.NONE, new ArrayList<>()));
                }
            }
        }

        // Get the linked containers (modern)
        if (pdc.has(HopperKeys.LINKS.getKey())) {
            final List<PersistentDataContainer> pdcList = pdc.get(HopperKeys.LINKS.getKey(), PersistentDataType.LIST.listTypeFrom(PersistentDataType.TAG_CONTAINER));

            // Check that the pdcList is not null and is not empty
            if (pdcList != null && !pdcList.isEmpty()) {
                pdcList.stream().filter(Objects::nonNull).forEach(linkedPDC -> {
                    // Get the linked container's location
                    Location linkedLocation = linkedPDC.get(HopperKeys.LOCATION.getKey(), DataType.LOCATION);

                    // Check if the location is not null and that the block is that of a Bukkit Container.
                    if(linkedLocation != null && linkedLocation.getBlock().getState(false) instanceof Container) {
                        // Get the output filter type.
                        FilterType outputFilterType = FilterType.getType(linkedPDC.get(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING));

                        // Get the output filter item names.
                        List<String> filterItemNames = linkedPDC.get(HopperKeys.FILTER_ITEMS.getKey(),
                                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING));

                        // Parse the item names into Materials.
                        ArrayList<Material> filterMaterials = filterItemNames != null ? new ArrayList<>(filterItemNames.stream().map(Material::getMaterial).filter(Objects::nonNull).toList()) : new ArrayList<>();

                        // Create the SkyContainer and add it to the list
                        containers.add(new SkyContainer(linkedLocation, outputFilterType, filterMaterials));
                    }
                });
            }
        }

        Double pdcTransferSpeed = pdc.get(HopperKeys.TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE);
        transferSpeed = Objects.requireNonNullElseGet(pdcTransferSpeed, () -> settings.skyhopper().startingTransferSpeed());

        Double pdcMaxTransferSpeed = pdc.get(HopperKeys.MAX_TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE);
        maxTransferSpeed = Objects.requireNonNullElse(pdcMaxTransferSpeed, transferSpeed);

        Integer pdcTransferAmount = pdc.get(HopperKeys.TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER);
        transferAmount = Objects.requireNonNullElseGet(pdcTransferAmount, () -> settings.skyhopper().startingTransferAmount());

        Integer pdcMaxTransferAmount = pdc.get(HopperKeys.MAX_TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER);
        maxTransferAmount = Objects.requireNonNullElse(pdcMaxTransferAmount, transferAmount);

        Double pdcSuctionSpeed = pdc.get(HopperKeys.SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE);
        suctionSpeed = Objects.requireNonNullElseGet(pdcSuctionSpeed, () -> settings.skyhopper().startingSuctionSpeed());

        Double pdcMaxSuctionSpeed = pdc.get(HopperKeys.MAX_SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE);
        maxSuctionSpeed = Objects.requireNonNullElse(pdcMaxSuctionSpeed, suctionSpeed);

        Integer pdcSuctionAmount = pdc.get(HopperKeys.SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER);
        suctionAmount = Objects.requireNonNullElseGet(pdcSuctionAmount, () -> settings.skyhopper().startingSuctionAmount());

        Integer pdcMaxSuctionAmount = pdc.get(HopperKeys.MAX_SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER);
        maxSuctionAmount = Objects.requireNonNullElse(pdcMaxSuctionAmount, suctionAmount);

        Integer pdcSuctionRange = pdc.get(HopperKeys.SUCTION_RANGE.getKey(), PersistentDataType.INTEGER);
        suctionRange = Objects.requireNonNullElseGet(pdcSuctionRange, () -> settings.skyhopper().startingSuctionRange());

        Integer pdcMaxSuctionRange = pdc.get(HopperKeys.MAX_SUCTION_RANGE.getKey(), PersistentDataType.INTEGER);
        maxSuctionRange = Objects.requireNonNullElse(pdcMaxSuctionRange, suctionRange);

        Integer pdcMaxContainers = pdc.get(HopperKeys.MAX_CONTAINERS.getKey(), PersistentDataType.INTEGER);
        maxContainers = Objects.requireNonNullElseGet(pdcMaxContainers, () -> settings.skyhopper().startingMaxContainers());

        // Set the next suction and transfer times
        nextSuction = System.currentTimeMillis() + ((long) suctionSpeed * 1000);
        nextTransfer = System.currentTimeMillis() + ((long) transferSpeed * 1000);

        // Create and return the SkyHopper
        return new SkyHopper(
                enabled,
                suctionParticles,
                owner,
                members,
                location,
                containers,
                inputFilterType,
                filterItems,
                transferSpeed,
                maxTransferSpeed,
                transferAmount,
                maxTransferAmount,
                suctionSpeed,
                maxSuctionSpeed,
                suctionAmount,
                maxSuctionAmount,
                suctionRange,
                maxSuctionRange,
                maxContainers,
                nextSuction,
                nextTransfer);
    }

    /**
     * Saves a SkyHopper to a PersistentDataContainer.
     * @param skyHopper The SkyHopper's data to save.
     * @param pdc The PersistentDataContainer to save data to.
     */
    public void saveSkyHopperToPDC(@NotNull SkyHopper skyHopper, @NotNull PersistentDataContainer pdc) {
        pdc.set(HopperKeys.ENABLED.getKey(), PersistentDataType.INTEGER, skyHopper.enabled() ? 1 : 0);

        pdc.set(HopperKeys.PARTICLES.getKey(), PersistentDataType.INTEGER, skyHopper.particles() ? 1 : 0);

        // Save the Skyhopper's owner
        if (skyHopper.owner() != null) {
            pdc.set(HopperKeys.OWNER.getKey(), DataType.UUID, skyHopper.owner());
        }

        // Save the SkyHopper's members
        pdc.set(HopperKeys.MEMBERS.getKey(), PersistentDataType.LIST.listTypeFrom(DataType.UUID), skyHopper.members());

        // Save the input filter type
        pdc.set(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING, skyHopper.filterType().name());

        // Save the input filter items
        pdc.set(HopperKeys.FILTER_ITEMS.getKey(),
                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING),
                skyHopper.filterItems().stream().map(Material::toString).filter(Objects::nonNull)
                        .collect(Collectors.toList()));

        // Save the linked containers
        List<PersistentDataContainer> pdcList = new ArrayList<>();

        for (SkyContainer skyContainer : skyHopper.containers()) {
            Location linkedLocation = skyContainer.location();

            FilterType filterType = skyContainer.filterType();
            PersistentDataContainer persistentDataContainer = pdc.getAdapterContext().newPersistentDataContainer();
            persistentDataContainer.set(HopperKeys.LOCATION.getKey(), DataType.LOCATION, linkedLocation);
            persistentDataContainer.set(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING, filterType.name());
            persistentDataContainer.set(HopperKeys.FILTER_ITEMS.getKey(),
                    PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING),
                    Objects.requireNonNull(skyContainer.filterItems()).stream().map(Material::toString).filter(Objects::nonNull)
                            .collect(Collectors.toList()));

            pdcList.add(persistentDataContainer);
        }

        pdc.set(HopperKeys.LINKS.getKey(), PersistentDataType.LIST.listTypeFrom(PersistentDataType.TAG_CONTAINER), pdcList);

        // Save the SkyHopper upgrades
        pdc.set(HopperKeys.TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.transferSpeed());

        pdc.set(HopperKeys.MAX_TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.maxTransferSpeed());

        pdc.set(HopperKeys.TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.transferAmount());

        pdc.set(HopperKeys.MAX_TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.maxTransferAmount());

        pdc.set(HopperKeys.SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.suctionSpeed());

        pdc.set(HopperKeys.MAX_SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.maxSuctionSpeed());

        pdc.set(HopperKeys.SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.suctionAmount());

        pdc.set(HopperKeys.MAX_SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.maxSuctionAmount());

        pdc.set(HopperKeys.SUCTION_RANGE.getKey(), PersistentDataType.INTEGER, skyHopper.suctionRange());

        pdc.set(HopperKeys.MAX_SUCTION_RANGE.getKey(), PersistentDataType.INTEGER, skyHopper.maxSuctionRange());

        pdc.set(HopperKeys.MAX_CONTAINERS.getKey(), PersistentDataType.INTEGER, skyHopper.maxContainers());
    }

    /**
     * Saves a SkyHopper to a Hopper's PersistentDataContainer.
     * @param skyHopper The SkyHopper's data to save.
     * @param hopper The Hopper to get the PersistentDataContainer for.
     */
    public void saveSkyHopperToBlockPDC(@NotNull SkyHopper skyHopper, @NotNull Hopper hopper) {
        saveSkyHopperToPDC(skyHopper, hopper.getPersistentDataContainer());

        // Update the hopper block
        hopper.update();
    }

    /**
     * Adds a viewer for a SkyHopper's Location.
     * @param location The Location of the SkyHopper.
     * @param viewer The Player's UUID viewing the SkyHopper's settings.
     * @param gui The GUI the Player is viewing.
     */
    public void addViewer(Location location, UUID viewer, ChestGUI gui) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            HashMap<UUID, ChestGUI> uuidGuiMap = guiData.getOrDefault(location, new HashMap<>());

            uuidGuiMap.put(viewer, gui);
            guiData.put(location, uuidGuiMap);
        }, 1L);
    }

    /**
     * Removes a viewer for a SkyHopper's Location.
     * @param location The Location of the SkyHopper.
     * @param viewer The Player's UUID who was viewing the SkyHopper's settings.
     */
    public void removeViewer(Location location, UUID viewer) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            HashMap<UUID, ChestGUI> uuidGuiMap = guiData.get(location);
            if(uuidGuiMap == null) return;

            uuidGuiMap.remove(viewer);

            if(uuidGuiMap.isEmpty()) {
                guiData.remove(location);
            } else {
                guiData.put(location, uuidGuiMap);
            }
        }, 1L);
    }

    /**
     * Refreshes all players viewing a SkyHopper's settings at the given Location.
     * @param location The Location of the SkyHopper.
     */
    public void refreshViewersGUI(Location location) {
        HashMap<UUID, ChestGUI> uuidGuiMap = guiData.get(location);
        if(uuidGuiMap == null) return;

        if(uuidGuiMap.isEmpty()) {
            guiData.remove(location);
            return;
        }

        for(Map.Entry<UUID, ChestGUI> entry : uuidGuiMap.entrySet()) {
            UUID uuid = entry.getKey();
            ChestGUI gui = entry.getValue();

            Player player = plugin.getServer().getPlayer(uuid);
            if(player != null && player.isOnline() && player.isConnected()) {
                gui.refresh();
            }
        }
    }

    /**
     * Closes the GUI for any players viewing an OutputFilterGUI for the given SkyHopper Location if a linked container was removed.
     * @param location The Location of the SkyHopper.
     */
    public void handleContainerRemoved(Location location) {
        HashMap<UUID, ChestGUI> uuidGuiMap = guiData.get(location);
        if(uuidGuiMap == null) return;

        if(uuidGuiMap.isEmpty()) {
            guiData.remove(location);
            return;
        }

        for(Map.Entry<UUID, ChestGUI> entry : uuidGuiMap.entrySet()) {
            UUID uuid = entry.getKey();
            ChestGUI gui = entry.getValue();

            Player player = plugin.getServer().getPlayer(uuid);
            if(player != null && player.isOnline() && player.isConnected()) {
                if(gui instanceof OutputFilterGUI) {
                    gui.close(plugin, player);
                }
            }
        }
    }

    /**
     * Closes all open GUIs.
     * @param onDisable Whether the closure is occurring on plugin disable or not.
     */
    public void closeOpenGuis(boolean onDisable) {
        for(Map.Entry<Location, HashMap<UUID, ChestGUI>> locationEntry : guiData.entrySet()) {
            HashMap<UUID, ChestGUI> viewers = locationEntry.getValue();

            for(Map.Entry<UUID, ChestGUI> viewerEntry : viewers.entrySet()) {
                UUID uuid = viewerEntry.getKey();
                Player player = plugin.getServer().getPlayer(uuid);
                if(player != null && player.isOnline() && player.isConnected()) {
                    if(onDisable) {
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);
                    } else {
                        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                                player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
                    }
                }
            }
        }

        guiData.clear();
    }

    /**
     * Gets the GUI that is open by the provided player's UUID.
     * @param uuid The UUID of the player.
     * @return The GUI the player is viewing or null if not viewing an open GUI.
     */
    @Nullable
    public ChestGUI getGuiByUUID(UUID uuid) {
        for(Map.Entry<Location, HashMap<UUID, ChestGUI>> locationEntry : guiData.entrySet()) {
            HashMap<UUID, ChestGUI> viewers = locationEntry.getValue();

            for(Map.Entry<UUID, ChestGUI> viewerEntry : viewers.entrySet()) {
                UUID viewerUuid = viewerEntry.getKey();
                if(viewerUuid.equals(uuid)) {
                    return viewerEntry.getValue();
                }
            }
        }

        return null;
    }
}