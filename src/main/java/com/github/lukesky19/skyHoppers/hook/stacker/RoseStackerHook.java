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
package com.github.lukesky19.skyHoppers.hook.stacker;

import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedItem;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles hooking into RoseStacker.
 */
public class RoseStackerHook {
    /**
     * Gets the amount of items in a StackedItem or the amount in the ItemStack if RoseStacker isn't enabled.
     * @param item The Item Entity to get the amount for.
     * @return The item amount.
     */
    public int getItemAmount(Item item) {
        @Nullable StackedItem stackedItem = RoseStackerAPI.getInstance().getStackedItem(item);

        if(stackedItem != null) return stackedItem.getStackSize();

        return item.getItemStack().getAmount();
    }

    /**
     * Sets the amount of items in a StackedItem or the ItemStack if RoseStacker isn't enabled.
     * @param item The Item Entity to set the amount for.
     * @param amount The item amount to set.
     */
    public void setItemAmount(Item item, int amount) {
        @Nullable StackedItem stackedItem = RoseStackerAPI.getInstance().getStackedItem(item);
        if (stackedItem != null) {
            stackedItem.setStackSize(amount);
            return;
        }

        item.getItemStack().setAmount(amount);
    }
}
