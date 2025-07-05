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
package com.github.lukesky19.skyHoppers.listener;

import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.manager.GUIManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Listens to Inventory related methods to pass the events to open GUIs.
 */
public class InventoryListener implements Listener {
    private final @NotNull GUIManager guiManager;

    /**
     * Constructor
     * @param guiManager A {@link GUIManager} instance.
     */
    public InventoryListener(@NotNull GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    /**
     * Passes the InventoryClickEvent to the player's open GUI.
     * @param inventoryClickEvent An InventoryClickEvent.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        UUID uuid = inventoryClickEvent.getWhoClicked().getUniqueId();
        Inventory inventory = inventoryClickEvent.getClickedInventory();

        SkyHopperGUI gui = guiManager.getGuiByUUID(uuid);
        if(gui == null) return;

        gui.handleGlobalClick(inventoryClickEvent);

        if(inventory instanceof PlayerInventory) {
            gui.handleBottomClick(inventoryClickEvent);
        } else {
            gui.handleTopClick(inventoryClickEvent);
        }
    }

    /**
     * Passes the InventoryDragEvent to the player's open GUI.
     * @param inventoryDragEvent An InventoryDragEvent.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        UUID uuid = inventoryDragEvent.getWhoClicked().getUniqueId();
        Inventory inventory = inventoryDragEvent.getInventory();

        SkyHopperGUI gui = guiManager.getGuiByUUID(uuid);
        if(gui == null) return;

        gui.handleGlobalDrag(inventoryDragEvent);

        if(inventory instanceof PlayerInventory) {
            gui.handleBottomDrag(inventoryDragEvent);
        } else {
            gui.handleTopDrag(inventoryDragEvent);
        }
    }

    /**
     * Passes the InventoryCloseEvent to the player's open GUI.
     * @param inventoryCloseEvent An InventoryCloseEvent.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        UUID uuid = inventoryCloseEvent.getPlayer().getUniqueId();

        SkyHopperGUI gui = guiManager.getGuiByUUID(uuid);
        if (gui != null) {
            gui.handleClose(inventoryCloseEvent);
        }
    }
}
