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
import com.github.lukesky19.skyHoppers.database.DatabaseManager;
import com.github.lukesky19.skyHoppers.hopper.*;
import com.github.lukesky19.skyHoppers.util.PluginUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.morepersistentdatatypes.DataType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class manages {@link SkyHopper}s including storage, creation, and saving.
 */
public class HopperManager {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull GUIManager guiManager;

    private final @NotNull List<@NotNull Location> hopperLocations = new ArrayList<>();
    private final @NotNull Map<@NotNull Location, @NotNull SkyHopper> skyHopperMap = new HashMap<>();

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     */
    public HopperManager(
            @NotNull SkyHoppers skyHoppers,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull GUIManager guiManager) {
        this.skyHoppers = skyHoppers;
        this.databaseManager = databaseManager;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
    }

    /**
     * Get the {@link SkyHopper} at a given location.
     * @param location The {@link Location} of the v
     * @return The {@link SkyHopper} or null if there is no {@link SkyHopper} at that {@link Location}.
     */
    public @Nullable SkyHopper getSkyHopper(@NotNull Location location) {
        return skyHopperMap.get(location);
    }

    /**
     * Get a {@link List} of {@link SkyHopper}s that are loaded.
     * @return A {@link List} of {@link SkyHopper}s that are loaded.
     */
    public @NotNull List<SkyHopper> getSkyHoppers() {
        return new ArrayList<>(skyHopperMap.values());
    }

    /**
     * Reloads all SkyHopper locations and caches all SkyHoppers in loaded chunks.
     */
    public void reload() {
        hopperLocations.clear();
        skyHopperMap.clear();

        // Migrates the old database to the new
        databaseManager.migrateLegacyDatabase().whenComplete((v, t) -> {
            // Gets all SkyHopper locations from the database
            hopperLocations.clear();

            // Load SkyHopper Locations
            databaseManager.getHoppersTable().getSkyHopperLocations().thenAccept(list -> {
                hopperLocations.addAll(list);

                // Load SkyHoppers in loaded chunks
                for(World world : skyHoppers.getServer().getWorlds()) {
                    for(Chunk chunk : world.getLoadedChunks()) {
                        loadSkyHoppersInChunk(chunk);
                    }
                }
            });
        });
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
                // Check if the SkyHopper is already loaded
                if(skyHopperMap.containsKey(location)) return;

                // Check if the block at the location is a hopper
                if(location.getBlock().getState(false) instanceof Hopper hopper) {
                    // Get the PersistentDataContainer
                    PersistentDataContainer pdc = hopper.getPersistentDataContainer();

                    // Get the SkyHopper from the given Hopper
                    SkyHopper skyHopper = getSkyHopperFromPDC(location, pdc);

                    // Check if the SkyHopper is valid
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
     * Loads a {@link SkyHopper} at a given Location.
     * @apiNote This method has performance costs because it loads the chunk if it isn't already loaded.
     * @param location The {@link Location} of the {@link SkyHopper}.
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
        skyHoppers.pauseSkyHoppers();

        guiManager.closeOpenGUIs(false);

        skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () -> {
            if(!force) {
                for (Location location : hopperLocations) {
                    if (!skyHopperMap.containsKey(location)) {
                        loadSkyHopperAtLocation(location);
                    }
                }
            } else {
                skyHopperMap.clear();

                for (Location location : hopperLocations) {
                    loadSkyHopperAtLocation(location);
                }
            }

            skyHoppers.unPauseSkyHoppers();
        }, 1L);
    }

    /**
     * Saves a {@link SkyHopper} to the {@link #skyHopperMap}.
     * @param location The {@link Location} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper}.
     */
    public void cacheSkyHopper(@NotNull Location location, @NotNull SkyHopper skyHopper) {
        databaseManager.getHoppersTable().addSkyHopperLocation(location);

        skyHopperMap.put(location, skyHopper);
    }

