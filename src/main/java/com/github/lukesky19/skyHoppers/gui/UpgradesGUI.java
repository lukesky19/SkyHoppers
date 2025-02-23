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
import com.github.lukesky19.skyHoppers.config.manager.SettingsManager;
import com.github.lukesky19.skyHoppers.config.record.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.config.record.Settings;
import com.github.lukesky19.skyHoppers.gui.upgrades.*;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.gui.GUIType;
import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Lets players manage the SkyHoppers upgrades.
 */
public class UpgradesGUI extends ChestGUI {
    private final SkyHoppers plugin;
    private final SettingsManager settingsManager;
    private final LocaleManager localeManager;
    private final GUIManager guiManager;
    private final HopperManager hopperManager;
    private final HopperGUI hopperGUI;
    private final Location location;
    private final GUIConfig guiConfig;
    private final GUIType guiType;

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin.
     * @param settingsManager A SettingsManager instance.
     * @param localeManager A LocaleManager instance.
     * @param guiManager A GUIManager instance.
     * @param hopperManager A HopperManager instance.
     * @param hopperGUI The HopperGUI the Player came from.
     * @param location The Location of the SkyHopper.
     * @param player The Player viewing the GUI.
     */
    public UpgradesGUI(
            SkyHoppers plugin,
            SettingsManager settingsManager,
            LocaleManager localeManager,
            GUIManager guiManager,
            HopperManager hopperManager,
            HopperGUI hopperGUI,
            Location location,
            Player player) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.hopperManager = hopperManager;
        this.hopperGUI = hopperGUI;
        this.location = location;

        guiConfig = guiManager.getGuiConfig("upgrades.yml");
        if (guiConfig == null) {
            throw new RuntimeException("Unable to find loaded config file hopper.yml.");
        }

        guiType = GUIType.getType(guiConfig.guiType());
        if (guiType == null) {
            throw new RuntimeException("Unknown GUIType " + guiConfig.guiType() + " in hopper.yml");
        }

        if (guiConfig.name() == null) {
            throw new RuntimeException("GUI name is null in hopper.yml.");
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
        final Settings settings = settingsManager.getSettings();
        if (settings == null) return;

        clearButtons();

        // Filler
        createFiller();

        // Suction Speed
        createSuctionSpeedButton();

        // Suction Amount
        createSuctionAmountButton();

        // Suction Range
        createSuctionRangeButton();

        // Number of Links
        createLinksButton();

        // Transfer Speed
        createTransferSpeedButton();

        // Transfer Amount
        createTransferAmountButton();

        // Exit Button
        createExitButton();

        super.update();
    }

