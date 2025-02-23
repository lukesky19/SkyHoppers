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
package com.github.lukesky19.skyHoppers.util;

import com.github.lukesky19.skyHoppers.hook.stacker.RoseStackerHook;
import com.github.lukesky19.skyHoppers.manager.HookManager;
import org.bukkit.entity.Item;

/**
 * Methods used to interface with RoseStacker.
 */
public class RoseStackerUtils {
    /**
     * Removes an amount from the ground Item or deletes the Item Entity if all items would be removed.
     * @param item The Item Entity to remove items from.
     * @param itemAmount The Item Entity's amount.
     * @param removeAmount The amount to remove.
     */
    public static void removeAmountFromGroundItem(Item item, int itemAmount, int removeAmount) {
        int updatedAmount = itemAmount - removeAmount;

        if(updatedAmount > 0) {
            setItemAmount(item, updatedAmount);
        } else {
            item.remove();
        }
    }

    /**
     * Get the amount of Items in an Item Entity.
     * @param item The Item entity.
     * @return The amount in the Item Entity
     */
    public static int getItemAmount(Item item) {
        RoseStackerHook roseStackerHook = HookManager.getRoseStackerHook();

        if(roseStackerHook != null) return roseStackerHook.getItemAmount(item);

        return item.getItemStack().getAmount();
    }

    /**
     * Sets the amount of Items inside an Item Entity.
     * @param item The Item Entity.
     * @param amount The amount to set.
     */
    public static void setItemAmount(Item item, int amount) {
        RoseStackerHook roseStackerHook = HookManager.getRoseStackerHook();

        if (roseStackerHook != null) {
            roseStackerHook.setItemAmount(item, amount);
            return;
        }

        item.getItemStack().setAmount(amount);
    }
}
