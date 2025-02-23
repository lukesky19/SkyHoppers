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
package com.github.lukesky19.skyHoppers.gui;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.manager.GUIManager;
import com.github.lukesky19.skyHoppers.config.record.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.hopper.SkyContainer;
import com.github.lukesky19.skyHoppers.hopper.FilterType;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.gui.GUIType;
import com.github.lukesky19.skylib.gui.GUIButton;
import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class lets Players manage the output filter items and filter type.
 */
public class OutputFilterGUI extends ChestGUI {
    private final SkyHoppers plugin;
    private final HopperManager hopperManager;
    private final LinksGUI linksGUI;
    private final Location location;
    private final SkyContainer skyContainer;
    private final GUIConfig guiConfig;
    private final GUIType guiType;

    private int itemNum = 0;
    private int added = 0;

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin
     * @param guiManager A GUIManager instance.
     * @param hopperManager A HopperManager instance.
     * @param skyContainer The SkyContainer to show the output filter for.
     * @param linksGUI The LinksGUI the player came from.
     * @param location The Location of the SkyHopper.
     * @param player The Player viewing the GUI.
     */
    public OutputFilterGUI(
            SkyHoppers plugin,
            GUIManager guiManager,
            HopperManager hopperManager,
            SkyContainer skyContainer,
            LinksGUI linksGUI,
            Location location,
            Player player) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
        this.linksGUI = linksGUI;
        this.location = location;
        this.skyContainer = skyContainer;

        guiConfig = guiManager.getGuiConfig("output_filter.yml");
        if(guiConfig == null) {
            throw new RuntimeException("Unable to find loaded config file output_filter.yml.");
        }

        guiType = GUIType.getType(guiConfig.guiType());
        if(guiType == null) {
            throw new RuntimeException("Unknown GUIType " + guiConfig.guiType() + " in output_filter.yml.");
        }

        if(guiConfig.name() == null) {
            throw new RuntimeException("GUI name is null in output_filter.yml.");
        }

        create(player, guiType, guiConfig.name(), null);

