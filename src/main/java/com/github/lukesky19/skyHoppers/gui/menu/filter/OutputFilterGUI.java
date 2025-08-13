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
package com.github.lukesky19.skyHoppers.gui.menu.filter;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.data.config.gui.ButtonConfig;
import com.github.lukesky19.skyHoppers.manager.GUIConfigManager;
import com.github.lukesky19.skyHoppers.data.config.gui.GUIConfig;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.gui.menu.links.LinksGUI;
import com.github.lukesky19.skyHoppers.hopper.*;
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
import java.util.UUID;

/**
 * This class lets Players manage the output filter items and filter type.
 */
public class OutputFilterGUI extends SkyHopperGUI {
    private final @NotNull HopperManager hopperManager;
    private final @NotNull LinksGUI linksGUI;

    private final @NotNull SkyHopper skyHopper;
    private final @NotNull SkyContainer skyContainer;

    private final @Nullable GUIConfig guiConfig;

    private int itemNum = 0;
    private int added = 0;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param location The {@link Location} of the {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper} the GUI is associated with.
     * @param player The {@link Player} viewing the GUI.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A {@link HopperManager} instance.
     * @param skyContainer The {@link SkyContainer} being modified.
     * @param linksGUI The {@link LinksGUI} the Player came from.
     */
    public OutputFilterGUI(
            @NotNull SkyHoppers skyHoppers,
            @NotNull GUIManager guiManager,
            @NotNull Location location,
            @NotNull SkyHopper skyHopper,
            @NotNull Player player,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull HopperManager hopperManager,
            @NotNull SkyContainer skyContainer,
            @NotNull LinksGUI linksGUI) {
        super(skyHoppers, guiManager, player, location);

        this.hopperManager = hopperManager;
        this.linksGUI = linksGUI;

        this.skyHopper = skyHopper;
        this.skyContainer = skyContainer;

        guiConfig = guiConfigManager.getGuiConfig("output_filter.yml");
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(guiConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the output_filter.yml GUI due to invalid GUI configuration."));
            return false;
        }

        GUIType guiType = guiConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the output_filter.yml GUI due to an invalid GUIType"));
            return false;
        }

        String guiName = guiConfig.name();
        if(guiName == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the output_filter.yml GUI due to an invalid gui name."));
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
            logger.warn(AdventureUtil.serialize("Unable to decorate the GUI due to invalid configuration for the output filter GUI."));
            return false;
        }

        clearButtons();

        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to update the output filter GUI as the InventoryView was not created."));
            if(isOpen) close();
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();
        int maxItems = skyContainer.getFilterItems().size() - 1;

        createFiller(guiSize);
        // Dummy Buttons
        createDummyButtons();
        createFilterItems(guiSize, maxItems);
        createNextPageButton(guiSize, maxItems);
        createPreviousPageButton(guiSize);
        createFilterTypeButton();
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
     * Close the current GUI and open the {@link LinksGUI} that the player came from.
     */
    @Override
    public void close() {
        super.close();

        linksGUI.update();

        linksGUI.open();
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

        guiManager.removeViewer(location, uuid);

        this.isOpen = false;

        linksGUI.update();

        linksGUI.open();
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
            logger.warn(AdventureUtil.serialize("Unable to add item to the output filter due to an invalid ItemType for Material " + FormatUtil.formatMaterialName(material)));
            return;
        }

        // Check if the item is already filtered
        if(!skyContainer.getFilterItems().contains(itemType)) return;

        // Add the ItemType to the filter
        skyContainer.addFilterItem(itemType);

        hopperManager.saveSkyHopperToPDC(skyHopper);

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
    private void createFilterItems(int guiSize, int maxItems) {
        if(guiSize - 10 >= 17) {
            assert guiConfig != null;
            ButtonConfig buttonConfig = guiConfig.entries().filterItem();
            List<Component> lore = buttonConfig.item().lore().stream().map(AdventureUtil::serialize).toList();
            List<ItemFlag> itemFlags = buttonConfig.item().itemFlags().stream().map(ItemFlag::valueOf).toList();

            for (int i = 0; i <= guiSize - 10; i++) {
                if(maxItems >= itemNum) {
                    ItemType itemType = skyHopper.getFilterItems().get(itemNum);

                    GUIButton.Builder builder = new GUIButton.Builder();

                    ItemStack itemStack = itemType.createItemStack();
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    itemMeta.displayName(AdventureUtil.serialize(FormatUtil.formatItemTypeName(itemType)));

                    itemMeta.lore(lore);
                    itemFlags.forEach(itemMeta::addItemFlags);

                    itemStack.setItemMeta(itemMeta);

                    builder.setItemStack(itemStack);

                    builder.setAction(event -> {
                        ItemStack currentItem = event.getCurrentItem();
                        if(currentItem != null) {
                            ItemType currentItemType = currentItem.getType().asItemType();
                            if(currentItemType != null) {
                                skyContainer.removeFilterItem(currentItemType);

                                hopperManager.saveSkyHopperToPDC(skyHopper);

                                guiManager.refreshViewersGUI(location);

                                added = 0;
                                itemNum = 0;

                                update();
                            }
                        }
                    });

                    setButton(i, builder.build());

                    itemNum++;
                    added++;
                }
            }
        }
    }

    /**
     * Creates the next page button if needed
     */
    private void createNextPageButton(int guiSize, int maxItems) {
        if(added > guiSize - 10 && (itemNum - 1) < maxItems) {
            assert guiConfig != null;
            ButtonConfig buttonConfig = guiConfig.entries().nextPage();
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to create the next page button in the output filter gui due to no slot configured."));
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
                logger.warn(AdventureUtil.serialize("Unable to create the previous page button in the output filter gui due to no slot configured."));
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
     */
    private void createFilterTypeButton() {
        assert guiConfig != null;
        ButtonConfig buttonConfig = guiConfig.entries().filter();

        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the filter button in the output filter gui due to no slot configured."));
            return;
        }

        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("filter_type", skyContainer.getFilterType().name()));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(buttonConfig.item(), null, null, placeholders);
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        if(optionalItemStack.isPresent()) {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(optionalItemStack.get());

            builder.setAction(event -> {
                FilterType updatedFilterType = getUpdatedFilterType();
                skyContainer.setFilterType(updatedFilterType);

                hopperManager.saveSkyHopperToPDC(skyHopper);

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
            logger.warn(AdventureUtil.serialize("Unable to create the exit button in the output filter gui due to no slot configured."));
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
                logger.warn(AdventureUtil.serialize("Unable to add a dummy button to the output filter GUI due to an invalid slot."));
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
     * @return The next {@link FilterType}.
     */
    private @NotNull FilterType getUpdatedFilterType() {
        FilterType updatedFilterType;

        switch(skyContainer.getFilterType()) {
            case NONE -> updatedFilterType = FilterType.WHITELIST;

            case WHITELIST -> updatedFilterType = FilterType.BLACKLIST;

            case BLACKLIST -> updatedFilterType = FilterType.DESTROY;

            default -> updatedFilterType = FilterType.NONE; // This also sets the filter type DESTROY to NONE
        }

        return updatedFilterType;
    }
}
