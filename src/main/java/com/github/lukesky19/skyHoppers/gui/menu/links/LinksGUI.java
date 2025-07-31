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
package com.github.lukesky19.skyHoppers.gui.menu.links;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.manager.GUIConfigManager;
import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.record.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.gui.menu.HopperGUI;
import com.github.lukesky19.skyHoppers.gui.menu.filter.OutputFilterGUI;
import com.github.lukesky19.skyHoppers.hopper.SkyContainer;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.listener.HopperClickListener;
import com.github.lukesky19.skyHoppers.manager.GUIManager;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.format.FormatUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
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
public class LinksGUI extends SkyHopperGUI {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;

    private final @NotNull HopperManager hopperManager;
    private final @NotNull HopperClickListener hopperClickListener;

    private final @NotNull HopperGUI hopperGUI;

    private final @NotNull SkyHopper skyHopper;

    private final @Nullable GUIConfig guiConfig;

    private int containerNum = 0;
    private int added = 0;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param location The {@link Location} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper}.
     * @param player The {@link Player} viewing the GUI.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A {@link HopperManager} instance.
     * @param hopperClickListener A {@link HopperClickListener} instance.
     * @param hopperGUI The {@link HopperGUI} the Player came from.
     */
    public LinksGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull Location location,
            @NotNull SkyHopper skyHopper,
            @NotNull Player player,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull HopperManager hopperManager,
            @NotNull HopperClickListener hopperClickListener,
            @NotNull HopperGUI hopperGUI) {
        super(skyHoppers, guiManager, player, location);

        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.hopperManager = hopperManager;
        this.hopperClickListener = hopperClickListener;

        this.hopperGUI = hopperGUI;

        this.skyHopper = skyHopper;

        guiConfig = guiConfigManager.getGuiConfig("links.yml");
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the links.yml GUI due to invalid GUI configuration."));
            return false;
        }

        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the links.yml GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = guiConfig.name();
        if(guiName == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the links.yml GUI due to an invalid gui name."));
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
            logger.warn(AdventureUtil.serialize("Unable to decorate the GUI due to invalid configuration for the links GUI."));
            if(isOpen) close();
            return false;
        }

        clearButtons();

        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to update the links GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        createFiller(guiSize);
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
     * Closes the current GUI and opens the {@link HopperGUI} the player came from.
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
     * Creates all the buttons for the linked containers.
     */
    private void createLinkedContainerButtons(int guiSize) {
        Locale locale = localeManager.getLocale();

        assert guiConfig != null;
        GUIConfig.Button buttonConfig = guiConfig.entries().linkedItem();

        int maxLinkedContainers = skyHopper.getLinkedContainers().size() - 1;

        if(guiSize - 10 >= 17) {
            List<ItemFlag> itemFlags = buttonConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

            for(int i = 0; i <= guiSize - 10; i++) {
                if(maxLinkedContainers >= containerNum) {
                    SkyContainer skyContainer = skyHopper.getLinkedContainers().get(containerNum);
                    Location linkedLocation = skyContainer.getLocation();

                    GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);

                    if(linkedLocation.getBlock().getState(false) instanceof Container container) {
                        ItemType containerItemType = container.getType().asItemType();

                        if(containerItemType == null) containerItemType = ItemType.BARRIER;

                        itemStackBuilder.setItemType(containerItemType);

                        Component itemName = container.customName();
                        if(itemName == null) {
                            itemName = AdventureUtil.serialize(FormatUtil.formatItemTypeName(containerItemType));
                        }

                        itemStackBuilder.setName(itemName);
                    } else {
                        itemStackBuilder.setItemType(ItemType.BARRIER);
                        itemStackBuilder.setName(AdventureUtil.serialize("<red>Unknown Container</red>"));
                    }

                    List<TagResolver.Single> placeholders = List.of(
                            Placeholder.parsed("x", String.valueOf(linkedLocation.getX())),
                            Placeholder.parsed("y", String.valueOf(linkedLocation.getY())),
                            Placeholder.parsed("z", String.valueOf(linkedLocation.getZ())));

                    List<Component> lore = buttonConfig.item().lore().stream().map(line -> AdventureUtil.serialize(line, placeholders)).toList();

                    itemStackBuilder.setLore(lore);

                    itemStackBuilder.setItemFlags(itemFlags);

                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
                    if(optionalItemStack.isPresent()) {
                        guiButtonBuilder.setItemStack(optionalItemStack.get());

                        guiButtonBuilder.setAction(event -> {
                            switch (event.getClick()) {
                                case LEFT, SHIFT_LEFT -> {
                                    skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                                            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW), 1L);

                                    guiManager.removeViewer(location, uuid);

                                    OutputFilterGUI outputFilterGUI = new OutputFilterGUI(skyHoppers, guiManager, location, skyHopper, player, guiConfigManager, hopperManager, skyContainer, this);

                                    boolean creationResult = outputFilterGUI.create();
                                    if(!creationResult) {
                                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                        return;
                                    }

                                    boolean updateResult = outputFilterGUI.update();
                                    if(!updateResult) {
                                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                        return;
                                    }

                                    boolean openResult = outputFilterGUI.open();
                                    if(!openResult) {
                                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                                    }
                                }

                                case RIGHT, SHIFT_RIGHT -> {
                                    skyHopper.removeLinkedContainer(skyContainer);

                                    hopperManager.saveSkyHopperToPDC(skyHopper);

                                    guiManager.closeOutputFilterGUIs(location);

                                    guiManager.refreshViewersGUI(location);

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
            GUIConfig.Button buttonConfig = guiConfig.entries().nextPage();
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the next page button in the links gui due to no slot configured."));
                return;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(optionalItemStack.get());

                builder.setAction(event -> {
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
            GUIConfig.Button buttonConfig = guiConfig.entries().previousPage();
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the previous page button in the links gui due to no slot configured."));
                return;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(optionalItemStack.get());

                builder.setAction(event -> {
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
        GUIConfig.Button buttonConfig = guiConfig.entries().link();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the link button in the links gui due to no slot configured."));
            return;
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();

            guiButtonBuilder.setItemStack(optionalItemStack.get());

            guiButtonBuilder.setAction(inventoryClickEvent -> {
                skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () -> {
                    player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                    guiManager.removeViewer(location, uuid);
                }, 1L);

                if(skyHopper.getLocation() != null) {
                    hopperClickListener.addLinkingPlayer(player, skyHopper.getLocation());

                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.linkingEnabled()));
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.linkingHowToExit()));
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
        GUIConfig.Button buttonConfig = guiConfig.entries().exit();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the exit button in the links gui due to no slot configured."));
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