    /**
     * Removes a {@link SkyHopper} from the cache, the {@link Location} database, and closes any open GUIs for the {@link SkyHopper}'s {@link Location}.
     * @param location The {@link Location} of the {@link SkyHopper}.
     */
    public void removeSkyHopper(@NotNull Location location) {
        databaseManager.getHoppersTable().removeSkyHopperLocation(location);

        skyHopperMap.remove(location);

        guiManager.closeOpenGUIsForLocation(location);
    }

    /**
     * Checks if an {@link ItemStack} is a {@link SkyHopper}.
     * @param itemStack The {@link ItemStack} to check.
     * @return true if a {@link SkyHopper}, false if not.
     */
    public boolean isItemStackSkyHopper(@NotNull ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer()
                .get(HopperKeys.ENABLED.getKey(), PersistentDataType.INTEGER) != null;
    }

    /**
     * Creates an {@link ItemStack} for a {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper}.
     * @param amount The amount to set the {@link ItemStack} amount to.
     * @return An {@link ItemStack} for the {@link SkyHopper} or null if {@link ItemStack} creation failed.
     */
    public @Nullable ItemStack createItemStackFromSkyHopper(@NotNull SkyHopper skyHopper, int amount) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return null;

        ItemStackConfig itemStackConfig = settings.skyHopperConfig().item();
        Settings.Placeholders placeholdersConfig = settings.skyHopperConfig().placeholders();

        // Create a list of placeholders for the lore of the ItemStack.
        List<TagResolver.Single> placeholders = new ArrayList<>();

        if(placeholdersConfig.enabled() != null && placeholdersConfig.disabled() != null) {
            if(skyHopper.isSkyHopperEnabled()) {
                placeholders.add(Placeholder.parsed("status", placeholdersConfig.enabled()));
            } else {
                placeholders.add(Placeholder.parsed("status", placeholdersConfig.disabled()));
            }
        }

        if(skyHopper.getOwner() != null) {
            String playerName = skyHoppers.getServer().getOfflinePlayer(skyHopper.getOwner()).getName();
            placeholders.add(Placeholder.parsed("name", String.valueOf(playerName)));
        } else {
            placeholders.add(Placeholder.parsed("name", "none"));
        }