        update();
    }

    /**
     * Opens this GUI for the Player.
     * @param plugin The SkyHoppers Plugin.
     * @param player The Player to open the GUI for.
     */
    @Override
    public void open(@NotNull Plugin plugin, @NotNull Player player) {
        super.open(plugin, player);

        hopperManager.addViewer(location, player.getUniqueId(), this);
    }

    /**
     * Creates and populates the buttons for this GUI.
     */
    @Override
    public void update() {
        clearButtons();

        SkyHopper skyHopper = hopperManager.getSkyHopper(location);
        if(skyHopper == null) return;

        int guiSize = guiType.getSize();
        int maxItems = skyContainer.filterItems().size() - 1;

        createFiller(guiSize);
        createFilterItems(guiSize, maxItems);
        createNextPageButton(guiSize, maxItems);
        createPreviousPageButton(guiSize);
        createFilterTypeButton();
        createExitButton();

        super.update();
    }

    /**
     * Refreshes the buttons in the GUI.
     */
    @Override
    public void refresh() {
        added = 0;
        itemNum = 0;

        update();
    }

    /**
     * Closes the current GUI and re-opens the HopperGUI.
     * @param plugin The SkyHoppers plugin.
     * @param player The Player to close the GUI for.
     */
    @Override
    public void close(@NotNull Plugin plugin, @NotNull Player player) {
        UUID uuid = player.getUniqueId();

        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

        hopperManager.removeViewer(location, uuid);

        linksGUI.refresh();

        linksGUI.open(plugin, player);
    }

    /**
     * Completely closes the GUI and doesn't re-open the HopperGUI.
     * @param plugin The SkyHoppers Plugin.
     * @param player The Player to unload the GUI for.
     * @param onDisable Whether the unload is occurring on plugin disable or not.
     */
    @Override
    public void unload(@NotNull Plugin plugin, @NotNull Player player, boolean onDisable) {
        if(onDisable) {
            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
        }

        hopperManager.removeViewer(location, player.getUniqueId());
    }

    /**
     * Handles when the player closes the GUI.
     * @param inventoryCloseEvent An InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        Player player = (Player) inventoryCloseEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        hopperManager.removeViewer(location, uuid);

        linksGUI.refresh();

        linksGUI.open(plugin, player);

        hopperManager.addViewer(location, uuid, linksGUI);
    }

    /**
     * Handles when a Player clicks an Item inside their inventory to add to the filter.
     * @param inventoryClickEvent An InventoryClickEvent
     */
    @Override
    public void handleBottomClick(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);
        if(currentSkyHopper == null) return;

        List<SkyContainer> containers = currentSkyHopper.containers();
        int index = containers.indexOf(skyContainer);

        if(index != -1) {
            ItemStack item = inventoryClickEvent.getCurrentItem();
            if (item != null && item.getType() != Material.AIR) {
                if (!skyContainer.filterItems().contains(item.getType())) {
                    if (currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                        skyContainer.addFilterItem(item.getType());

                        containers.set(index, skyContainer);

                        SkyHopper updatedSkyHopper = new SkyHopper(
                                currentSkyHopper.enabled(),
                                currentSkyHopper.particles(),
                                currentSkyHopper.owner(),
                                currentSkyHopper.members(),
                                currentSkyHopper.location(),
                                containers,
                                currentSkyHopper.filterType(),
                                currentSkyHopper.filterItems(),
                                currentSkyHopper.transferSpeed(),
                                currentSkyHopper.maxTransferSpeed(),
                                currentSkyHopper.transferAmount(),
                                currentSkyHopper.maxTransferAmount(),
                                currentSkyHopper.suctionSpeed(),
                                currentSkyHopper.maxSuctionSpeed(),
                                currentSkyHopper.suctionAmount(),
                                currentSkyHopper.maxSuctionAmount(),
                                currentSkyHopper.suctionRange(),
                                currentSkyHopper.maxSuctionRange(),
                                currentSkyHopper.maxContainers(),
                                currentSkyHopper.nextSuctionTime(),
                                currentSkyHopper.nextTransferTime());

                        hopperManager.saveSkyHopperToBlockPDC(updatedSkyHopper, hopper);

                        hopperManager.cacheSkyHopper(location, updatedSkyHopper);

                        hopperManager.refreshViewersGUI(location);

                        added = 0;
                        itemNum = 0;

                        update();
                    }
                }
            }
        } else {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            UUID uuid = player.getUniqueId();

            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

            hopperManager.removeViewer(location, uuid);

            linksGUI.update();

            linksGUI.open(plugin, player);

            hopperManager.addViewer(location, uuid, linksGUI);
        }
    }

    /**
     * Creates all the Filler buttons.
     */
    private void createFiller(int guiSize) {
        GUIConfig.Filler filler = guiConfig.entries().filler();

        if(filler.item().material() != null) {
            Material fillerMaterial = Material.getMaterial(filler.item().material());

            if (fillerMaterial != null) {
                List<Component> lore = filler.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = filler.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(fillerMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = filler.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                GUIButton button = builder.build();

                for (int i = 0; i <= guiSize - 1; i++) {
                    setButton(i, button);
                }
            }
        }
    }

    /**
     * Creates all the buttons for items in the filtered items list
     */
    private void createFilterItems(int guiSize, int maxItems) {
        if(guiSize - 10 >= 17) {
            GUIConfig.GenericEntry filterItem = guiConfig.entries().filterItem();
            List<Component> lore = filterItem.item().lore().stream().map(FormatUtil::format).toList();
            List<ItemFlag> itemFlags = filterItem.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

            for (int i = 0; i <= guiSize - 10; i++) {
                if (maxItems >= itemNum) {
                    Material material = skyContainer.filterItems().get(itemNum);

                    GUIButton.Builder builder = new GUIButton.Builder();

                    ItemStack itemStack = ItemStack.of(material);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    itemMeta.displayName(FormatUtil.format(FormatUtil.formatMaterialNameSentenceCase(material.name())));

                    itemMeta.lore(lore);
                    itemFlags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    builder.setItemStack(itemStack);

                    builder.setAction(event -> {
                        ItemStack item = event.getCurrentItem();
                        if (item != null) {
                            SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);

                            if(currentSkyHopper != null && currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                                List<SkyContainer> containers = currentSkyHopper.containers();
                                int index = containers.indexOf(skyContainer);

                                skyContainer.removeFilterItem(item.getType());

                                containers.set(index, skyContainer);

                                SkyHopper updatedSkyHopper = new SkyHopper(
                                        currentSkyHopper.enabled(),
                                        currentSkyHopper.particles(),
                                        currentSkyHopper.owner(),
                                        currentSkyHopper.members(),
                                        currentSkyHopper.location(),
                                        containers,
                                        currentSkyHopper.filterType(),
                                        currentSkyHopper.filterItems(),
                                        currentSkyHopper.transferSpeed(),
                                        currentSkyHopper.maxTransferSpeed(),
                                        currentSkyHopper.transferAmount(),
                                        currentSkyHopper.maxTransferAmount(),
                                        currentSkyHopper.suctionSpeed(),
                                        currentSkyHopper.maxSuctionSpeed(),
                                        currentSkyHopper.suctionAmount(),
                                        currentSkyHopper.maxSuctionAmount(),
                                        currentSkyHopper.suctionRange(),
                                        currentSkyHopper.maxSuctionRange(),
                                        currentSkyHopper.maxContainers(),
                                        currentSkyHopper.nextSuctionTime(),
                                        currentSkyHopper.nextTransferTime());

                                hopperManager.saveSkyHopperToBlockPDC(updatedSkyHopper, hopper);

                                hopperManager.cacheSkyHopper(location, updatedSkyHopper);

                                hopperManager.refreshViewersGUI(location);

                                added = 0;
                                itemNum = 0;
                                update();
                            }
                        }
                    });

                    setButton(i, builder.build());

                    itemNum++;
                    added++;
                }
            }
        }
    }

    /**
     * Creates the next page button if needed
     */
    private void createNextPageButton(int guiSize, int maxItems) {
        if(added > guiSize - 10 && (itemNum - 1) < maxItems) {
            GUIConfig.GenericEntry nextPage = guiConfig.entries().nextPage();

            if (nextPage.item().material() != null) {
                Material nextPageMaterial = Material.getMaterial(nextPage.item().material());

                if (nextPageMaterial != null) {
                    List<Component> lore = nextPage.item().lore().stream().map(FormatUtil::format).toList();
                    List<ItemFlag> itemFlags = nextPage.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                    GUIButton.Builder builder = new GUIButton.Builder();

                    ItemStack itemStack = ItemStack.of(nextPageMaterial);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    String name = nextPage.item().name();
                    if (name != null) {
                        itemMeta.displayName(FormatUtil.format(name));
                    }

                    itemMeta.lore(lore);
                    itemFlags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    builder.setItemStack(itemStack);

                    builder.setAction(event -> {
                        added = 0;
                        update();
                    });

                    setButton(nextPage.slot(), builder.build());
                }
            }
        }
    }

    /**
     * Creates the previous page button if needed
     */
    private void createPreviousPageButton(int guiSize) {
        if(itemNum > guiSize - 9) {
            GUIConfig.GenericEntry previousPage = guiConfig.entries().previousPage();

            if (previousPage.item().material() != null) {
                Material previousPageMaterial = Material.getMaterial(previousPage.item().material());

                if (previousPageMaterial != null) {
                    List<Component> lore = previousPage.item().lore().stream().map(FormatUtil::format).toList();
                    List<ItemFlag> itemFlags = previousPage.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                    GUIButton.Builder builder = new GUIButton.Builder();

                    ItemStack itemStack = ItemStack.of(previousPageMaterial);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    String name = previousPage.item().name();
                    if (name != null) {
                        itemMeta.displayName(FormatUtil.format(name));
                    }

                    itemMeta.lore(lore);
                    itemFlags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    builder.setItemStack(itemStack);

                    builder.setAction(event -> {
                        if (itemNum > (guiSize - 9) + added) {
                            itemNum -= (guiSize - 9) + added;
                        } else {
                            itemNum -= added;
                        }

                        added = 0;
                        update();
                    });

                    setButton(previousPage.slot(), builder.build());
                }
            }
        }
    }

    /**
     * Creates the filter type button
     */
    private void createFilterTypeButton() {
        GUIConfig.GenericEntry filter = guiConfig.entries().filter();

        if (filter.item().material() != null) {
            Material filterMaterial = Material.getMaterial(filter.item().material());

            if (filterMaterial != null) {
                List<TagResolver.Single> placeholders = new ArrayList<>();
                placeholders.add(Placeholder.parsed("filter_type", skyContainer.filterType().name()));

                List<Component> lore = filter.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = filter.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(filterMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = filter.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name, placeholders));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);

                    if (currentSkyHopper != null && currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                        List<SkyContainer> containers = getUpdatedSkyContainersList(currentSkyHopper);

                        SkyHopper updatedSkyHopper = new SkyHopper(
                                currentSkyHopper.enabled(),
                                currentSkyHopper.particles(),
                                currentSkyHopper.owner(),
                                currentSkyHopper.members(),
                                currentSkyHopper.location(),
                                containers,
                                currentSkyHopper.filterType(),
                                currentSkyHopper.filterItems(),
                                currentSkyHopper.transferSpeed(),
                                currentSkyHopper.maxTransferSpeed(),
                                currentSkyHopper.transferAmount(),
                                currentSkyHopper.maxTransferAmount(),
                                currentSkyHopper.suctionSpeed(),
                                currentSkyHopper.maxSuctionSpeed(),
                                currentSkyHopper.suctionAmount(),
                                currentSkyHopper.maxSuctionAmount(),
                                currentSkyHopper.suctionRange(),
                                currentSkyHopper.maxSuctionRange(),
                                currentSkyHopper.maxContainers(),
                                currentSkyHopper.nextSuctionTime(),
                                currentSkyHopper.nextTransferTime());

                        hopperManager.saveSkyHopperToBlockPDC(updatedSkyHopper, hopper);

                        hopperManager.cacheSkyHopper(location, updatedSkyHopper);

                        hopperManager.refreshViewersGUI(location);

                        added = 0;
                        itemNum = 0;

                        update();
                    }
                });

                setButton(filter.slot(), builder.build());
            }
        }
    }

    private void createExitButton() {
        GUIConfig.GenericEntry exit = guiConfig.entries().exit();

        if(exit.item().material() != null) {
            Material exitMaterial = Material.getMaterial(exit.item().material());

            if (exitMaterial != null) {
                List<Component> lore = exit.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = exit.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(exitMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = exit.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();
                    UUID uuid = player.getUniqueId();

                    plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    hopperManager.removeViewer(location, uuid);

                    linksGUI.refresh();

                    linksGUI.open(plugin, player);

                    hopperManager.addViewer(location, uuid, linksGUI);
                });

                setButton(exit.slot(), builder.build());
            }
        }
    }

    /**
     * Gets the List of updated linked containers (SkyContainers) after updating the FilterType.
     * @param currentSkyHopper The SkyHopper associated with this GUI.
     * @return A List of SkyContainers.
     */
    private @NotNull List<SkyContainer> getUpdatedSkyContainersList(SkyHopper currentSkyHopper) {
        List<SkyContainer> containers = currentSkyHopper.containers();
        int index = containers.indexOf(skyContainer);

        switch (skyContainer.filterType()) {
            case NONE -> {
                SkyContainer newSkyContainer = new SkyContainer(skyContainer.location(), FilterType.WHITELIST, skyContainer.filterItems());
                containers.set(index, newSkyContainer);
            }

            case WHITELIST -> {
                SkyContainer newSkyContainer = new SkyContainer(skyContainer.location(), FilterType.BLACKLIST, skyContainer.filterItems());
                containers.set(index, newSkyContainer);
            }

            case BLACKLIST -> {
                SkyContainer newSkyContainer = new SkyContainer(skyContainer.location(), FilterType.DESTROY, skyContainer.filterItems());
                containers.set(index, newSkyContainer);
            }

            case DESTROY -> {
                SkyContainer newSkyContainer = new SkyContainer(skyContainer.location(), FilterType.NONE, skyContainer.filterItems());
                containers.set(index, newSkyContainer);
            }
        }
        return containers;
    }
}