    /**
     * Refreshes the buttons in the GUI.
     */
    @Override
    public void refresh() {
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
        if (onDisable) {
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
    public void handleClose(InventoryCloseEvent inventoryCloseEvent) {
        if (inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW))
            return;

        Player player = (Player) inventoryCloseEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        hopperManager.removeViewer(location, uuid);

        hopperGUI.update();

        hopperGUI.open(plugin, player);

        hopperManager.addViewer(location, uuid, hopperGUI);
    }

    /**
     * Creates all the Filler buttons.
     */
    private void createFiller() {
        int guiSize = guiType.getSize();
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

                for (int i = 0; i <= guiSize - 1; i++) {
                    setButton(i, button);
                }
            }
        }
    }

    /**
     * Creates the button to access the suction speed upgrade GUI.
     */
    private void createSuctionSpeedButton() {
        GUIConfig.GenericEntry suctionSpeedEntry = guiConfig.entries().suctionSpeed();

        if (suctionSpeedEntry.item().material() != null) {
            Material suctionSpeedMaterial = Material.getMaterial(suctionSpeedEntry.item().material());

            if (suctionSpeedMaterial != null) {
                List<Component> lore = suctionSpeedEntry.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = suctionSpeedEntry.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(suctionSpeedMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = suctionSpeedEntry.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();

                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    new SuctionSpeedUpgradeGUI(plugin, settingsManager, localeManager, guiManager, hopperManager, this, location, player).open(plugin, player);
                });

                setButton(suctionSpeedEntry.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the button to access the suction amount upgrade GUI.
     */
    private void createSuctionAmountButton() {
        GUIConfig.GenericEntry suctionAmountEntry = guiConfig.entries().suctionAmount();

        if (suctionAmountEntry.item().material() != null) {
            Material suctionAmountMaterial = Material.getMaterial(suctionAmountEntry.item().material());

            if (suctionAmountMaterial != null) {
                List<Component> lore = suctionAmountEntry.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = suctionAmountEntry.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(suctionAmountMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = suctionAmountEntry.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();

                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    new SuctionAmountUpgradeGUI(plugin, settingsManager, localeManager, guiManager, hopperManager, this, location, player).open(plugin, player);
                });

                setButton(suctionAmountEntry.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the button to access the suction range upgrade GUI.
     */
    private void createSuctionRangeButton() {
        GUIConfig.GenericEntry suctionRangeEntry = guiConfig.entries().suctionRange();

        if (suctionRangeEntry.item().material() != null) {
            Material suctionRangeMaterial = Material.getMaterial(suctionRangeEntry.item().material());

            if (suctionRangeMaterial != null) {
                List<Component> lore = suctionRangeEntry.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = suctionRangeEntry.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(suctionRangeMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = suctionRangeEntry.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();

                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    new SuctionRangeUpgradeGUI(plugin, settingsManager, localeManager, guiManager, hopperManager, this, location, player).open(plugin, player);
                });

                setButton(suctionRangeEntry.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the button to access the links upgrade GUI.
     */
    private void createLinksButton() {
        GUIConfig.GenericEntry linkedContainersEntry = guiConfig.entries().maxLinks();

        if (linkedContainersEntry.item().material() != null) {
            Material linkedContainersMaterial = Material.getMaterial(linkedContainersEntry.item().material());

            if (linkedContainersMaterial != null) {
                List<Component> lore = linkedContainersEntry.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = linkedContainersEntry.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(linkedContainersMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = linkedContainersEntry.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();

                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    new LinksUpgradeGUI(plugin, settingsManager, localeManager, guiManager, hopperManager, this, location, player).open(plugin, player);
                });

                setButton(linkedContainersEntry.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the button to access the transfer amount upgrade GUI.
     */
    private void createTransferAmountButton() {
        GUIConfig.GenericEntry transferSpeedEntry = guiConfig.entries().transferSpeed();

        if (transferSpeedEntry.item().material() != null) {
            Material transferSpeedMaterial = Material.getMaterial(transferSpeedEntry.item().material());

            if (transferSpeedMaterial != null) {
                List<Component> lore = transferSpeedEntry.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = transferSpeedEntry.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(transferSpeedMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = transferSpeedEntry.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();

                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    new TransferSpeedUpgradeGUI(plugin, settingsManager, localeManager, guiManager, hopperManager, this, location, player).open(plugin, player);
                });

                setButton(transferSpeedEntry.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the button to access the transfer speed upgrade GUI.
     */
    private void createTransferSpeedButton() {
        GUIConfig.GenericEntry transferAmountEntry = guiConfig.entries().transferAmount();

        if (transferAmountEntry.item().material() != null) {
            Material transferAmountMaterial = Material.getMaterial(transferAmountEntry.item().material());

            if (transferAmountMaterial != null) {
                List<Component> lore = transferAmountEntry.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = transferAmountEntry.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(transferAmountMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = transferAmountEntry.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);

                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();

                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    new TransferAmountUpgradeGUI(plugin, settingsManager, localeManager, guiManager, hopperManager, this, location, player).open(plugin, player);
                });

                setButton(transferAmountEntry.slot(), builder.build());
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

                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    hopperManager.removeViewer(location, player.getUniqueId());

                    hopperGUI.update();

                    hopperGUI.open(plugin, player);
                });

                setButton(exit.slot(), builder.build());
            }
        }
    }
}
