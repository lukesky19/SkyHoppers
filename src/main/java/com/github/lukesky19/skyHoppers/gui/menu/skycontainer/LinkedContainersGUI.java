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
import com.github.lukesky19.skyHoppers.config.LocaleManager;
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.config.data.Locale;
import com.github.lukesky19.skyHoppers.config.data.button.ButtonConfig;
import com.github.lukesky19.skyHoppers.config.data.gui.LinkedContainersGUIConfig;
import com.github.lukesky19.skyHoppers.gui.GUIManager;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.gui.menu.HopperGUI;
import com.github.lukesky19.skyHoppers.listener.HopperClickListener;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.util.ImmutableLocation;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.format.FormatUtil;
import com.github.lukesky19.skylib.paper.api.gui.GUIButton;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * This class lets Players manage their linked containers.
 */
public class LinkedContainersGUI extends SkyHopperGUI {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;

    private final @NotNull HopperClickListener hopperClickListener;

    private final @Nullable LinkedContainersGUIConfig guiConfig;

    private int containerNum = 0;
    private int added = 0;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param location The {@link ImmutableLocation} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper}.
     * @param player The {@link Player} viewing the GUI.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A {@link SkyHopperManager} instance.
     * @param hopperClickListener A {@link HopperClickListener} instance.
     * @param hopperGUI The {@link HopperGUI} the Player came from.
     */
    public LinkedContainersGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull ImmutableLocation location,
            @NotNull SkyHopper skyHopper,
            @NotNull Player player,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull SkyHopperManager hopperManager,
            @NotNull HopperClickListener hopperClickListener,
            @NotNull HopperGUI hopperGUI) {
        super(skyHoppers, guiManager, player, skyHopper, location, hopperManager, hopperGUI);

        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.hopperClickListener = hopperClickListener;

        guiConfig = guiConfigManager.getLinkedContainersGUIConfig();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(guiConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the links.yml GUI due to invalid GUI configuration."));
            return false;
        }

        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the links.yml GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = guiConfig.name();
        if(guiName == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the links.yml GUI due to an invalid gui name."));
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
            logger.warn(AdventureUtility.plain("Unable to decorate the GUI due to invalid configuration for the links GUI."));
            if(isOpen) close();
            return false;
        }

        clearButtons();

        if(inventoryView == null) {
            logger.warn(AdventureUtility.plain("Unable to update the links GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        createFiller(guiSize);
        // Dummy Buttons
        createDummyButtons();
        createLinkedContainerButtons(guiSize);
        createNextPageButton(guiSize);
        createPreviousPageButton(guiSize);
        createLinkButton();
        createExitButton();

        return super.update();
    }

    /**
     * Refreshes the buttons in the GUI.
     */
    @Override
    public boolean refresh() {
        added = 0;
        containerNum = 0;

        return update();
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
     * Creates all the buttons for the linked containers.
     */
    private void createLinkedContainerButtons(int guiSize) {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().linkedItem();

        int maxLinkedContainers = skyHopper.getLinkedContainers().size() - 1;

        if(guiSize - 10 >= 17) {
            List<ItemFlag> itemFlags = buttonConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

            for(int i = 0; i <= guiSize - 10; i++) {
                if(maxLinkedContainers >= containerNum) {
                    SkyContainer skyContainer = skyHopper.getLinkedContainers().get(containerNum);
                    ImmutableLocation linkedLocation = skyContainer.getLocation();

                    GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);

                    if(linkedLocation.getBlock().getState(false) instanceof Container container) {
                        ItemType containerItemType = container.getType().asItemType();

                        if(containerItemType == null) containerItemType = ItemType.BARRIER;

                        itemStackBuilder.setItemType(containerItemType);

                        Component itemName = container.customName();
                        if(itemName == null) {
                            itemName = AdventureUtility.deserialize(FormatUtil.formatItemTypeName(containerItemType));
                        }

                        itemStackBuilder.setName(itemName);
                    } else {
                        itemStackBuilder.setItemType(ItemType.BARRIER);
                        itemStackBuilder.setName(AdventureUtility.deserialize("<red>Unknown Container</red>"));
                    }

                    List<TagResolver.Single> placeholders = List.of(
                            Placeholder.parsed("priority", String.valueOf(skyContainer.getPriority())),
                            Placeholder.parsed("world", linkedLocation.getWorld().getName()),
                            Placeholder.parsed("x", String.valueOf(linkedLocation.getX())),
                            Placeholder.parsed("y", String.valueOf(linkedLocation.getY())),
                            Placeholder.parsed("z", String.valueOf(linkedLocation.getZ())));

                    List<Component> lore = buttonConfig.item().lore().stream().map(line -> AdventureUtility.deserialize(line, placeholders)).toList();

                    itemStackBuilder.setLore(lore);

                    itemStackBuilder.setItemFlags(itemFlags);

                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                    if(optionalItemStack.isPresent()) {
                        guiButtonBuilder.setItemStack(optionalItemStack.get());

                        guiButtonBuilder.setAction(event -> {
                            switch (event.getClick()) {
                                case LEFT, SHIFT_LEFT -> {
                                    skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () -> {
                                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

                                        guiManager.removeOpenGUI(identifier);
                                    }, 1L);

                                    SkyContainerGUI skyContainerGUI = new SkyContainerGUI(skyHoppers, guiManager, location, skyHopper, skyContainer, player, settingsManager, localeManager, guiConfigManager, hopperManager, this);

                                    boolean creationResult = skyContainerGUI.create();
                                    if(!creationResult) {
                                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                                        return;
                                    }

                                    boolean updateResult = skyContainerGUI.update();
                                    if(!updateResult) {
                                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                                        return;
                                    }

                                    boolean openResult = skyContainerGUI.open();
                                    if(!openResult) {
                                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                                    }
                                }

                                case RIGHT, SHIFT_RIGHT -> {
                                    skyHopper.removeLinkedContainer(skyContainer, true);

                                    guiManager.closeSkyContainerRelatedGUIs(location);

                                    guiManager.refreshGUIsByLocation(location);

                                    added = 0;
                                    containerNum = 0;

                                    update();
                                }
                            }
                        });

                        setButton(i, guiButtonBuilder.build());

                        added++;
                        containerNum++;
                    }
                }
            }
        }
    }

    /**
     * Creates the next page button if needed
     */
    private void createNextPageButton(int guiSize) {
        if(added > guiSize - 1) {
            assert guiConfig != null;
            ButtonConfig buttonConfig = guiConfig.entries().nextPage();
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtility.plain("Unable to create the next page button in the links gui due to no slot configured."));
                return;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(optionalItemStack.get());

                builder.setAction(_ -> {
                    added = 0;
                    update();
                });

                setButton(buttonConfig.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the previous page button if needed
     */
    private void createPreviousPageButton(int guiSize) {
        if(containerNum > guiSize) {
            assert guiConfig != null;
            ButtonConfig buttonConfig = guiConfig.entries().previousPage();
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtility.plain("Unable to create the previous page button in the links gui due to no slot configured."));
                return;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(optionalItemStack.get());

                builder.setAction(_ -> {
                    containerNum -= (guiSize + added);

                    added = 0;

                    update();
                });

                setButton(buttonConfig.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the button to link and or unlink containers.
     */
    private void createLinkButton() {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().link();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to create the link button in the links gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();

            guiButtonBuilder.setItemStack(optionalItemStack.get());

            guiButtonBuilder.setAction(_ -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () -> {
                    player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                    guiManager.removeOpenGUI(identifier);
                }, 1L);

                if(skyHopper.getLocation() != null) {
                    hopperClickListener.addLinkingPlayer(player, skyHopper.getLocation());

                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.linkingEnabled()));
                    player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.linkingHowToExit()));
                }
            });

            setButton(buttonConfig.slot(), guiButtonBuilder.build());
        }
    }

    /**
     * Creates the Exit button.
     */
    private void createExitButton() {
        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().exit();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to create the exit button in the links gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(_ -> close());

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
                logger.warn(AdventureUtility.plain("Unable to add a dummy button to the links GUI due to an invalid slot."));
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