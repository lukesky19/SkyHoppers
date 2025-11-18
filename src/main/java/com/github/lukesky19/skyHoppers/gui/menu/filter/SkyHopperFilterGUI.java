/*
    SkyHoppers adds upgradable hoppers that can suction items, transfer items wirelessly to linked containers.
    Copyright (C) 2025 lukeskywlker19

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
package com.github.lukesky19.skyHoppers.gui.menu.filter;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.GUIConfigManager;
import com.github.lukesky19.skyHoppers.config.data.gui.ButtonConfig;
import com.github.lukesky19.skyHoppers.config.data.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.gui.GUIManager;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.gui.menu.HopperGUI;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.Filterable.FilterType;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class lets Players manage a SkyHopper's filter items and filter type.
 */
public class SkyHopperFilterGUI extends SkyHopperGUI {
    private final @NotNull SkyHopperManager hopperManager;

    private final @NotNull SkyHopper skyHopper;

    private final @Nullable GUIConfig guiConfig;

    private int itemNum = 0;
    private int added = 0;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param location The {@link Location} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper}.
     * @param player The {@link Player} viewing the GUI.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A {@link SkyHopperManager} instance.
     * @param hopperGUI The {@link HopperGUI} the Player came from.
     */
    public SkyHopperFilterGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull Location location,
            @NotNull SkyHopper skyHopper,
            @NotNull Player player,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull SkyHopperManager hopperManager,
            @NotNull HopperGUI hopperGUI) {
        super(skyHoppers, guiManager, player, location, hopperGUI);

        this.hopperManager = hopperManager;

        this.skyHopper = skyHopper;

        guiConfig = guiConfigManager.getGuiConfig("input_filter.yml");
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the input_filter.yml GUI due to invalid GUI configuration."));
            return false;
        }

        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the input_filter.yml GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = guiConfig.name();
        if(guiName == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the input_filter.yml GUI due to an invalid gui name."));
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
            logger.warn(AdventureUtil.deserialize("Unable to decorate the GUI due to invalid configuration for the input filter GUI."));
            if(isOpen) close();
            return false;
        }

        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to update the input filter GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        clearButtons();

        int guiSize = inventoryView.getTopInventory().getSize();

        createFiller(guiSize);
        // Dummy Buttons
        createDummyButtons();
        createFilterItems(guiSize);
        createNextPageButton(guiSize);
        createPreviousPageButton(guiSize);
        createFilterButton(skyHopper);
        createExitButton();

        return super.update();
    }

    /**
     * Refreshes the buttons in the GUI.
     */
    @Override
    public boolean refresh() {
        added = 0;
        itemNum = 0;

        return update();
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

        if(previousGUI != null) {
            previousGUI.update();

            previousGUI.open();
        }
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
     * Handles when a Player clicks an Item inside their inventory to add to the filter.
     * @param inventoryClickEvent An InventoryClickEvent
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        // Get the clicked ItemStack
        ItemStack clickedItemStack = inventoryClickEvent.getCurrentItem();
        if(clickedItemStack == null) return;

        // Check if the Material is AIR
        Material material = clickedItemStack.getType();
        if(material.equals(Material.AIR)) return;

        // Get the ItemType
        ItemType itemType = material.asItemType();
        if(itemType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add an item to the filter as there is no ItemType for Material " + FormatUtil.formatMaterialName(material)));
            return;
        }

        // Add the ItemType to the filter
        skyHopper.addFilterItem(itemType);

        hopperManager.getSkyHopperSaver().saveSkyHopper(skyHopper);

        guiManager.refreshViewersGUI(location);

        added = 0;
        itemNum = 0;

        update();
    }

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
     * Creates all the buttons for items in the filtered items list
     */
    private void createFilterItems(int guiSize) {
        int maxItems = skyHopper.getFilterItems().size() - 1;

        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().filterItem();
        List<Component> lore = buttonConfig.item().lore().stream().map(AdventureUtil::deserialize).toList();
        List<ItemFlag> itemFlags = buttonConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

        if(guiSize - 10 >= 17) {
            for(int i = 0; i <= guiSize - 10; i++) {
                if(maxItems >= itemNum) {
                    ItemType itemType = skyHopper.getFilterItems().get(itemNum);

                    GUIButton.Builder builder = new GUIButton.Builder();

                    ItemStack itemStack = itemType.createItemStack();
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    itemMeta.displayName(AdventureUtil.deserialize(FormatUtil.formatItemTypeName(itemType)));

                    itemMeta.lore(lore);
                    itemFlags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    builder.setItemStack(itemStack);

                    builder.setAction(event -> {
                        ItemStack currentItem = event.getCurrentItem();
                        if(currentItem != null) {
                            ItemType currentItemType = currentItem.getType().asItemType();
                            if(currentItemType != null) {
                                skyHopper.removeFilterItem(currentItemType);

                                hopperManager.getSkyHopperSaver().saveSkyHopper(skyHopper);

                                guiManager.refreshViewersGUI(location);

                                added = 0;
                                itemNum = 0;

                                update();
                            }
                        }
                    });

                    setButton(i, builder.build());

                    added++;
                    itemNum++;
                }
            }
        }
    }

    /**
     * Creates the next page button if needed
     */
    private void createNextPageButton(int guiSize) {
        int maxItems = skyHopper.getFilterItems().size() - 1;

        if(added > guiSize - 10 && (itemNum - 1) < maxItems) {
            assert guiConfig != null;
            ButtonConfig buttonConfig = guiConfig.entries().nextPage();
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to create the next page button in the input filter gui due to no slot configured."));
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
        if(itemNum > guiSize - 9) {
            assert guiConfig != null;
            ButtonConfig buttonConfig = guiConfig.entries().previousPage();
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to create the previous page button in the input filter gui due to no slot configured."));
                return;
            }

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, List.of());
            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

            if(optionalItemStack.isPresent()) {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(optionalItemStack.get());

                builder.setAction(event -> {
                    if (itemNum > (guiSize - 9) + added) {
                        itemNum -= (guiSize - 9) + added;
                    } else {
                        itemNum -= added;
                    }

                    added = 0;

                    update();
                });

                setButton(buttonConfig.slot(), builder.build());
            }
        }
    }

    /**
     * Creates the filter type button
     * @param skyHopper The {@link SkyHopper} associated with this GUI
     */
    private void createFilterButton(@NotNull SkyHopper skyHopper) {
        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().filter();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the filter button in the input filter gui due to no slot configured."));
            return;
        }

        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("filter_type", skyHopper.getFilterType().name()));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, placeholders);
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                FilterType updatedFilterType = getUpdatedFilterType(skyHopper);
                skyHopper.setFilterType(updatedFilterType);

                hopperManager.getSkyHopperSaver().saveSkyHopper(skyHopper);

                guiManager.refreshViewersGUI(location);

                added = 0;
                itemNum = 0;

                update();
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
            logger.warn(AdventureUtil.deserialize("Unable to create the exit button in the input filter gui due to no slot configured."));
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
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(guiConfig == null) return;

        guiConfig.entries().dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a dummy button to the input filter GUI due to an invalid slot."));
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
     * Gets the next {@link FilterType} in the rotation.
     * @param skyHopper The {@link SkyHopper} associated with this GUI.
     * @return The next {@link FilterType}.
     */
    private @NotNull FilterType getUpdatedFilterType(@NotNull SkyHopper skyHopper) {
        FilterType updatedFilterType;

        switch(skyHopper.getFilterType()) {
            case NONE -> updatedFilterType = FilterType.WHITELIST;

            case WHITELIST -> updatedFilterType = FilterType.BLACKLIST;

            case BLACKLIST -> updatedFilterType = FilterType.DESTROY;

            default -> updatedFilterType = FilterType.NONE; // This also sets the filter type DESTROY to NONE
        }

        return updatedFilterType;
    }
}
