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
package com.github.lukesky19.skyHoppers.hook.impl.rosestacker;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.hook.interfaces.Hook;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedItem;
import org.bukkit.entity.Item;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles hooking into and interfacing with RoseStacker.
 */
public class RoseStackerHook implements Hook {
    private final @NotNull SkyHoppers skyHoppers;
    private @Nullable RoseStackerAPI roseStackerAPI;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     */
    public RoseStackerHook(@NotNull SkyHoppers skyHoppers) {
        this.skyHoppers = skyHoppers;
    }

    /**
     * Attempt to get the {@link RoseStackerAPI} from RoseStacker.
     */
    @Override
    public void initialize() {
        @Nullable Plugin plugin = skyHoppers.getServer().getPluginManager().getPlugin("RoseStacker");
        if(plugin != null && plugin.isEnabled()) {
            roseStackerAPI = RoseStackerAPI.getInstance();
        }
    }

    /**
     * Sets the {@link #roseStackerAPI} to null.
     */
    @Override
    public void deinitialize() {
        roseStackerAPI = null;
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return roseStackerAPI != null;
    }

    /**
     * Returns "RoseStacker".
     * @return "RoseStacker"
     */
    @Override
    public @NotNull String getHookName() {
        return "RoseStacker";
    }

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
