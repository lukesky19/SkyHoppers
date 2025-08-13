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
import com.github.lukesky19.skyHoppers.data.config.gui.ButtonConfig;
import com.github.lukesky19.skyHoppers.manager.GUIConfigManager;
import com.github.lukesky19.skyHoppers.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.manager.SettingsManager;
import com.github.lukesky19.skyHoppers.data.config.Locale;
import com.github.lukesky19.skyHoppers.data.config.Settings;
import com.github.lukesky19.skyHoppers.data.config.gui.upgrade.UpgradeGUIConfig;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.GUIManager;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
 * This class lets Players upgrade the range SkyHoppers can suction items.
 */
public class SuctionRangeUpgradeGUI extends SkyHopperGUI {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull HopperManager hopperManager;

    private final @NotNull UpgradesGUI upgradesGUI;

    private final @NotNull SkyHopper skyHopper;

    private final @Nullable UpgradeGUIConfig guiConfig;

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
     * @param upgradesGUI The {@link UpgradesGUI} the player cane from.
     */
    public SuctionRangeUpgradeGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull Location location,
            @NotNull SkyHopper skyHopper,
            @NotNull Player player,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull HopperManager hopperManager,
            @NotNull UpgradesGUI upgradesGUI) {
        super(skyHoppers, guiManager, player, location);

        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.hopperManager = hopperManager;

        this.skyHopper = skyHopper;

        this.upgradesGUI = upgradesGUI;

        guiConfig = guiConfigManager.getUpgradeConfig("suction_range.yml");
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the suction range upgrade GUI due to invalid GUI configuration."));
            return false;
        }

        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the suction range upgrade GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = guiConfig.name();
        if(guiName == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the suction range upgrade GUI due to an invalid gui name."));
            return false;
        }

        return create(guiType, guiName, List.of());
    }

    /**
     * Creates and populates the buttons for this GUI.
     */
    @Override
    public boolean update() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to decorate the GUI due to invalid configuration for the suction range upgrade GUI."));
            if(isOpen) close();
            return false;
        }

        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to update the suction range upgrade GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        Settings settings = settingsManager.getSettings();
        if(settings == null) {
            logger.warn(AdventureUtil.serialize("Unable to update the suction range upgrade GUI as the plugin settings are invalid."));
            if(isOpen) close();
            return false;
        }

        TreeMap<Integer, Double> upgrades = settingsManager.getSuctionRangeUpgrades();
        if(upgrades == null) {
            logger.warn(AdventureUtil.serialize("Unable to update the suction range upgrade GUI as the suction range upgrade settings are invalid."));
            if(isOpen) close();
            return false;
        }

        clearButtons();

        int guiSize = inventoryView.getTopInventory().getSize();

        createFiller(guiSize);
        // Dummy Buttons
        createDummyButtons();
        createExitButton();
        createIncreaseButton(upgrades);
        createDecreaseButton(upgrades);
        createUpgradeButton(upgrades);

        return super.update();
    }

    /**
     * Close the current GUI and open the {@link UpgradesGUI} that the player came from.
     */
    @Override
    public void close() {
        super.close();

        upgradesGUI.update();

        upgradesGUI.open();
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

        upgradesGUI.update();

        upgradesGUI.open();
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
     * Creates the Exit button.
     */
    private void createExitButton() {
        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().exit();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the exit button in the suction range upgrade gui due to no slot configured."));
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

    /**
     * Creates the button to increase the suction range.
     */
    private void createIncreaseButton(@NotNull TreeMap<Integer, Double> upgrades) {
        assert guiConfig != null;

        if(skyHopper.getSuctionRange() != skyHopper.getMaxSuctionRange()) {
            ButtonConfig buttonConfig = guiConfig.entries().increase();

            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the increase button in the suction range upgrade gui due to no slot configured."));
                return;
            }

            Optional<Map.Entry<Integer, Double>> optionalUpgrade = getLargerRange(upgrades, skyHopper.getSuctionRange());
            if(optionalUpgrade.isEmpty()) return;
            Map.Entry<Integer, Double> upgrade = optionalUpgrade.get();

            List<TagResolver.Single> placeholders = List.of(
                    Placeholder.parsed("current", String.valueOf(skyHopper.getSuctionRange())),
                    Placeholder.parsed("change", String.valueOf(upgrade.getKey())));

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, placeholders);
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder buttonBuilder = new GUIButton.Builder();

                buttonBuilder.setItemStack(optionalItemStack.get());

                buttonBuilder.setAction(inventoryClickEvent -> {
                    skyHopper.setSuctionRange(upgrade.getKey());

                    hopperManager.saveSkyHopperToPDC(skyHopper);

                    guiManager.refreshViewersGUI(location);

                    update();
                });

                setButton(buttonConfig.slot(), buttonBuilder.build());
            }
        } else {
            ButtonConfig buttonConfig = guiConfig.entries().increaseMax();

            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the increase max button in the suction range upgrade gui due to no slot configured."));
                return;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder buttonBuilder = new GUIButton.Builder();

                buttonBuilder.setItemStack(optionalItemStack.get());

                setButton(buttonConfig.slot(), buttonBuilder.build());
            }
        }
    }

    /**
     * Creates the button to decrease the suction range.
     */
    private void createDecreaseButton(@NotNull TreeMap<Integer, Double> upgrades) {
        assert guiConfig != null;

        if(skyHopper.getSuctionRange() != upgrades.firstKey()) {
            ButtonConfig buttonConfig = guiConfig.entries().decrease();

            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the decrease button in the suction range upgrade gui due to no slot configured."));
                return;
            }

            Optional<Map.Entry<Integer, Double>> optionalUpgrade = getSmallerRange(upgrades, skyHopper.getSuctionRange());
            if(optionalUpgrade.isEmpty()) return;
            Map.Entry<Integer, Double> upgrade = optionalUpgrade.get();

            List<TagResolver.Single> placeholders = List.of(
                    Placeholder.parsed("current", String.valueOf(skyHopper.getSuctionRange())),
                    Placeholder.parsed("change", String.valueOf(upgrade.getKey())));

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, placeholders);
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder buttonBuilder = new GUIButton.Builder();

                buttonBuilder.setItemStack(optionalItemStack.get());

                buttonBuilder.setAction(inventoryClickEvent -> {
                    skyHopper.setSuctionRange(upgrade.getKey());

                    hopperManager.saveSkyHopperToPDC(skyHopper);

                    guiManager.refreshViewersGUI(location);

                    update();
                });

                setButton(buttonConfig.slot(), buttonBuilder.build());
            }
        } else {
            ButtonConfig buttonConfig = guiConfig.entries().decreaseMin();

            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the decrease min button in the suction range upgrade gui due to no slot configured."));
                return;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder buttonBuilder = new GUIButton.Builder();

                buttonBuilder.setItemStack(optionalItemStack.get());

                setButton(buttonConfig.slot(), buttonBuilder.build());
            }
        }
    }

    /**
     * Creates the button to upgrade the suction range.
     */
    private void createUpgradeButton(@NotNull TreeMap<Integer, Double> upgrades) {
        assert guiConfig != null;

        Optional<Map.Entry<Integer, Double>> nextUpgrade = getNextUpgrade(upgrades, skyHopper.getMaxSuctionRange());
        if(nextUpgrade.isPresent()) {
            ButtonConfig buttonConfig = guiConfig.entries().upgrade();

            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the upgrade button in the suction range upgrade gui due to no slot configured."));
                return;
            }

            int upgradeRange = nextUpgrade.get().getKey();
            double upgradePrice = nextUpgrade.get().getValue();

            List<TagResolver.Single> placeholders = List.of(
                    Placeholder.parsed("current", String.valueOf(skyHopper.getMaxSuctionRange())),
                    Placeholder.parsed("next", String.valueOf(upgradeRange)),
                    Placeholder.parsed("price", String.valueOf(upgradePrice)));

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, placeholders);
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder buttonBuilder = new GUIButton.Builder();

                buttonBuilder.setItemStack(optionalItemStack.get());

                buttonBuilder.setAction(inventoryClickEvent -> {
                    Locale locale = localeManager.getLocale();

                    if(skyHoppers.getEconomy().getBalance(player) >= upgradePrice) {
                        skyHoppers.getEconomy().withdrawPlayer(player, upgradePrice);

                        skyHopper.setSuctionRange(upgradeRange);
                        skyHopper.setMaxSuctionRange(upgradeRange);

                        List<TagResolver.Single> messagePlaceholders = List.of(
                                Placeholder.parsed("current", String.valueOf(skyHopper.getSuctionRange())),
                                Placeholder.parsed("next", String.valueOf(upgradeRange)));

                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.suctionRangeUpgrade(), messagePlaceholders));

                        hopperManager.saveSkyHopperToPDC(skyHopper);

                        guiManager.refreshViewersGUI(location);

                        update();
                    } else {
                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.notEnoughMoney()));
                    }
                });

                setButton(buttonConfig.slot(), buttonBuilder.build());
            }
        } else {
            ButtonConfig buttonConfig = guiConfig.entries().upgradeMax();

            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the upgrade max button in the suction range upgrade gui due to no slot configured."));
                return;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder buttonBuilder = new GUIButton.Builder();

                buttonBuilder.setItemStack(optionalItemStack.get());

                setButton(buttonConfig.slot(), buttonBuilder.build());
            }
        }
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(guiConfig == null) return;

        guiConfig.entries().dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to add a dummy button to the suction range upgrade GUI due to an invalid slot."));
                return;
            }

            ItemStackConfig itemStackConfig = buttonConfig.item();
            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, List.of());
            Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresent(itemStack -> {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(itemStack);

                setButton(buttonConfig.slot(), builder.build());
            });
        });
    }

    /**
     * Gets the next available upgrade.
     * @param upgrades The TreeMap of upgrades.
     * @param currentKey The current upgrade.
     * @return An Optional of the next available upgrade.
     */
    private @NotNull Optional<Map.Entry<Integer, Double>> getNextUpgrade(TreeMap<Integer, Double> upgrades, Integer currentKey) {
        Map.Entry<Integer, Double> currentEntry = upgrades.floorEntry(currentKey);

        if(currentEntry == null) return Optional.empty();

        return Optional.ofNullable(upgrades.higherEntry(currentEntry.getKey()));
    }

    /**
     * Gets suction range larger than the current.
     * @param upgrades The TreeMap of upgrades.
     * @param currentKey The current upgrade.
     * @return An Optional of the larger suction range.
     */
    private @NotNull Optional<Map.Entry<Integer, Double>> getLargerRange(TreeMap<Integer, Double> upgrades, Integer currentKey) {
        Map.Entry<Integer, Double> currentEntry = upgrades.floorEntry(currentKey);

        if(currentEntry == null) return Optional.empty();

        Map.Entry<Integer, Double> maxEntry = upgrades.lastEntry();

        if(maxEntry.getKey().equals(currentEntry.getKey())) return Optional.empty();

        return Optional.ofNullable(upgrades.higherEntry(currentEntry.getKey()));
    }

    /**
     * Gets suction range smaller than the current.
     * @param upgrades The TreeMap of upgrades.
     * @param currentKey The current upgrade.
     * @return An Optional of the smaller suction range.
     */
    private @NotNull Optional<Map.Entry<Integer, Double>> getSmallerRange(TreeMap<Integer, Double> upgrades, Integer currentKey) {
        Map.Entry<Integer, Double> currentEntry = upgrades.floorEntry(currentKey);

        if (currentEntry == null) return Optional.empty();

        Map.Entry<Integer, Double> minEntry = upgrades.firstEntry();

        if(minEntry.getKey().equals(currentEntry.getKey())) return Optional.empty();

        return Optional.ofNullable(upgrades.lowerEntry(currentEntry.getKey()));
    }
}
