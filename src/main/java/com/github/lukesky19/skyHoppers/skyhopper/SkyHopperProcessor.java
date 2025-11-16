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
package com.github.lukesky19.skyHoppers.skyhopper;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.LocaleManager;
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.config.data.Locale;
import com.github.lukesky19.skyHoppers.config.data.Settings;
import com.github.lukesky19.skyHoppers.gui.GUIManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.Filterable;
import com.github.lukesky19.skyHoppers.skyhopper.data.HopperKeys;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.task.data.QueuedTransfer;
import com.github.lukesky19.skyHoppers.util.PluginUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skylib.libs.morepersistentdatatypes.DataType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class is used to load and unload {@link SkyHopper}s in {@link Chunk}s.
 */
public class SkyHopperProcessor {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull SkyHopperManager skyHopperManager;
    private final int LATEST_SKYHOPPER_VERSION = 1;

    private final @NotNull Queue<Chunk> chunkLoadQueue = new LinkedList<>();
    private final @NotNull Queue<Chunk> chunkUnloadQueue = new LinkedList<>();
    private final @NotNull Queue<QueuedTransfer> queuedTransfers = new LinkedList<>();

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param skyHopperManager A {@link SkyHopperManager} instance.
     */
    public SkyHopperProcessor(
            @NotNull SkyHoppers skyHoppers,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIManager guiManager,
            @NotNull SkyHopperManager skyHopperManager) {
        this.skyHoppers = skyHoppers;
        this.logger = skyHoppers.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.skyHopperManager = skyHopperManager;
    }

    /**
     * Get the next {@link Chunk} to be processed or null.
     * @return The next {@link Chunk} to be processed or null.
     */
    public @Nullable Chunk getNextQueuedLoadChunk() {
        return chunkLoadQueue.poll();
    }

    /**
     * Queues a chunk to load SkyHoppers in.
     * @param chunk The {@link Chunk} to queue.
     */
    public void queueLoadChunk(@NotNull Chunk chunk) {
        chunkUnloadQueue.remove(chunk);

        chunkLoadQueue.add(chunk);
    }

    /**
     * Get the next unloaded {@link Chunk} to be processed or null.
     * @return The next unloaded {@link Chunk} to be processed or null.
     */
    public @Nullable Chunk getNextQueuedUnloadChunk() {
        return chunkUnloadQueue.poll();
    }

    /**
     * Queues a chunk to unload SkyHoppers in.
     * @param chunk The {@link Chunk} to queue.
     */
    public void queueUnloadChunk(@NotNull Chunk chunk) {
        chunkLoadQueue.remove(chunk);

        chunkUnloadQueue.add(chunk);
    }

    /**
     * Get the next {@link QueuedTransfer} to be processed or null.
     * @return The next {@link QueuedTransfer} to be processed or null.
     */
    public @Nullable QueuedTransfer getQueuedTransfer() {
        return queuedTransfers.poll();
    }

    /**
     * Queues a {@link QueuedTransfer} to be processed or null.
     * @param queuedTransfer The {@link QueuedTransfer} to queue.
     */
    public void queueQueuedTransfer(@NotNull QueuedTransfer queuedTransfer) {
        queuedTransfers.add(queuedTransfer);
    }

    /**
     * Clear the queued transfers.
     */
    public void clearQueuedTransfers() {
        queuedTransfers.clear();
    }