        placeholders.add(Placeholder.parsed("member_count", String.valueOf(skyHopper.getMembers().size())));
        placeholders.add(Placeholder.parsed("filter_type", skyHopper.getFilterType().name()));
        placeholders.add(Placeholder.parsed("current_links", String.valueOf(skyHopper.getLinkedContainers().size())));
        placeholders.add(Placeholder.parsed("max_links", String.valueOf(skyHopper.getMaxContainers())));
        placeholders.add(Placeholder.parsed("transfer_amount", String.valueOf(skyHopper.getTransferAmount())));
        placeholders.add(Placeholder.parsed("transfer_speed", String.valueOf(skyHopper.getTransferSpeed())));
        placeholders.add(Placeholder.parsed("suction_amount", String.valueOf(skyHopper.getSuctionAmount())));
        placeholders.add(Placeholder.parsed("suction_speed", String.valueOf(skyHopper.getSuctionSpeed())));
        placeholders.add(Placeholder.parsed("suction_range", String.valueOf(skyHopper.getSuctionRange())));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyHoppers.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, null, null, placeholders);
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        ItemStack itemStack = null;
        if(optionalItemStack.isPresent()) {
            itemStack = optionalItemStack.get();
        }

        if(itemStack == null) {
            skyHoppers.getComponentLogger().error(AdventureUtil.serialize("Failed to create the ItemStack for a SkyHopper."));
            return null;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        saveSkyHopperToPDC(skyHopper, itemMeta.getPersistentDataContainer());

        itemStack.setItemMeta(itemMeta);

        itemStack.setAmount(amount);

        return itemStack;
    }

    /**
     * Get a {@link SkyHopper} from a {@link PersistentDataContainer}.
     * @param location The {@link Location} of the {@link PersistentDataContainer} or null if an {@link ItemStack}'s {@link PersistentDataContainer}.
     * @param pdc The {@link PersistentDataContainer}.
     * @return A {@link SkyHopper} or null if {@link SkyHopper} creation failed.
     */
    public @Nullable SkyHopper getSkyHopperFromPDC(@Nullable Location location, @NotNull PersistentDataContainer pdc) {
        Locale locale = localeManager.getLocale();

        // Get the skyHoppers's settings
        @Nullable Settings settings = settingsManager.getSettings();
        if(settings == null) {
            skyHoppers.getComponentLogger().info(AdventureUtil.serialize(locale.prefix() + locale.failedSkyHopperLoad()));
            return null;
        }

        // Get the SkyHopper's settings from the PDC
        boolean enabled;
        boolean suctionParticles;
        FilterType inputFilterType;
        List<ItemType> filterItems = new ArrayList<>();
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
        if(hopperStatus == null) return null;

        // Set the SkyHopper's status
        enabled = hopperStatus == 1;

        // Set the suction particles status
        Integer particlesStatus = pdc.get(HopperKeys.PARTICLES.getKey(), PersistentDataType.INTEGER);
        if(particlesStatus != null) {
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
            if(modernMaterialNames != null) {
                modernMaterialNames.stream().map(Material::getMaterial).filter(Objects::nonNull).map(Material::asItemType).filter(Objects::nonNull).forEach(filterItems::add);
            }
        }

        // Then handle the legacy storage of the filter items.
        if(pdc.has(HopperKeys.FILTER_ITEMS.getKey(), PersistentDataType.STRING)) {
            String materialsString = pdc.get(HopperKeys.FILTER_ITEMS.getKey(), PersistentDataType.STRING);
            List<Material> legacyMaterials = PluginUtils.deserializeMaterials(materialsString);
            List<ItemType> itemTypes = legacyMaterials.stream().map(Material::asItemType).filter(Objects::nonNull).toList();
            filterItems.addAll(itemTypes);
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

            if(serializedLocation != null) {
                BlockState linkedBlockState = PluginUtils.deserializeLocation(serializedLocation).getBlock().getState(false);

                if (linkedBlockState instanceof Container linkedContainer) {
                    containers.add(new SkyContainer(linkedContainer.getLocation(), FilterType.NONE, new ArrayList<>()));
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
                        FilterType outputFilterType = FilterType.getType(linkedPDC.get(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING));

                        // Get the output filter item names.
                        List<String> filterItemNames = linkedPDC.get(HopperKeys.FILTER_ITEMS.getKey(),
                                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING));

                        // Parse the item names into ItemTypes.
                        List<ItemType> linkedContainerFilterItems = filterItemNames != null ? new ArrayList<>(filterItemNames.stream().map(Material::getMaterial).filter(Objects::nonNull).map(Material::asItemType).filter(Objects::nonNull).toList()) : new ArrayList<>();

                        // Create the SkyContainer and add it to the list
                        containers.add(new SkyContainer(linkedLocation, outputFilterType, linkedContainerFilterItems));
                    }
                });
            }
        }

        Double pdcTransferSpeed = pdc.get(HopperKeys.TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE);
        transferSpeed = Objects.requireNonNullElseGet(pdcTransferSpeed, () -> settings.skyHopperConfig().startingTransferSpeed());

        Double pdcMaxTransferSpeed = pdc.get(HopperKeys.MAX_TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE);
        maxTransferSpeed = Objects.requireNonNullElse(pdcMaxTransferSpeed, transferSpeed);

        Integer pdcTransferAmount = pdc.get(HopperKeys.TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER);
        transferAmount = Objects.requireNonNullElseGet(pdcTransferAmount, () -> settings.skyHopperConfig().startingTransferAmount());

        Integer pdcMaxTransferAmount = pdc.get(HopperKeys.MAX_TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER);
        maxTransferAmount = Objects.requireNonNullElse(pdcMaxTransferAmount, transferAmount);

        Double pdcSuctionSpeed = pdc.get(HopperKeys.SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE);
        suctionSpeed = Objects.requireNonNullElseGet(pdcSuctionSpeed, () -> settings.skyHopperConfig().startingSuctionSpeed());

        Double pdcMaxSuctionSpeed = pdc.get(HopperKeys.MAX_SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE);
        maxSuctionSpeed = Objects.requireNonNullElse(pdcMaxSuctionSpeed, suctionSpeed);

        Integer pdcSuctionAmount = pdc.get(HopperKeys.SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER);
        suctionAmount = Objects.requireNonNullElseGet(pdcSuctionAmount, () -> settings.skyHopperConfig().startingSuctionAmount());

        Integer pdcMaxSuctionAmount = pdc.get(HopperKeys.MAX_SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER);
        maxSuctionAmount = Objects.requireNonNullElse(pdcMaxSuctionAmount, suctionAmount);

        Integer pdcSuctionRange = pdc.get(HopperKeys.SUCTION_RANGE.getKey(), PersistentDataType.INTEGER);
        suctionRange = Objects.requireNonNullElseGet(pdcSuctionRange, () -> settings.skyHopperConfig().startingSuctionRange());

        Integer pdcMaxSuctionRange = pdc.get(HopperKeys.MAX_SUCTION_RANGE.getKey(), PersistentDataType.INTEGER);
        maxSuctionRange = Objects.requireNonNullElse(pdcMaxSuctionRange, suctionRange);

        Integer pdcMaxContainers = pdc.get(HopperKeys.MAX_CONTAINERS.getKey(), PersistentDataType.INTEGER);
        maxContainers = Objects.requireNonNullElseGet(pdcMaxContainers, () -> settings.skyHopperConfig().startingMaxContainers());

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
     * Saves a {@link SkyHopper} to the {@link PersistentDataContainer}.
     * @param skyHopper The {@link SkyHopper}.
     */
    public void saveSkyHopperToPDC(@NotNull SkyHopper skyHopper) {
        Location location = skyHopper.getLocation();
        if(location == null) {
            skyHoppers.getComponentLogger().warn(AdventureUtil.serialize("Unable to save SkyHopper to a Hopper's PDC due to a null location for the SkyHopper."));
            return;
        }

        if(location.getBlock().getState(false) instanceof Hopper hopper) {
            PersistentDataContainer persistentDataContainer = hopper.getPersistentDataContainer();

            saveSkyHopperToPDC(skyHopper, persistentDataContainer);

            hopper.update();
        } else {
            skyHoppers.getComponentLogger().warn(AdventureUtil.serialize("Unable to save SkyHopper to a Hopper's PDC as the block at the SkyHopper's location is not a Hopper."));
        }
    }

    /**
     * Saves a {@link SkyHopper} to a {@link PersistentDataContainer}.
     * @param skyHopper The {@link SkyHopper}.
     * @param pdc The {@link PersistentDataContainer} to save data to.
     */
    public void saveSkyHopperToPDC(@NotNull SkyHopper skyHopper, @NotNull PersistentDataContainer pdc) {
        pdc.set(HopperKeys.ENABLED.getKey(), PersistentDataType.INTEGER, skyHopper.isSkyHopperEnabled() ? 1 : 0);

        pdc.set(HopperKeys.PARTICLES.getKey(), PersistentDataType.INTEGER, skyHopper.isParticlesEnabled() ? 1 : 0);

        // Save the Skyhopper's owner
        if (skyHopper.getOwner() != null) {
            pdc.set(HopperKeys.OWNER.getKey(), DataType.UUID, skyHopper.getOwner());
        }

        // Save the SkyHopper's members
        pdc.set(HopperKeys.MEMBERS.getKey(), PersistentDataType.LIST.listTypeFrom(DataType.UUID), skyHopper.getMembers());

        // Save the input filter type
        pdc.set(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING, skyHopper.getFilterType().name());

        // Save the input filter items
        pdc.set(HopperKeys.FILTER_ITEMS.getKey(),
                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING),
                skyHopper.getFilterItems().stream().map(ItemType::toString).filter(Objects::nonNull)
                        .collect(Collectors.toList()));

        // Save the linked containers
        List<PersistentDataContainer> pdcList = new ArrayList<>();

        for (SkyContainer skyContainer : skyHopper.getLinkedContainers()) {
            Location linkedLocation = skyContainer.getLocation();

            FilterType filterType = skyContainer.getFilterType();
            PersistentDataContainer persistentDataContainer = pdc.getAdapterContext().newPersistentDataContainer();
            persistentDataContainer.set(HopperKeys.LOCATION.getKey(), DataType.LOCATION, linkedLocation);
            persistentDataContainer.set(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING, filterType.name());
            persistentDataContainer.set(HopperKeys.FILTER_ITEMS.getKey(),
                    PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING),
                    Objects.requireNonNull(skyContainer.getFilterItems()).stream().map(ItemType::toString).filter(Objects::nonNull)
                            .collect(Collectors.toList()));

            pdcList.add(persistentDataContainer);
        }

        pdc.set(HopperKeys.LINKS.getKey(), PersistentDataType.LIST.listTypeFrom(PersistentDataType.TAG_CONTAINER), pdcList);

        // Save the SkyHopper upgrades
        pdc.set(HopperKeys.TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.getTransferSpeed());

        pdc.set(HopperKeys.MAX_TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.getMaxTransferSpeed());

        pdc.set(HopperKeys.TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.getTransferAmount());

        pdc.set(HopperKeys.MAX_TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.getMaxTransferAmount());

        pdc.set(HopperKeys.SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.getSuctionSpeed());

        pdc.set(HopperKeys.MAX_SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.getMaxSuctionSpeed());

        pdc.set(HopperKeys.SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.getSuctionAmount());

        pdc.set(HopperKeys.MAX_SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.getMaxSuctionAmount());

        pdc.set(HopperKeys.SUCTION_RANGE.getKey(), PersistentDataType.INTEGER, skyHopper.getSuctionRange());

        pdc.set(HopperKeys.MAX_SUCTION_RANGE.getKey(), PersistentDataType.INTEGER, skyHopper.getMaxSuctionRange());

        pdc.set(HopperKeys.MAX_CONTAINERS.getKey(), PersistentDataType.INTEGER, skyHopper.getMaxContainers());
    }

    /**
     * Saves a {@link SkyHopper} to a {@link Hopper}'s {@link PersistentDataContainer}.
     * @param skyHopper The {@link SkyHopper} to save.
     * @param hopper The {@link Hopper} to get the {@link PersistentDataContainer} for.
     */
    public void saveSkyHopperToBlockPDC(@NotNull SkyHopper skyHopper, @NotNull Hopper hopper) {
        saveSkyHopperToPDC(skyHopper, hopper.getPersistentDataContainer());

        // Update the hopper block
        hopper.update();
    }

    /**
     * Check if the container broken is linked to any SkyHoppers and refresh any open GUIs for that SkyHopper.
     */
    public void handleContainerBroken(@NotNull Container container) {
        Location containerLocation = container.getLocation();

        // Loop through all loaded SkyHoppers
        skyHopperMap.forEach((location, skyHopper) -> {
            // Loop through the SkyHopper's Linked Containers
            skyHopper.getLinkedContainers().forEach(skyContainer -> {
                // Check if the broken container matches a linked container's location
                if(skyContainer.getLocation().equals(containerLocation)) {
                    // Close any output filter GUIs for the SkyContainer provided
                    guiManager.closeOutputFilterGUIs(location);

                    // Refresh any other open GUIs for the SkyHopper.
                    guiManager.refreshViewersGUI(location);
                }
            });
        });
    }

}