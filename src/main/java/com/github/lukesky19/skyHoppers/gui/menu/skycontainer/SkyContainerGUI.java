/*
    SkyHoppers adds upgradable hoppers that can suction items, transfer items wirelessly to linked containers.
    Copyright (C) 2024  lukeskywlker19

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
import com.github.lukesky19.skyHoppers.config.LocaleManager;
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.config.data.Locale;
import com.github.lukesky19.skyHoppers.config.data.button.ButtonConfig;
import com.github.lukesky19.skyHoppers.config.data.gui.SkyContainerGUIConfig;
import com.github.lukesky19.skyHoppers.gui.GUIManager;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.gui.menu.filter.SkyContainerFilterGUI;
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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * This GUI lets the player select what they would like to change related to a linked container.
 * Currently only the filter and priority.
 */
public class SkyContainerGUI extends SkyHopperGUI {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;

    private final @NotNull SkyContainer skyContainer;

    private final @Nullable SkyContainerGUIConfig guiConfig;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param location The {@link Location} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper} the GUI is associated with.
     * @param skyContainer The {@link SkyContainer} the GUI is associated with.
     * @param player The {@link Player} viewing the GUI.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A {@link SkyHopperManager} instance.
     * @param linkedContainersGUI The {@link LinkedContainersGUI} to return to when this GUI is closed.
     */
    public SkyContainerGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull ImmutableLocation location,
            @NotNull SkyHopper skyHopper,
            @NotNull SkyContainer skyContainer,
            @NotNull Player player,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull SkyHopperManager hopperManager,
            @NotNull LinkedContainersGUI linkedContainersGUI) {
        super(skyHoppers, guiManager, player, skyHopper, location, hopperManager, linkedContainersGUI);

        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;

        this.skyContainer = skyContainer;

        guiConfig = guiConfigManager.getSkyContainerGUIConfig();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the hopper.yml GUI due to invalid GUI configuration."));
            return false;
        }

        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the hopper.yml GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = guiConfig.name();
        if(guiName == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the hopper.yml GUI due to an invalid gui name."));
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
            logger.warn(AdventureUtil.deserialize("Unable to decorate the GUI due to invalid configuration for the hopper.yml GUI."));
            return false;
        }

        clearButtons();

        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to update the main hopper GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        // GUI Size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Filler
        createFiller(guiSize);

        // Dummy Buttons
        createDummyButtons();

        createFilterButton();

        createPriorityButton();

        // Exit Button
        createExitButton();

        // Info Button
        createInfoButton();

        return super.update();
    }

    /**
     * Refreshes the buttons in the GUI.
     */
    @Override
    public boolean refresh() {
        return this.update();
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
     * Creates the filter Button.
     */
    private void createFilterButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().filter();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the filter button in the sky container GUI due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () -> {
                    player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

                    guiManager.removeOpenGUI(identifier);
                }, 1L);

                SkyContainerFilterGUI outputFilterGUI = new SkyContainerFilterGUI(skyHoppers, guiManager, location, skyHopper, player, guiConfigManager, hopperManager, skyContainer, this);

                boolean creationResult = outputFilterGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = outputFilterGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = outputFilterGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the priority Button.
     */
    private void createPriorityButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().priority();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the priority button in the sky container gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () -> {
                    player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

                    guiManager.removeOpenGUI(identifier);
                }, 1L);

                PriorityGUI priorityGUI = new PriorityGUI(skyHoppers, guiManager, location, skyHopper, skyContainer, player, settingsManager, guiConfigManager, hopperManager, this);

                boolean creationResult = priorityGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = priorityGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = priorityGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                }
            });

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the Exit button.
     */
    private void createExitButton() {
        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().exit();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the exit button due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event ->
                    skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, this::close, 1L));

            setButton(buttonConfig.slot(), builder.build());
        }
    }

    /**
     * Creates the info button.
     */
    private void createInfoButton() {
        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().info();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the info button for the sky container gui due to no slot configured."));
            return;
        }

        skyContainer.getFilterType();
        skyContainer.getLocation();
        skyContainer.getPriority();

        List<TagResolver.Single> lorePlaceholders = List.of(
                Placeholder.parsed("filter_type", skyHopper.getFilterType().name()),
                Placeholder.parsed("world", skyContainer.getLocation().getWorld().getName()),
                Placeholder.parsed("x", String.valueOf(skyContainer.getLocation().getX())),
                Placeholder.parsed("y", String.valueOf(skyContainer.getLocation().getY())),
                Placeholder.parsed("z", String.valueOf(skyContainer.getLocation().getZ())),
                Placeholder.parsed("priority", String.valueOf(skyContainer.getPriority())));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, lorePlaceholders);
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

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
                logger.warn(AdventureUtil.deserialize("Unable to add a dummy button to the main hopper GUI due to an invalid slot."));
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