    /**
     * Queue the currently loaded chunks for processing.
     */
    public void queueLoadedChunks() {
        for(World world : skyHoppers.getServer().getWorlds()) {
            for(Chunk chunk : world.getLoadedChunks()) {
                queueLoadChunk(chunk);
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

        // Retrieve SkyHoppers for the specific chunk
        List<Location> locationList = skyHopperManager.getSkyHopperDataManager().getLocationsInChunk(chunk.getWorld(), chunkX, chunkZ);

        for(Location location : locationList) {
            loadSkyHopperAtLocation(location);
        }
    }

    /**
     * Attempt to load the SkyHopper at the provided {@link Location}.
     * @param location The {@link Location} of the SkyHopper to load.
     */
    public void loadSkyHopperAtLocation(@NotNull Location location) {
        // Check if the location is a SkyHopper
        if(!skyHopperManager.isLocationSkyHopper(location)) return;
        // Check if the SkyHopper is already loaded
        if(skyHopperManager.getSkyHopperDataManager().isSkyHopperLoaded(location)) return;

        this.loadSkyHopperAtLocationDirectly(location);
    }

    /**
     * Loads a {@link SkyHopper} from the location provided directly if possible.
     * @param location The {@link Location} of the SkyHopper.
     */
    public void loadSkyHopperAtLocationDirectly(@NotNull Location location) {
        // Check if the block at the location is a hopper
        if(!(location.getBlock().getState(false) instanceof Hopper hopper)) return;

        // Get the PersistentDataContainer
        PersistentDataContainer pdc = hopper.getPersistentDataContainer();

        // Check if Hopper is not a SkyHopper
        Integer hopperStatus = pdc.get(HopperKeys.ENABLED.getKey(), PersistentDataType.INTEGER);
        // If the hopper status is null, this is not a SkyHopper so there is no data to load and the SkyHopper was removed in some other way.
        if(hopperStatus == null) {
            skyHopperManager.getSkyHopperDataManager().removeSkyHopper(location);
            return;
        }

        // Get the SkyHopper from the given Hopper
        @Nullable SkyHopper skyHopper = loadSkyHopper(location, pdc);
        if(skyHopper == null) return;

        // Save any updated SkyHopper data to the Hopper PDC
        skyHopperManager.getSkyHopperSaver().saveSkyHopper(skyHopper, hopper);

        // Cache the SkyHopper
        skyHopperManager.getSkyHopperDataManager().cacheSkyHopper(location, skyHopper);
    }

    /**
     * Saves and unloads any SkyHoppers in a chunk.
     * @param chunk The chunk to check for SkyHoppers to unload.
     */
    public void unLoadSkyHoppersInChunk(@NotNull Chunk chunk) {
        @NotNull Map<Location, SkyHopper> skyHopperMap = skyHopperManager.getSkyHopperDataManager().getSkyHoppersMap();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        Iterator<Map.Entry<Location, SkyHopper>> iterator = skyHopperMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Location, SkyHopper> entry = iterator.next();
            Location location = entry.getKey();

            // Change location X and Z to chunk X and Z.
            int locX = location.getBlockX() >> 4;
            int locZ = location.getBlockZ() >> 4;
            if(locX != chunkX || locZ != chunkZ) continue;

            // Close any open GUIs for this SkyHopper
            guiManager.closeOpenGUIsForLocation(location);

            // Then remove it from the cache
            iterator.remove();
        }
    }

    /**
     * Load the {@link SkyHopper} from the {@link PersistentDataContainer}.
     * @param location The {@link Location} of the {@link SkyHopper}. May be null.
     * @param pdc The {@link PersistentDataContainer} to load from.
     * @return A {@link SkyHopper} or null.
     */
    public @Nullable SkyHopper loadSkyHopper(@Nullable Location location, @NotNull PersistentDataContainer pdc) {
        @Nullable Settings settings = settingsManager.getSettings();
        @NotNull Locale locale = localeManager.getLocale();

        if(settings == null) {
            logger.info(AdventureUtil.serialize(locale.prefix() + locale.failedSkyHopperLoad()));
            return null;
        }

        // Get the hopper status
        Integer hopperStatus = pdc.get(HopperKeys.ENABLED.getKey(), PersistentDataType.INTEGER);
        // If the hopper status is null, this is not a SkyHoppers o there is no data to load
        if(hopperStatus == null) return null;

        // Create a default SkyHopper.
        SkyHopper skyHopper = new SkyHopper(settings);

        // Set the SkyHopper's status
        skyHopper.setEnabled(hopperStatus == 1);

        // Set the version number
        skyHopper.setVersion(pdc.getOrDefault(HopperKeys.VERSION.getKey(), PersistentDataType.INTEGER, 0));

        // Set the SkyHopper's location
        skyHopper.setLocation(location);

        // Load the rest of the SkyHopper data based on its version number
        return switch(skyHopper.getVersion()) {
            case LATEST_SKYHOPPER_VERSION -> loadVersionOneSkyHopper(settings, skyHopper, pdc);

            default -> loadUnVersionedSkyHopper(settings, skyHopper, pdc);
        };
    }

    /**
     * Load the {@link SkyHopper} from the {@link PersistentDataContainer} using the legacy format.
     * @param settings The plugin's {@link Settings}.
     * @param skyHopper The {@link SkyHopper} to apply settings to.
     * @param pdc The {@link PersistentDataContainer} to load SkyHopper settings from.
     * @return A {@link SkyHopper}.
     */
    public @NotNull SkyHopper loadUnVersionedSkyHopper(@NotNull Settings settings, @NotNull SkyHopper skyHopper, @NotNull PersistentDataContainer pdc) {
        // Set the SkyHopper's version number to the latest
        skyHopper.setVersion(LATEST_SKYHOPPER_VERSION);

        // Set the particles status
        skyHopper.setParticles(pdc.getOrDefault(HopperKeys.PARTICLES.getKey(), PersistentDataType.INTEGER, 1) != 0);

        // Set the SkyHopper's filter type
        skyHopper.setFilterType(Filterable.FilterType.getType(pdc.get(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING)));

        // Get the SkyHopper's filtered ItemTypes
        // First handle the modern storage of the filter items.
        if(pdc.has(HopperKeys.FILTER_ITEMS.getKey(), PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING))) {
            List<String> modernItemTypeNames = pdc.get(HopperKeys.FILTER_ITEMS.getKey(),
                    PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING));
            if(modernItemTypeNames != null) {
                modernItemTypeNames.stream()
                        .map(itemTypeName -> RegistryUtil.getItemType(logger, itemTypeName))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(skyHopper::addFilterItem);
            }
        }

