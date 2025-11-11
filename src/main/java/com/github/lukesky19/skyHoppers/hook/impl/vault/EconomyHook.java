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
package com.github.lukesky19.skyHoppers.hook.impl.vault;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.hook.interfaces.Hook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles hooking into and interfacing with Vault and its Economy API.
 */
public class EconomyHook implements Hook {
    private final @NotNull SkyHoppers skyHoppers;
    private @Nullable Economy economy;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     */
    public EconomyHook(@NotNull SkyHoppers skyHoppers) {
        this.skyHoppers = skyHoppers;
    }

    /**
     * Attempt to get the {@link Economy} from Vault.
     */
    @Override
    public void initialize() {
        if(skyHoppers.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = skyHoppers.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
            }
        }
    }

    /**
     * Sets the {@link #economy} to null.
     */
    @Override
    public void deinitialize() {
        economy = null;
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return economy != null;
    }

    @Override
    public @NotNull String getHookName() {
        return "Economy";
    }

    /**
     * Remove the amount provided from the player's balance.
     * This method will prevent balances from going into the negative.
     * @param player The {@link Player}.
     * @param amount The amount to remove.
     * @apiNote If the economy was not hooked into, this method will do nothing. Can be checked with {@link #isHooked()}.
     */
    public void removeFromBalance(@NotNull Player player, double amount) {
        if(economy == null) return;

        double balance = economy.getBalance(player);
        if(balance - amount < 0) {
            economy.withdrawPlayer(player, balance);
        } else {
            economy.withdrawPlayer(player, amount);
        }
    }

    /**
     * Get the player's balance.
     * @apiNote Will always return 0 if the economy was not hooked into. Can be checked with {@link #isHooked()}.
     * @param player The {@link Player} to get the economy for.
     * @return The player's balance or 0 if not hooked.
     */
    public double getBalance(@NotNull Player player) {
        if(economy == null) return 0;

        return economy.getBalance(player);
    }
}
