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
import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.record.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.hopper.SkyContainer;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.listener.HopperClickListener;
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
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * This class lets Players manage their linked containers.
 */
public class LinksGUI extends ChestGUI {
    private final SkyHoppers plugin;
    private final LocaleManager localeManager;
    private final GUIManager guiManager;
    private final HopperManager hopperManager;
    private final HopperClickListener hopperClickListener;
    private final HopperGUI hopperGUI;
    private final Location location;
    private final GUIConfig guiConfig;
    private final GUIType guiType;

    private int containerNum = 0;
    private int added = 0;

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin.
     * @param localeManager A LocaleManager instance.
     * @param guiManager A GUIManager instance.
     * @param hopperManager A HopperManager instance.
     * @param hopperClickListener A HopperClickListener instance.
     * @param hopperGUI The HopperGUI the Player came from.
     * @param location The Location of the SkyHopper.
     * @param player The Player viewing the GUI.
     */
    public LinksGUI(
            SkyHoppers plugin,
            LocaleManager localeManager,
            GUIManager guiManager,
            HopperManager hopperManager,
            HopperClickListener hopperClickListener,
            HopperGUI hopperGUI,
            Location location,
            Player player) {
        this.plugin = plugin;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.hopperManager = hopperManager;
        this.hopperClickListener = hopperClickListener;
        this.hopperGUI = hopperGUI;
        this.location = location;

        guiConfig = guiManager.getGuiConfig("links.yml");
        if(guiConfig == null) {
            throw new RuntimeException("Unable to find loaded config file links.yml.");
        }

        guiType = GUIType.getType(guiConfig.guiType());
        if(guiType == null) {
            throw new RuntimeException("Unknown GUIType " + guiConfig.guiType() + " in links.yml.");
        }

        if(guiConfig.name() == null) {
            throw new RuntimeException("GUI name is null in links.yml.");
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

        createFiller();
        createLinkedContainerButtons();
        createNextPageButton();
        createPreviousPageButton();
        createLinkButton();
        createExitButton();

        super.update();
    }

    /**
     * Refreshes the buttons in the GUI.
     */
    @Override
    public void refresh() {
        added = 0;
        containerNum = 0;

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

        hopperGUI.update();

        hopperGUI.open(plugin, player);
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

        hopperGUI.update();

        hopperGUI.open(plugin, player);
    }

    /**
     * Removes a linked container from the SkyHopper.
     * @param currentSkyHopper The SkyHopper associated with this GUI.
     * @param skyContainer The SkyContainer to remove.
     * @return The updated SkyHopper.
     */
    private @NotNull SkyHopper removeLinkedContainer(SkyHopper currentSkyHopper, SkyContainer skyContainer) {
        List<SkyContainer> containers = currentSkyHopper.containers();
        containers.removeIf(container -> container.location().equals(skyContainer.location()));

        return new SkyHopper(
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
    }

    /**
     * Creates all the Filler buttons.
     */
    private void createFiller() {
        GUIConfig.Filler filler = guiConfig.entries().filler();

        if (filler.item().material() != null) {
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

                for (int i = 0; i <= guiType.getSize() - 1; i++) {
                    setButton(i, button);
                }
            }
        }
    }

    /**
     * Creates all the buttons for the linked containers.
     */
    private void createLinkedContainerButtons() {
        SkyHopper skyHopper = hopperManager.getSkyHopper(location);
        if(skyHopper == null) return;

        GUIConfig.GenericEntry linkedItem = guiConfig.entries().linkedItem();

        int guiSize = guiType.getSize();
        int maxContainers = skyHopper.containers().size() - 1;

        if(guiSize - 10 >= 17) {
            List<ItemFlag> itemFlags = linkedItem.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

            for (int i = 0; i <= guiSize - 10; i++) {
                if(maxContainers >= containerNum) {
                    SkyContainer skyContainer = skyHopper.containers().get(containerNum);
                    Location linkedLocation = skyContainer.location();

                    GUIButton.Builder builder = new GUIButton.Builder();

                    ItemStack itemStack;
                    @Nullable Component containerName;
                    if(linkedLocation.getBlock().getState(false) instanceof Container container) {
                        itemStack = ItemStack.of(container.getType());
                        if(container.customName() != null) {
                            containerName = container.customName();
                        } else {
                            containerName = FormatUtil.format(FormatUtil.formatMaterialNameSentenceCase(container.getType().name()));
                        }
                    } else {
                        itemStack = ItemStack.of(Material.BARRIER);
                        containerName = FormatUtil.format("<red>Unknown Container</red>");
                    }

                    ItemMeta itemMeta = itemStack.getItemMeta();

                    itemMeta.displayName(containerName);

                    List<TagResolver.Single> placeholders = List.of(
                            Placeholder.parsed("x", String.valueOf(linkedLocation.getX())),
                            Placeholder.parsed("y", String.valueOf(linkedLocation.getY())),
                            Placeholder.parsed("z", String.valueOf(linkedLocation.getZ())));

                    List<Component> lore = linkedItem.item().lore().stream().map(line -> FormatUtil.format(line, placeholders)).toList();

                    itemMeta.lore(lore);
                    itemFlags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    builder.setItemStack(itemStack);

                    builder.setAction(event -> {
                        Player player = (Player) event.getWhoClicked();

                        switch (event.getClick()) {
                            case LEFT, SHIFT_LEFT -> {
                                plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                                hopperManager.removeViewer(location, player.getUniqueId());

                                new OutputFilterGUI(plugin, guiManager, hopperManager, skyContainer, this, location, player)
                                        .open(plugin, player);
                            }

                            case RIGHT, SHIFT_RIGHT -> {
                                SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);
                                if(currentSkyHopper == null) {
                                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                                            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
                                    return;
                                }

                                if(currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                                    SkyHopper updatedSkyHopper = removeLinkedContainer(currentSkyHopper, skyContainer);

                                    hopperManager.saveSkyHopperToBlockPDC(updatedSkyHopper, hopper);

                                    hopperManager.cacheSkyHopper(location, updatedSkyHopper);

                                    hopperManager.refreshViewersGUI(location);

                                    hopperManager.handleContainerRemoved(location);

                                    added = 0;
                                    containerNum = 0;
                                    update();
                                }
                            }
                        }
                    });

                    setButton(i, builder.build());

                    added++;
                    containerNum++;
                }
            }
        }
    }

