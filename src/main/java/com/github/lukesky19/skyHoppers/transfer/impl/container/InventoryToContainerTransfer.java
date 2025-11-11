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
package com.github.lukesky19.skyHoppers.transfer.impl.container;

import com.github.lukesky19.skyHoppers.hook.impl.quickshop.QuickShopHook;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.transfer.TransferLogic;
import com.github.lukesky19.skyHoppers.transfer.impl.brewing.InventoryToBrewingStandTransfer;
import com.github.lukesky19.skyHoppers.transfer.impl.crafter.InventoryToCrafterTransfer;
import com.github.lukesky19.skyHoppers.transfer.impl.furnace.InventoryToFurnaceTransfer;
import com.github.lukesky19.skyHoppers.transfer.impl.generic.InventoryToInventoryTransfer;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.block.Crafter;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles transferring items from an inventory to a container.
 */
public class InventoryToContainerTransfer extends TransferLogic {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public InventoryToContainerTransfer() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Transfer items from source inventory to the destination inventory until amount is exhausted or there are no more items to transfer.
     * @apiNote This will call the proper method to process based on the destination inventory and destination {@link Container} and update any QuickShop-Hikari shop signs if applicable.
     * @param quickShopHook A {@link QuickShopHook} instance.
     * @param sourceInventory The {@link Inventory} to transfer from.
     * @param destinationSkyHopper The {@link SkyHopper} being transferred to. May be null.
     * @param destinationSkyContainer The {@link SkyContainer} being transferred to. May be null.
     * @param destinationContainer The {@link Container} being transferred to.
     * @param destinationInventory The {@link Inventory} being transferred to.
     * @param amount The amount of items to transfer.
     * @return The amount of items transferred.
     */
    public static int transfer(
            @NotNull QuickShopHook quickShopHook,
            @NotNull Inventory sourceInventory,
            @Nullable SkyHopper destinationSkyHopper,
            @Nullable SkyContainer destinationSkyContainer,
            @NotNull Container destinationContainer,
            @NotNull Inventory destinationInventory,
            int amount) {
        int result;
        if(destinationContainer instanceof Crafter crafter) {
            result = InventoryToCrafterTransfer.transfer(sourceInventory, destinationSkyHopper, destinationSkyContainer, crafter, destinationInventory, amount);
        } else {
            result = switch (destinationInventory) {
                case FurnaceInventory furnaceInventory -> InventoryToFurnaceTransfer.transfer(sourceInventory, destinationSkyHopper, destinationSkyContainer, furnaceInventory, amount);
                case BrewerInventory brewerInventory -> InventoryToBrewingStandTransfer.transfer(sourceInventory, destinationSkyHopper, destinationSkyContainer, brewerInventory, amount);
                default -> InventoryToInventoryTransfer.transfer(sourceInventory, destinationSkyHopper, destinationSkyContainer, destinationInventory, amount);
            };
        }

        if(result > 0) {
            @Nullable Location sourceLocation = sourceInventory.getLocation();
            @Nullable Location destinationLocation = destinationInventory.getLocation();

            if(sourceLocation != null) quickShopHook.updateShopSign(sourceLocation);
            if(destinationLocation != null) quickShopHook.updateShopSign(destinationLocation);
        }

        return result;
    }
}