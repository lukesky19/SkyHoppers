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
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.listener.HopperClickListener;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skyHoppers.task.HopperViewTask;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * This class lets Players manage a SkyHopper's settings.
 */
public class HopperGUI extends ChestGUI {
    private final SkyHoppers plugin;
    private final SettingsManager settingsManager;
    private final LocaleManager localeManager;
    private final GUIManager guiManager;
    private final HopperManager hopperManager;
    private final HopperClickListener hopperClickListener;
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
     * @param hopperClickListener A HopperClickListener instance.
     * @param location The Location of the SkyHopper.
     * @param player The Player viewing the SkyHopper's settings.
     */
    public HopperGUI(
            SkyHoppers plugin,
            SettingsManager settingsManager,
            LocaleManager localeManager,
            GUIManager guiManager,
            HopperManager hopperManager,
            HopperClickListener hopperClickListener,
            Location location,
            Player player) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.hopperManager = hopperManager;
        this.hopperClickListener = hopperClickListener;
        this.location = location;

        guiConfig = guiManager.getGuiConfig("hopper.yml");
        if(guiConfig == null) {
            throw new RuntimeException("Unable to find loaded config file hopper.yml.");
        }

        guiType = GUIType.getType(guiConfig.guiType());
        if(guiType == null) {
            throw new RuntimeException("Unknown GUIType " + guiConfig.guiType() + " in hopper.yml");
        }

