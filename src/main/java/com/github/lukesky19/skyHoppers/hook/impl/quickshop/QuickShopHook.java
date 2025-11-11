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
package com.github.lukesky19.skyHoppers.hook.impl.quickshop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.QuickShopBukkit;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.hook.interfaces.Hook;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles hooking into and interfacing with QuickShop-Hikari.
 */
public class QuickShopHook implements Hook {
    private final @NotNull SkyHoppers skyHoppers;
    private @Nullable QuickShop quickShop;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     */
    public QuickShopHook(@NotNull SkyHoppers skyHoppers) {
        this.skyHoppers = skyHoppers;
    }

    /**
     * Attempt to get the {@link RoseStackerAPI} from RoseStacker.
     */
    @Override
    public void initialize() {
        @Nullable Plugin plugin = skyHoppers.getServer().getPluginManager().getPlugin("QuickShop-Hikari");
        if(plugin != null && plugin.isEnabled()) {
            quickShop = ((QuickShopBukkit) QuickShopAPI.getPluginInstance()).getQuickShop();
        }
    }

    /**
     * Sets the {@link #quickShop} to null.
     */
    @Override
    public void deinitialize() {
        quickShop = null;
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return quickShop != null;
    }

    /**
     * Returns "QuickShop".
     * @return "QuickShop"
     */
    @Override
    public @NotNull String getHookName() {
        return "QuickShop";
    }

    /**
     * Update the shop's sign at the provided {@link Location}.
     * @apiNote Will do nothing if QuickShop-Hikari is not hooked into or there is no shop at the location.
     * @param location The {@link Location} of the shop.
     */
    public void updateShopSign(@NotNull Location location) {
        if(quickShop == null) return;

        Shop shop = quickShop.getShopManager().getShopIncludeAttached(location);
        if(shop == null) return;

        shop.setSignText(quickShop.text().findRelativeLanguages(shop.getOwner(), false));
    }
}
