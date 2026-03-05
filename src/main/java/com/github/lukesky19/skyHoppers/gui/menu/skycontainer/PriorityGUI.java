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
package com.github.lukesky19.skyHoppers.gui.menu.skycontainer;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.GUIConfigManager;
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.config.data.Settings;
import com.github.lukesky19.skyHoppers.config.data.button.ButtonConfig;
import com.github.lukesky19.skyHoppers.config.data.gui.PriorityGUIConfig;
import com.github.lukesky19.skyHoppers.gui.GUIManager;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.util.ImmutableLocation;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * This GUI lets player's change the priority of a linked container.
 */
public class PriorityGUI extends SkyHopperGUI {
    private final @NotNull SettingsManager settingsManager;

    private final @NotNull SkyContainer skyContainer;

    private final @Nullable PriorityGUIConfig guiConfig;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param location The {@link ImmutableLocation} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper}.
     * @param skyContainer The {@link SkyContainer}.
     * @param player The {@link Player} viewing the GUI.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A {@link SkyHopperManager} instance.
     * @param skyContainerGUI The {@link SkyContainerGUI} the Player came from.
     */
    public PriorityGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull ImmutableLocation location,
            @NotNull SkyHopper skyHopper,
            @NotNull SkyContainer skyContainer,
            @NotNull Player player,
            @NotNull SettingsManager settingsManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull SkyHopperManager hopperManager,
            @NotNull SkyContainerGUI skyContainerGUI) {
        super(skyHoppers, guiManager, player, skyHopper, location, hopperManager, skyContainerGUI);

        this.settingsManager = settingsManager;

        this.skyContainer = skyContainer;

        guiConfig = guiConfigManager.getPriorityGUIConfig();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the priority GUI due to invalid GUI configuration."));
            return false;
        }

        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the priority GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = guiConfig.name();
        if(guiName == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the priority GUI due to an invalid gui name."));
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
            logger.warn(AdventureUtil.deserialize("Unable to decorate the priority GUI due to invalid configuration for the links GUI."));
            if(isOpen) close();
            return false;
        }

        clearButtons();

        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to update the priority GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        createFiller(guiSize);

        // Dummy Buttons
        createDummyButtons();

        createIncreaseButton();
        createCurrentPriorityButton();
        createDecreaseButton();
        createHighestPriorityButton();
        createLowestPriorityButton();
        createDefaultPriorityButton();

        createExitButton();

        return super.update();
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
        itemStackBuilder.fromItemStackConfig(filler, null, List.of());
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
     * Creates the increase priority button.
     */
    private void createIncreaseButton() {
        if(guiConfig == null) return;
        ButtonConfig buttonConfig = guiConfig.entries().increasePriority();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the increase button in the priority gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();

            guiButtonBuilder.setItemStack(optionalItemStack.get());

            guiButtonBuilder.setAction(inventoryClickEvent -> {
                if(skyContainer.getPriority() == settingsManager.getHighestPriority()) return;

                skyContainer.increasePriority();

                skyHopper.sortLinkedContainers();

                guiManager.refreshGUIsByLocation(identifier.location());
            });

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        }
    }

    /**
     * Creates the decrease priority button.
     */
    private void createDecreaseButton() {
        if(guiConfig == null) return;
        ButtonConfig buttonConfig = guiConfig.entries().decreasePriority();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the decrease button in the priority gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();

            guiButtonBuilder.setItemStack(optionalItemStack.get());

            guiButtonBuilder.setAction(inventoryClickEvent -> {
                if(skyContainer.getPriority() == settingsManager.getLowestPriority()) return;

                skyContainer.decreasePriority();

                skyHopper.sortLinkedContainers();

                guiManager.refreshGUIsByLocation(identifier.location());
            });

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        }
    }

    /**
     * Creates the current priority button.
     */
    private void createCurrentPriorityButton() {
        if(guiConfig == null) return;
        ButtonConfig buttonConfig = guiConfig.entries().currentPriority();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the current priority button in the priority gui due to no slot configured."));
            return;
        }

        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("priority", String.valueOf(skyContainer.getPriority())));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, placeholders);
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();

            guiButtonBuilder.setItemStack(optionalItemStack.get());

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        }
    }

    /**
     * Creates the highest priority button.
     */
    private void createHighestPriorityButton() {
        if(guiConfig == null) return;
        ButtonConfig buttonConfig = guiConfig.entries().highestPriority();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the highest priority button in the priority gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();

            guiButtonBuilder.setItemStack(optionalItemStack.get());

            guiButtonBuilder.setAction(inventoryClickEvent -> {
                skyContainer.setPriority(settingsManager.getHighestPriority());

                skyHopper.sortLinkedContainers();

                guiManager.refreshGUIsByLocation(identifier.location());
            });

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        }
    }

    /**
     * Creates the highest priority button.
     */
    private void createLowestPriorityButton() {
        if(guiConfig == null) return;
        ButtonConfig buttonConfig = guiConfig.entries().lowestPriority();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the lowest priority button in the priority gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();

            guiButtonBuilder.setItemStack(optionalItemStack.get());

            guiButtonBuilder.setAction(inventoryClickEvent -> {
                @Nullable Settings settings = settingsManager.getSettings();
                int lowestPriority = settings != null ? settings.skyContainerConfig().lowestPriority() : 255;

                skyContainer.setPriority(lowestPriority);

                skyHopper.sortLinkedContainers();

                guiManager.refreshGUIsByLocation(identifier.location());
            });

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        }
    }

    /**
     * Creates the default priority button.
     */
    private void createDefaultPriorityButton() {
        if(guiConfig == null) return;
        ButtonConfig buttonConfig = guiConfig.entries().defaultPriority();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the default priority button in the priority gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();

            guiButtonBuilder.setItemStack(optionalItemStack.get());

            guiButtonBuilder.setAction(inventoryClickEvent -> {
                @Nullable Settings settings = settingsManager.getSettings();
                int defaultPriority = settings != null ? settings.skyContainerConfig().startingPriority() : 1;

                skyContainer.setPriority(defaultPriority);

                skyHopper.sortLinkedContainers();

                guiManager.refreshGUIsByLocation(identifier.location());
            });

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        }
    }

    /**
     * Creates the Exit button.
     */
    private void createExitButton() {
        if(guiConfig == null) return;
        ButtonConfig buttonConfig = guiConfig.entries().exit();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the exit button in the priority gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> close());

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(guiConfig == null) return;

        guiConfig.entries().dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a dummy button to the priority GUI due to an invalid slot."));
                return;
            }

            ItemStackConfig itemStackConfig = buttonConfig.item();
            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());
            Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresent(itemStack -> {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(itemStack);

                setButton(buttonConfig.slot(), builder.build());
            });
        });
    }
}