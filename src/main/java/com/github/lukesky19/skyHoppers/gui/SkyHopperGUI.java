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
import com.github.lukesky19.skyHoppers.manager.GUIManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.interfaces.ButtonGUI;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.builder.InventoryViewBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to create a GUI to interface with a SkyHopper.
 */
public abstract class SkyHopperGUI implements ButtonGUI {
    /**
     * A {@link SkyHoppers} instance.
     */
    protected final @NotNull SkyHoppers skyHoppers;
    /**
     * The plugin's {@link ComponentLogger} to log warnings or errors with.
     */
    protected final @NotNull ComponentLogger logger;
    /**
     * The {@link GUIManager} that the plugin is using to track open GUIs with.
     */
    protected final @NotNull GUIManager guiManager;

    /**
     * The {@link Map} to store the mapping of slots to {@link GUIButton}s for.
     */
    protected final @NotNull Map<Integer, GUIButton> slotButtons = new HashMap<>();

    /**
     * The {@link Player} to create the GUI for.
     */
    protected final @NotNull Player player;
    /**
     * The {@link UUID} of the {@link Player}.
     */
    protected final @NotNull UUID uuid;
    /**
     * The {@link Location} the GUI is associated with. This is the location of the SkyHopper.
     */
    protected final @NotNull Location location;
    /**
     * The {@link InventoryView} associated with this GUI.
     */
    protected @Nullable InventoryView inventoryView;
    /**
     * Is the GUI currently open?
     */
    protected boolean isOpen = false;

    /**
     * Constructor.
     * @param skyHoppers A {@link SkyHoppers} instance.
     * @param guiManager The {@link GUIManager} that is used to track open GUIs.
     * @param player The {@link Player} associated with the created GUI.
     * @param location The {@link Location} of the SkyHopper the GUI is for.
     */
    public SkyHopperGUI(@NotNull SkyHoppers skyHoppers, @NotNull GUIManager guiManager, @NotNull Player player, @NotNull Location location) {
        this.skyHoppers = skyHoppers;
        this.logger = skyHoppers.getComponentLogger();
        this.guiManager = guiManager;
        this.player = player;
        this.uuid = player.getUniqueId();
        this.location = location;
    }

    /**
     * Get the {@link InventoryView} associated with the GUI.
     * @return An {@link Optional} containing an {@link InventoryView}. If empty, that means {@link #create(GUIType, String, List)} was not called.
     */
    @Override
    public @NotNull Optional<@NotNull InventoryView> getInventoryView() {
        return Optional.ofNullable(inventoryView);
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @param guiType The {@link GUIType} for this GUI. Only CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, and CHEST_54 are allowed.
     * @param name The name of the GUI to display in the InventoryView.
     * @param placeholders A {@link List} of {@link TagResolver.Single} for any placeholders in the GUI name.
     * @return true if created successfully, otherwise false.
     */
    @Override
    public boolean create(@NotNull GUIType guiType, @NotNull String name, @NotNull List<TagResolver.Single> placeholders) {
        switch(guiType) {
            case CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54 -> {
                // Create the InventoryViewBuilder
                InventoryViewBuilder<@NotNull InventoryView> inventoryViewBuilder = guiType.getMenuType().typed().builder();

                // Set the title of the InventoryView/GUI
                inventoryViewBuilder.title(AdventureUtil.serialize(player, name, placeholders));

                // Build the InventoryView
                inventoryView = inventoryViewBuilder.build(player);

                return true;
            }

            default -> {
                // If the GUIType provided is unsupported, log a warning and return false.
                logger.warn(AdventureUtil.serialize("Unsupported GUIType provided."));
                return false;
            }
        }
    }

    /**
     * Open the GUI for the {@link Player} the InventoryView was created with.
     * @return true if opened successfully, otherwise false.
     */
    @Override
    public boolean open() {
        if(inventoryView == null) {
            // If the InventoryView was not created, log a warning and return false.
            logger.warn(AdventureUtil.serialize("Unable to open the InventoryView as it was not created."));
            return false;
        }

        // Close the current Inventory the player has open (if any)
        skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () -> {
            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

            guiManager.removeViewer(location, uuid);
        }, 1L);

        // Then 1 tick later, open the GUI and track that it is open for the player.
        skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () -> {
            inventoryView.open();

            guiManager.addViewer(location, uuid, this);
        }, 2L);

        isOpen = true;