    /**
     * Creates the next page button if needed
     */
    private void createNextPageButton() {
        int guiSize = guiType.getSize();

        if(added > guiSize - 1) {
            GUIConfig.GenericEntry nextPage = guiConfig.entries().nextPage();

            if (nextPage.item().material() != null) {
                Material nextPageMaterial = Material.getMaterial(nextPage.item().material());
                List<Component> lore = nextPage.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = nextPage.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                if (nextPageMaterial != null) {
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
    private void createPreviousPageButton() {
        int guiSize = guiType.getSize();

        if (containerNum > guiSize) {
            GUIConfig.GenericEntry previousPage = guiConfig.entries().previousPage();

            if (previousPage.item().material() != null) {
                Material previousPageMaterial = Material.getMaterial(previousPage.item().material());
                List<Component> lore = previousPage.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = previousPage.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                if (previousPageMaterial != null) {
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
                        containerNum -= (guiSize + added);
                        added = 0;
                        update();
                    });

                    setButton(previousPage.slot(), builder.build());
                }
            }
        }
    }

    /**
     * Creates the button to link and or unlink containers.
     */
    private void createLinkButton() {
        Locale locale = localeManager.getLocale();

        GUIConfig.GenericEntry link = guiConfig.entries().link();

        if (link.item().material() != null) {
            Material linkMaterial = Material.getMaterial(link.item().material());

            if (linkMaterial != null) {
                List<Component> lore = link.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = link.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(linkMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = link.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);
                    hopperManager.removeViewer(location, player.getUniqueId());

                    SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);
                    if (currentSkyHopper == null) return;

                    if (currentSkyHopper.containers().size() != currentSkyHopper.maxContainers()) {
                        if (currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper) {
                            hopperClickListener.addLinkingPlayer(player, currentSkyHopper.location());

                            player.sendMessage(FormatUtil.format(locale.prefix() + locale.linkingEnabled()));
                            player.sendMessage(FormatUtil.format(locale.prefix() + locale.linkingHowToExit()));
                        }
                    } else {
                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.containerLinksMaxed()));
                    }
                });

                setButton(link.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the Exit button.
     */
    private void createExitButton() {
        GUIConfig.GenericEntry exit = guiConfig.entries().exit();

        if (exit.item().material() != null) {
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

                    close(plugin, player);
                });

                setButton(exit.slot(), builder.build());
            }
        }
    }
}