        if(guiConfig.name() == null) {
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
        clearButtons();

        SkyHopper skyHopper = hopperManager.getSkyHopper(location);
        if(skyHopper == null) return;

        // Filler
        createFiller(guiType.getSize());

        // SkyHopper Status Enabled/Disabled Buttons
        if(skyHopper.enabled()) {
            createStatusEnabledButton();
        } else {
            createStatusDisabledButton();
        }

        // Particles Enabled/Disabled Buttons
        if(skyHopper.particles()) {
            createParticlesEnabledButton();
        } else {
            createParticlesDisabledButton();
        }

        // Linked Containers Button
        createLinkedContainersButton();

        // Input Filter Button
        createInputFilterButton();

        // Upgrades Button
        createUpgradesButton();

        // Members
        createMembersButton();

        // Visualize
        createVisualizeButton();

        // Exit Button
        createExitButton();

        // Info Button
        createInfoButton(skyHopper);

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
     * Closes the current GUI.
     * @param plugin The SkyHoppers plugin.
     * @param player The Player to close the GUI for.
     */
    @Override
    public void close(@NotNull Plugin plugin, @NotNull Player player) {
        UUID uuid = player.getUniqueId();

        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

        hopperManager.removeViewer(location, uuid);
    }

    /**
     * Completely closes the GUI.
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
     * Creates the Status Enabled Button.
     */
    private void createStatusEnabledButton() {
        GUIConfig.GenericEntry enabled = guiConfig.entries().hopperEnabled();
        
        if(enabled.item().material() != null) {
            Material enabledMaterial = Material.getMaterial(enabled.item().material());

            if (enabledMaterial != null) {
                List<Component> lore = enabled.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = enabled.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(enabledMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = enabled.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);
                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);

                    if (currentSkyHopper != null && currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                        SkyHopper updatedSkyHopper = new SkyHopper(
                                !currentSkyHopper.enabled(),
                                currentSkyHopper.particles(),
                                currentSkyHopper.owner(),
                                currentSkyHopper.members(),
                                currentSkyHopper.location(),
                                currentSkyHopper.containers(),
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

                        update();
                    }
                });

                setButton(enabled.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the Status Disabled Button.
     */
    private void createStatusDisabledButton() {
        GUIConfig.GenericEntry disabled = guiConfig.entries().hopperDisabled();

        if (disabled.item().material() != null) {
            Material disabledMaterial = Material.getMaterial(disabled.item().material());

            if (disabledMaterial != null) {
                List<Component> lore = disabled.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = disabled.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(disabledMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = disabled.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);
                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);

                    if (currentSkyHopper != null && currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                        SkyHopper updatedSkyHopper = new SkyHopper(
                                !currentSkyHopper.enabled(),
                                currentSkyHopper.particles(),
                                currentSkyHopper.owner(),
                                currentSkyHopper.members(),
                                currentSkyHopper.location(),
                                currentSkyHopper.containers(),
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

                        update();
                    }
                });

                setButton(disabled.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the Particles Enabled Button.
     */
    private void createParticlesEnabledButton() {
        GUIConfig.GenericEntry enabled = guiConfig.entries().particlesEnabled();

        if(enabled.item().material() != null) {
            Material enabledMaterial = Material.getMaterial(enabled.item().material());

            if (enabledMaterial != null) {
                List<Component> lore = enabled.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = enabled.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(enabledMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = enabled.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);
                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);

                    if (currentSkyHopper != null && currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                        SkyHopper updatedSkyHopper = new SkyHopper(
                                currentSkyHopper.enabled(),
                                !currentSkyHopper.particles(),
                                currentSkyHopper.owner(),
                                currentSkyHopper.members(),
                                currentSkyHopper.location(),
                                currentSkyHopper.containers(),
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

                        update();
                    }
                });

                setButton(enabled.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the Particles Disabled Button.
     */
    private void createParticlesDisabledButton() {
        GUIConfig.GenericEntry disabled = guiConfig.entries().particlesDisabled();
        if (disabled.item().material() != null) {
            Material disabledMaterial = Material.getMaterial(disabled.item().material());

            if (disabledMaterial != null) {
                List<Component> lore = disabled.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = disabled.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(disabledMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = disabled.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);
                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    SkyHopper currentSkyHopper = hopperManager.getSkyHopper(location);

                    if (currentSkyHopper != null && currentSkyHopper.location() != null && currentSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                        SkyHopper updatedSkyHopper = new SkyHopper(
                                currentSkyHopper.enabled(),
                                !currentSkyHopper.particles(),
                                currentSkyHopper.owner(),
                                currentSkyHopper.members(),
                                currentSkyHopper.location(),
                                currentSkyHopper.containers(),
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

                        update();
                    }
                });

                setButton(disabled.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the Linked Containers Button.
     */
    private void createLinkedContainersButton() {
        GUIConfig.GenericEntry links = guiConfig.entries().link();
        if (links.item().material() != null) {
            Material linksMaterial = Material.getMaterial(links.item().material());

            if (linksMaterial != null) {
                List<Component> lore = links.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = links.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(linksMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = links.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);
                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    Player player = (Player) event.getWhoClicked();

                    plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                    new LinksGUI(plugin, localeManager, guiManager, hopperManager, hopperClickListener, this, location, player).open(plugin, player);
                });

                setButton(links.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the Input Filter Button.
     */
    private void createInputFilterButton() {
        GUIConfig.GenericEntry inputFilter = guiConfig.entries().filter();
        if (inputFilter.item().material() != null) {
            Material inputFilterMaterial = Material.getMaterial(inputFilter.item().material());

            if (inputFilterMaterial != null) {
                List<Component> lore = inputFilter.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = inputFilter.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(inputFilterMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = inputFilter.item().name();
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

                    new InputFilterGUI(plugin, guiManager, hopperManager, this, location, player).open(plugin, player);
                });

                setButton(inputFilter.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the Upgrades button.
     */
    private void createUpgradesButton() {
        GUIConfig.GenericEntry upgrades = guiConfig.entries().upgrades();

        if (upgrades.item().material() != null) {
            Material upgradesMaterial = Material.getMaterial(upgrades.item().material());

            if (upgradesMaterial != null) {
                List<Component> lore = upgrades.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = upgrades.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(upgradesMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = upgrades.item().name();
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

                    new UpgradesGUI(plugin, settingsManager, localeManager, guiManager, hopperManager, this, location, player).open(plugin, player);
                });

                setButton(upgrades.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the Members button.
     */
    private void createMembersButton() {
        GUIConfig.GenericEntry members = guiConfig.entries().members();
        if (members.item().material() != null) {
            Material membersMaterial = Material.getMaterial(members.item().material());

            if (membersMaterial != null) {
                List<Component> lore = members.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = members.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(membersMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = members.item().name();
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

                    new MembersGUI(plugin, guiManager, hopperManager, this, location, player).open(plugin, player);
                });

                setButton(members.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the visualize button.
     */
    private void createVisualizeButton() {
        GUIConfig.GenericEntry visualize = guiConfig.entries().visualize();

        if (visualize.item().material() != null) {
            Material visualizeMaterial = Material.getMaterial(visualize.item().material());

            if (visualizeMaterial != null) {
                List<Component> lore = visualize.item().lore().stream().map(FormatUtil::format).toList();
                List<ItemFlag> itemFlags = visualize.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

                GUIButton.Builder builder = new GUIButton.Builder();

                ItemStack itemStack = ItemStack.of(visualizeMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();

                String name = visualize.item().name();
                if (name != null) {
                    itemMeta.displayName(FormatUtil.format(name));
                }

                itemMeta.lore(lore);
                itemFlags.forEach(itemMeta::addItemFlags);

                itemStack.setItemMeta(itemMeta);
                builder.setItemStack(itemStack);

                builder.setAction(event -> {
                    SkyHopper skyHopper = hopperManager.getSkyHopper(location);

                    if (skyHopper != null && skyHopper.location() != null) {
                        Player player = (Player) event.getWhoClicked();

                        Bukkit.getScheduler().runTaskLater(plugin, () ->
                                player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);

                        hopperManager.removeViewer(location, player.getUniqueId());

                        new HopperViewTask(hopperManager, location, player).runTaskTimerAsynchronously(plugin, 0L, 1L);
                    }
                });

                setButton(visualize.slot(), builder.build());
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

    /**
     * Creates the info button.
     * @param skyHopper The SkyHopper associated with this GUI.
     */
    private void createInfoButton(SkyHopper skyHopper) {
        GUIConfig.GenericEntry infoConfig = guiConfig.entries().info();

        GUIButton.Builder infoBuilder = new GUIButton.Builder();

        if(infoConfig.item().material() != null) {
            Material infoMaterial = Material.getMaterial(infoConfig.item().material());
            if(infoMaterial != null) {
                if(skyHopper.owner() != null) {
                    String playerName = plugin.getServer().getOfflinePlayer(skyHopper.owner()).getName();
                    if(playerName != null) {

                        ItemStack itemStack = ItemStack.of(infoMaterial);
                        ItemMeta itemMeta = itemStack.getItemMeta();

                        if (infoConfig.item().name() != null) {
                            itemMeta.displayName(FormatUtil.format(infoConfig.item().name()));
                        }

                        List<TagResolver.Single> lorePlaceholders = List.of(
                                Placeholder.parsed("status", String.valueOf(skyHopper.enabled())),
                                Placeholder.parsed("owner", playerName),
                                Placeholder.parsed("member_count", String.valueOf(skyHopper.members().size())),
                                Placeholder.parsed("filter_type", skyHopper.filterType().name()),
                                Placeholder.parsed("links_count", String.valueOf(skyHopper.containers().size())),
                                Placeholder.parsed("links_amount", String.valueOf(skyHopper.maxContainers())),
                                Placeholder.parsed("transfer_amount", String.valueOf(skyHopper.transferAmount())),
                                Placeholder.parsed("transfer_speed", String.valueOf(skyHopper.transferSpeed())),
                                Placeholder.parsed("suction_amount", String.valueOf(skyHopper.suctionAmount())),
                                Placeholder.parsed("suction_speed", String.valueOf(skyHopper.suctionSpeed())),
                                Placeholder.parsed("suction_range", String.valueOf(skyHopper.suctionRange())));

                        List<Component> lore = infoConfig.item().lore().stream().map(line -> FormatUtil.format(line, lorePlaceholders)).toList();
                        itemMeta.lore(lore);

                        List<ItemFlag> itemFlags = infoConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();
                        itemFlags.forEach(itemMeta::addItemFlags);

                        itemStack.setItemMeta(itemMeta);

                        infoBuilder.setItemStack(itemStack);

                        setButton(infoConfig.slot(), infoBuilder.build());
                    }
                }
            }
        }
    }
}
