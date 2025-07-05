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
package com.github.lukesky19.skyHoppers.gui.menu.upgrades;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.manager.GUIConfigManager;
import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.manager.SettingsManager;
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.config.record.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.config.record.Settings;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.gui.menu.HopperGUI;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.GUIManager;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Lets players manage the SkyHoppers upgrades.
 */
public class UpgradesGUI extends SkyHopperGUI {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull HopperManager hopperManager;

    private final @NotNull HopperGUI hopperGUI;

    private final @NotNull SkyHopper skyHopper;

    private final @Nullable GUIConfig guiConfig;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param location The {@link Location} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper}.
     * @param player The {@link Player} viewing the GUI.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A {@link HopperManager} instance.
     * @param hopperGUI The {@link HopperGUI} the Player came from.
     */
    public UpgradesGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull Location location,
            @NotNull SkyHopper skyHopper,
            @NotNull Player player,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull HopperManager hopperManager,
            @NotNull HopperGUI hopperGUI) {
        super(skyHoppers, guiManager, player, location);

        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.hopperManager = hopperManager;

        this.hopperGUI = hopperGUI;

        this.skyHopper = skyHopper;

        guiConfig = guiConfigManager.getGuiConfig("upgrades.yml");
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the input_filter.yml GUI due to invalid GUI configuration."));
            return false;
        }

        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the input_filter.yml GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = guiConfig.name();
        if(guiName == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the input_filter.yml GUI due to an invalid gui name."));
            return false;
        }

        return create(guiType, guiName, List.of());
    }

    /**
     * Creates and populates the buttons for this GUI.
     */
    @Override
    public boolean update() {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return false;

        if(guiConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to decorate the GUI due to invalid configuration for the input filter GUI."));
            if(isOpen) close();
            return false;
        }

        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to update the input filter GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        clearButtons();

        int guiSize = inventoryView.getTopInventory().getSize();

        // Filler
        createFiller(guiSize);

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

        return super.update();
    }

    /**
     * Close the current GUI and open the {@link HopperGUI} that the player came from.
     */
    @Override
    public void close() {
        super.close();

        hopperGUI.update();

        hopperGUI.open();
    }

    /**
     * Handles when the player closes the GUI.
     * @param inventoryCloseEvent An InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeViewer(location, uuid);

        isOpen = false;

        hopperGUI.update();

        hopperGUI.open();
    }

    /**
     * Handles when items are dragged across the bottom (player's) inventory.
     * This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}.
     */
    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when items are dragged across the top or bottom inventory.
     * This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}.
     */
    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when a slot is clicked in the bottom (player's) inventory.
     * This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Handles when a slot is clicked in the top or bottom inventory.
     * This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}


    /**
     * Creates all the Filler buttons.
     */
    private void createFiller(int guiSize) {
        assert guiConfig != null;
        ItemStackConfig filler = guiConfig.entries().filler();

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(filler, null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            GUIButton button = builder.build();

            for(int i = 0; i <= guiSize - 1; i++) {
                setButton(i, button);
            }
        }
    }

    /**
     * Creates the button to access the suction speed upgrade GUI.
     */
    private void createSuctionSpeedButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().suctionSpeed();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the suction speed button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder buttonBuilder = new GUIButton.Builder();

            buttonBuilder.setItemStack(optionalItemStack.get());

            buttonBuilder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                guiManager.removeViewer(location, player.getUniqueId());

                SuctionSpeedUpgradeGUI suctionSpeedUpgradeGUI = new SuctionSpeedUpgradeGUI(skyHoppers, guiManager, location, skyHopper, player, settingsManager, localeManager, guiConfigManager, hopperManager, this);

                boolean creationResult = suctionSpeedUpgradeGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = suctionSpeedUpgradeGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = suctionSpeedUpgradeGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), buttonBuilder.build());
        }
    }

    /**
     * Creates the button to access the suction amount upgrade GUI.
     */
    private void createSuctionAmountButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().suctionAmount();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the suction amount button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder buttonBuilder = new GUIButton.Builder();

            buttonBuilder.setItemStack(optionalItemStack.get());

            buttonBuilder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                guiManager.removeViewer(location, player.getUniqueId());

                SuctionAmountUpgradeGUI suctionAmountUpgradeGUI = new SuctionAmountUpgradeGUI(skyHoppers, guiManager, location, skyHopper, player, settingsManager, localeManager, guiConfigManager, hopperManager, this);

                boolean creationResult = suctionAmountUpgradeGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = suctionAmountUpgradeGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = suctionAmountUpgradeGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), buttonBuilder.build());
        }
    }

    /**
     * Creates the button to access the suction range upgrade GUI.
     */
    private void createSuctionRangeButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().suctionRange();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the suction range button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder buttonBuilder = new GUIButton.Builder();

            buttonBuilder.setItemStack(optionalItemStack.get());

            buttonBuilder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                guiManager.removeViewer(location, player.getUniqueId());

                SuctionRangeUpgradeGUI suctionRangeUpgradeGUI = new SuctionRangeUpgradeGUI(skyHoppers, guiManager, location, skyHopper, player, settingsManager, localeManager, guiConfigManager, hopperManager, this);

                boolean creationResult = suctionRangeUpgradeGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = suctionRangeUpgradeGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = suctionRangeUpgradeGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), buttonBuilder.build());
        }
    }

    /**
     * Creates the button to access the links upgrade GUI.
     */
    private void createLinksButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().maxLinks();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the max links button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder buttonBuilder = new GUIButton.Builder();

            buttonBuilder.setItemStack(optionalItemStack.get());

            buttonBuilder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                guiManager.removeViewer(location, player.getUniqueId());

                LinksUpgradeGUI linksUpgradeGUI = new LinksUpgradeGUI(skyHoppers, guiManager, location, skyHopper, player, settingsManager, localeManager, guiConfigManager, hopperManager, this);

                boolean creationResult = linksUpgradeGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = linksUpgradeGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = linksUpgradeGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), buttonBuilder.build());
        }
    }

    /**
     * Creates the button to access the transfer amount upgrade GUI.
     */
    private void createTransferAmountButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().transferSpeed();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the transfer speed button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder buttonBuilder = new GUIButton.Builder();

            buttonBuilder.setItemStack(optionalItemStack.get());

            buttonBuilder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                guiManager.removeViewer(location, player.getUniqueId());

                TransferSpeedUpgradeGUI transferSpeedUpgradeGUI = new TransferSpeedUpgradeGUI(skyHoppers, guiManager, location, skyHopper, player, settingsManager, localeManager, guiConfigManager, hopperManager, this);

                boolean creationResult = transferSpeedUpgradeGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = transferSpeedUpgradeGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = transferSpeedUpgradeGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), buttonBuilder.build());
        }
    }

    /**
     * Creates the button to access the transfer speed upgrade GUI.
     */
    private void createTransferSpeedButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().transferAmount();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the transfer amount button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder buttonBuilder = new GUIButton.Builder();

            buttonBuilder.setItemStack(optionalItemStack.get());

            buttonBuilder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                guiManager.removeViewer(location, player.getUniqueId());

                TransferAmountUpgradeGUI transferAmountUpgradeGUI = new TransferAmountUpgradeGUI(skyHoppers, guiManager, location, skyHopper, player, settingsManager, localeManager, guiConfigManager, hopperManager, this);

                boolean creationResult = transferAmountUpgradeGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = transferAmountUpgradeGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = transferAmountUpgradeGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), buttonBuilder.build());
        }
    }

    /**
     * Creates the Exit button.
     */
    private void createExitButton() {
        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().exit();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the exit button in the upgrades gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> close());

            setButton(buttonConfig.slot(), builder.build());
        }
    }
}