        // Then handle the legacy storage of the filter items.
        if(pdc.has(HopperKeys.FILTER_ITEMS.getKey(), PersistentDataType.STRING)) {
            String materialsString = pdc.get(HopperKeys.FILTER_ITEMS.getKey(), PersistentDataType.STRING);
            List<Material> legacyMaterials = PluginUtils.deserializeMaterials(materialsString);
            List<ItemType> itemTypes = legacyMaterials.stream().map(Material::asItemType).filter(Objects::nonNull).toList();
            itemTypes.forEach(skyHopper::addFilterItem);
        }

        // Get the owner of the SkyHopper
        // Get the modern storage of the owner otherwise try to get the legacy owner.
        if(pdc.has(HopperKeys.OWNER.getKey(), DataType.UUID)) {
            skyHopper.setOwner(pdc.get(HopperKeys.OWNER.getKey(), DataType.UUID));
        } else if(pdc.has(HopperKeys.OWNER.getKey(), PersistentDataType.STRING)) {
            String ownerString = pdc.get(HopperKeys.OWNER.getKey(), PersistentDataType.STRING);
            if(ownerString != null) {
                skyHopper.setOwner(UUID.fromString(ownerString));
            }
        }

        // Get the SkyHopper's members
        List<UUID> pdcMembers = pdc.get(HopperKeys.MEMBERS.getKey(), PersistentDataType.LIST.listTypeFrom(DataType.UUID));
        if(pdcMembers != null && !pdcMembers.isEmpty()) {
            pdcMembers.forEach(skyHopper::addMember);
        }