        return true;
    }

    /**
     * Close the GUI with an UNLOADED {@link InventoryCloseEvent.Reason}.
     * You should use {@link #unload(boolean)} if the plugin is being disabled, and you are trying to close open GUIs.
     */
    @Override
    public void close() {
        skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () ->
                player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);

        guiManager.removeViewer(location, uuid);

        this.isOpen = false;
    }

    /**
     * Close the GUI with an UNLOADED {@link InventoryCloseEvent.Reason}.
     * If the plugin is being disabled, the scheduler won't be used as it is unavailable during server shutdown.
     * @param onDisable Is the plugin being disabled?
     */
    @Override
    public void unload(boolean onDisable) {
        if(!onDisable) {
            skyHoppers.getServer().getScheduler().runTaskLater(skyHoppers, () -> {
                player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                guiManager.removeViewer(location, uuid);
            }, 1L);
        } else {
            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

            guiManager.removeViewer(location, uuid);
        }

        this.isOpen = false;
    }

    /**
     * This method takes the mapping of slots to {@link GUIButton} and populates the {@link ItemStack}s in the InventoryView.
     * @apiNote You should overwrite this method to contain the code that actually creates the {@link GUIButton}s and
     * maps it to slots using {@link #setButton(int, GUIButton)} and then call this method using super.update().<br><br>
     * While this method returns a {@link CompletableFuture} to support asynchronous operations, by default this method executes synchronously.
     * @return A {@link CompletableFuture} containing true if successful, otherwise false.
     */
    @Override
    public boolean update() {
        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to add button ItemStacks to the InventoryView as it was not created."));
            return false;
        }

        // Check if any slots are out-of-bounds for the InventoryView's GUI size.
        List<Integer> invalidSlots = new ArrayList<>();
        slotButtons.forEach((slot, button) -> {
            if (slot < 0 || slot >= inventoryView.getTopInventory().getSize()) {
                invalidSlots.add(slot);
            }
        });

        // If any slots were out-of-bounds for the InventoryView's GUI size, log a warning and return false.
        if(!invalidSlots.isEmpty()) {
            logger.warn(AdventureUtil.serialize("Button Mapping has buttons for slots outside of inventory bounds: " + invalidSlots));
            return false;
        }

        // Add each button's ItemStack to the InventoryView
        slotButtons.forEach((slot, button) -> inventoryView.setItem(slot, button.itemStack()));

        // return true that all buttons were added successfully.
        return true;
    }

    @Override
    public abstract void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent);

    @Override
    public void handleTopDrag(@NotNull InventoryDragEvent inventoryDragEvent) {
        inventoryDragEvent.setCancelled(true);
    }

    @Override
    public abstract void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent);

    @Override
    public abstract void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent);

    @Override
    public void handleTopClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
        int slot = inventoryClickEvent.getSlot();

        GUIButton button = slotButtons.get(slot);
        if(button != null) {
            button.action().accept(inventoryClickEvent);
        }
    }

    @Override
    public abstract void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent);

    @Override
    public abstract void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent);

    /**
     * Clear the {@link InventoryView} of all {@link ItemStack}s associated with {@link GUIButton}s.
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean clearButtons() {
        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to clear buttons as the InventoryView was not created."));
            return false;
        }

        ItemStack clearStack = new ItemStack(Material.AIR);
        for(int slot = 0; slot <= (inventoryView.getTopInventory().getSize() - 1); slot++) {
            if(slotButtons.containsKey(slot)) {
                inventoryView.setItem(slot, clearStack);
            }
        }

        slotButtons.clear();
        return true;
    }

    /**
     * Clear the {@link InventoryView} of all {@link ItemStack}s from any source. Will also clear the {@link #slotButtons} {@link Map}.
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean clearInventory()  {
        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to clear buttons as the InventoryView was not created."));
            return false;
        }

        ItemStack clearStack = new ItemStack(Material.AIR);
        for(int slot = 0; slot <= (inventoryView.getTopInventory().getSize() - 1); slot++) {
            inventoryView.setItem(slot, clearStack);
        }

        slotButtons.clear();
        return true;
    }

    /**
     * Adds a {@link GUIButton} to the {@link #slotButtons} mapping for the provided slot. If there is already data mapped to that slot, it will be overwritten.
     * @apiNote You need to call {@link #update()} to actually populate the GUI with the {@link ItemStack}s.
     * @param slot The slot to map the {@link GUIButton} to.
     * @param button The {@link GUIButton} for the given slot.
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean setButton(int slot, @NotNull GUIButton button) {
        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to add the slot and button to the button mapping as the InventoryView was not created."));
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();
        if(slot < 0 || slot >= guiSize) {
            logger.warn(AdventureUtil.serialize("Provided slot is outside of inventory bounds. Slot must be greater than 0 and less than " + guiSize));
            return false;
        }

        slotButtons.put(slot, button);
        return true;
    }

    /**
     * Replaces the current mapping of {@link GUIButton}s to slots with the provided mapping. This will overwrite any existing data by clearing the current mapping first.
     * @apiNote You need to call {@link #update()} to actually populate the GUI with the {@link ItemStack}s.
     * @param buttonMap The mapping of slots and {@link GUIButton}s.
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean setButtons(@NotNull Map<Integer, GUIButton> buttonMap) {
        if(clearButtons()) {
            slotButtons.putAll(buttonMap);
            return true;
        } else {
            logger.warn(AdventureUtil.serialize("Unable to replace the slot-button mapping as existing buttons failed to be cleared."));
            return false;
        }
    }
}