        // Get the legacy linked container
        if(pdc.has(HopperKeys.LINKED.getKey())) {
            final String serializedLocation = pdc.get(HopperKeys.LINKED.getKey(), PersistentDataType.STRING);

            if(serializedLocation != null) {
                @Nullable Location deserializedLocation = PluginUtils.deserializeLocation(serializedLocation);
                if(deserializedLocation != null) {
                    BlockState linkedBlockState = deserializedLocation.getBlock().getState(false);

                    if (linkedBlockState instanceof Container linkedContainer) {
                        skyHopper.addLinkedContainer(new SkyContainer(linkedContainer.getLocation(), Filterable.FilterType.NONE, new ArrayList<>()));
                    }
                }
            }
        }

        // Get the linked containers (modern)
        if(pdc.has(HopperKeys.LINKS.getKey())) {
            List<PersistentDataContainer> pdcList = pdc.get(HopperKeys.LINKS.getKey(), PersistentDataType.LIST.listTypeFrom(PersistentDataType.TAG_CONTAINER));

            // Check that the pdcList is not null and is not empty
            if (pdcList != null && !pdcList.isEmpty()) {
                pdcList.stream().filter(Objects::nonNull).forEach(linkedPDC -> {
                    // Get the linked container's location
                    Location linkedLocation = linkedPDC.get(HopperKeys.LOCATION.getKey(), DataType.LOCATION);

                    // Check if the location is not null and that the block is that of a Bukkit Container.
                    if(linkedLocation != null && linkedLocation.getBlock().getState(false) instanceof Container) {
                        // Get the output filter type.
                        Filterable.FilterType outputFilterType = Filterable.FilterType.getType(linkedPDC.get(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING));

                        // Get the output filter item names.
                        List<String> filterItemNames = linkedPDC.get(HopperKeys.FILTER_ITEMS.getKey(),
                                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING));

                        // Parse the item names into ItemTypes.
                        List<ItemType> linkedContainerFilterItems = filterItemNames != null ? new ArrayList<>(filterItemNames.stream().map(itemName -> RegistryUtil.getItemType(logger, itemName)).filter(Optional::isPresent).map(Optional::get).toList()) : new ArrayList<>();

                        // Create the SkyContainer and add it to the list
                        skyHopper.addLinkedContainer(new SkyContainer(linkedLocation, outputFilterType, linkedContainerFilterItems));
                    }
                });
            }
        }

        // Transfer Speed
        double pdcTransferSpeed = pdc.getOrDefault(HopperKeys.TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE, settings.skyHopperConfig().startingTransferSpeed());
        double pdcMaxTransferSpeed = pdc.getOrDefault(HopperKeys.MAX_TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE, pdcTransferSpeed);
        skyHopper.setTransferSpeed(pdcTransferSpeed);
        skyHopper.setMaxTransferSpeed(pdcMaxTransferSpeed);

        // Transfer Amount
        int pdcTransferAmount = pdc.getOrDefault(HopperKeys.TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER, settings.skyHopperConfig().startingTransferAmount());
        int pdcMaxTransferAmount = pdc.getOrDefault(HopperKeys.MAX_TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER, pdcTransferAmount);
        skyHopper.setTransferAmount(pdcTransferAmount);
        skyHopper.setMaxTransferAmount(pdcMaxTransferAmount);

        // Suction Speed
        double pdcSuctionSpeed = pdc.getOrDefault(HopperKeys.SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE, settings.skyHopperConfig().startingSuctionSpeed());
        double pdcMaxSuctionSpeed = pdc.getOrDefault(HopperKeys.MAX_SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE, pdcSuctionSpeed);
        skyHopper.setSuctionSpeed(pdcSuctionSpeed);
        skyHopper.setMaxSuctionSpeed(pdcMaxSuctionSpeed);

        // Suction Amount
        int pdcSuctionAmount = pdc.getOrDefault(HopperKeys.SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER, settings.skyHopperConfig().startingSuctionAmount());
        int pdcMaxSuctionAmount = pdc.getOrDefault(HopperKeys.MAX_SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER, pdcSuctionAmount);
        skyHopper.setSuctionAmount(pdcSuctionAmount);
        skyHopper.setMaxSuctionAmount(pdcMaxSuctionAmount);

        // Suction Change
        int pdcSuctionRange = pdc.getOrDefault(HopperKeys.SUCTION_RANGE.getKey(), PersistentDataType.INTEGER, settings.skyHopperConfig().startingSuctionRange());
        int pdcMaxSuctionRange = pdc.getOrDefault(HopperKeys.MAX_SUCTION_RANGE.getKey(), PersistentDataType.INTEGER, pdcSuctionRange);
        skyHopper.setSuctionRange(pdcSuctionRange);
        skyHopper.setMaxSuctionRange(pdcMaxSuctionRange);

        int pdcMaxContainers = pdc.getOrDefault(HopperKeys.MAX_CONTAINERS.getKey(), PersistentDataType.INTEGER, settings.skyHopperConfig().startingMaxContainers());
        skyHopper.setMaxContainers(pdcMaxContainers);

        // Update the next suction and transfer times
        long now = System.currentTimeMillis();
        skyHopper.setNextSuctionTime(now + ((long) skyHopper.getSuctionSpeed() * 1000L));
        skyHopper.setNextTransferTime(now + ((long) skyHopper.getTransferSpeed() * 1000L));

        return skyHopper;
    }

    /**
     * Load the {@link SkyHopper} using the version one format.
     * @param settings The plugin's {@link Settings}.
     * @param skyHopper The {@link SkyHopper} to apply settings to.
     * @param pdc The {@link PersistentDataContainer} to load SkyHopper settings from.
     * @return A {@link SkyHopper}.
     */
    public @NotNull SkyHopper loadVersionOneSkyHopper(@NotNull Settings settings, @NotNull SkyHopper skyHopper, @NotNull PersistentDataContainer pdc) {
        // Set the particles status
        skyHopper.setParticles(pdc.getOrDefault(HopperKeys.PARTICLES.getKey(), PersistentDataType.INTEGER, 1) != 0);

        // Set the SkyHopper's filter type
        skyHopper.setFilterType(Filterable.FilterType.getType(pdc.get(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING)));

        // Set the SkyHopper's filtered ItemTypes
        if(pdc.has(HopperKeys.FILTER_ITEMS.getKey(), PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING))) {
            List<String> modernItemTypeNames = pdc.get(HopperKeys.FILTER_ITEMS.getKey(),
                    PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING));
            if(modernItemTypeNames != null) {
                modernItemTypeNames.stream()
                        .map(itemTypeName -> RegistryUtil.getItemType(logger, itemTypeName))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(skyHopper::addFilterItem);
            }
        }

        // Set the owner of the SkyHopper
        skyHopper.setOwner(pdc.get(HopperKeys.OWNER.getKey(), DataType.UUID));

        // Set the SkyHopper's members
        List<UUID> pdcMembers = pdc.get(HopperKeys.MEMBERS.getKey(), PersistentDataType.LIST.listTypeFrom(DataType.UUID));
        if(pdcMembers != null && !pdcMembers.isEmpty()) {
            pdcMembers.forEach(skyHopper::addMember);
        }

        // Get the linked containers (modern)
        if(pdc.has(HopperKeys.LINKS.getKey())) {
            List<PersistentDataContainer> pdcList = pdc.get(HopperKeys.LINKS.getKey(), PersistentDataType.LIST.listTypeFrom(PersistentDataType.TAG_CONTAINER));

            // Check that the pdcList is not null and is not empty
            if (pdcList != null && !pdcList.isEmpty()) {
                pdcList.stream().filter(Objects::nonNull).forEach(linkedPDC -> {
                    // Get the linked container's location
                    Location linkedLocation = linkedPDC.get(HopperKeys.LOCATION.getKey(), DataType.LOCATION);

                    // Check if the location is not null and that the block is that of a Bukkit Container.
                    if(linkedLocation != null && linkedLocation.getBlock().getState(false) instanceof Container) {
                        // Get the SkyContainer's filter type.
                        Filterable.FilterType outputFilterType = Filterable.FilterType.getType(linkedPDC.get(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING));

                        // Get the SkyContainer's filter item names.
                        List<String> filterItemNames = linkedPDC.get(HopperKeys.FILTER_ITEMS.getKey(),
                                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING));

                        // Parse the item names into ItemTypes.
                        List<ItemType> linkedContainerFilterItems = filterItemNames != null ? new ArrayList<>(filterItemNames.stream().map(itemName -> RegistryUtil.getItemType(logger, itemName)).filter(Optional::isPresent).map(Optional::get).toList()) : new ArrayList<>();

                        // Create the SkyContainer and add it to the list
                        skyHopper.addLinkedContainer(new SkyContainer(linkedLocation, outputFilterType, linkedContainerFilterItems));
                    }
                });
            }
        }

        // Transfer Speed
        double pdcTransferSpeed = pdc.getOrDefault(HopperKeys.TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE, settings.skyHopperConfig().startingTransferSpeed());
        double pdcMaxTransferSpeed = pdc.getOrDefault(HopperKeys.MAX_TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE, pdcTransferSpeed);
        skyHopper.setTransferSpeed(pdcTransferSpeed);
        skyHopper.setMaxTransferSpeed(pdcMaxTransferSpeed);

        // Transfer Amount
        int pdcTransferAmount = pdc.getOrDefault(HopperKeys.TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER, settings.skyHopperConfig().startingTransferAmount());
        int pdcMaxTransferAmount = pdc.getOrDefault(HopperKeys.MAX_TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER, pdcTransferAmount);
        skyHopper.setTransferAmount(pdcTransferAmount);
        skyHopper.setMaxTransferAmount(pdcMaxTransferAmount);

        // Suction Speed
        double pdcSuctionSpeed = pdc.getOrDefault(HopperKeys.SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE, settings.skyHopperConfig().startingSuctionSpeed());
        double pdcMaxSuctionSpeed = pdc.getOrDefault(HopperKeys.MAX_SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE, pdcSuctionSpeed);
        skyHopper.setSuctionSpeed(pdcSuctionSpeed);
        skyHopper.setMaxSuctionSpeed(pdcMaxSuctionSpeed);

        // Suction Amount
        int pdcSuctionAmount = pdc.getOrDefault(HopperKeys.SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER, settings.skyHopperConfig().startingSuctionAmount());
        int pdcMaxSuctionAmount = pdc.getOrDefault(HopperKeys.MAX_SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER, pdcSuctionAmount);
        skyHopper.setSuctionAmount(pdcSuctionAmount);
        skyHopper.setMaxSuctionAmount(pdcMaxSuctionAmount);

        // Suction Change
        int pdcSuctionRange = pdc.getOrDefault(HopperKeys.SUCTION_RANGE.getKey(), PersistentDataType.INTEGER, settings.skyHopperConfig().startingSuctionRange());
        int pdcMaxSuctionRange = pdc.getOrDefault(HopperKeys.MAX_SUCTION_RANGE.getKey(), PersistentDataType.INTEGER, pdcSuctionRange);
        skyHopper.setSuctionRange(pdcSuctionRange);
        skyHopper.setMaxSuctionRange(pdcMaxSuctionRange);

        int pdcMaxContainers = pdc.getOrDefault(HopperKeys.MAX_CONTAINERS.getKey(), PersistentDataType.INTEGER, settings.skyHopperConfig().startingMaxContainers());
        skyHopper.setMaxContainers(pdcMaxContainers);

        // Update the next suction and transfer times
        long now = System.currentTimeMillis();
        skyHopper.setNextSuctionTime(now + ((long) skyHopper.getSuctionSpeed() * 1000L));
        skyHopper.setNextTransferTime(now + ((long) skyHopper.getTransferSpeed() * 1000L));

        return skyHopper;
    }
}