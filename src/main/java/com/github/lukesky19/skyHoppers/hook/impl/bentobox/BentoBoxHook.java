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
package com.github.lukesky19.skyHoppers.hook.impl.bentobox;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.hook.interfaces.ProtectionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;
import world.bentobox.bentobox.managers.IslandsManager;

import java.util.Optional;

/**
 * This class handles hooking into and interfacing with BentoBox.
 */
public class BentoBoxHook implements ProtectionHook {
    private final @NotNull SkyHoppers skyHoppers;
    private @Nullable BentoBox bentoBox;
    private @Nullable IslandsManager islandsManager;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     */
    public BentoBoxHook(@NotNull SkyHoppers skyHoppers) {
        this.skyHoppers = skyHoppers;
    }

    @Override
    public void initialize() {
        @Nullable Plugin plugin = skyHoppers.getServer().getPluginManager().getPlugin("BentoBox");
        if(plugin != null && plugin.isEnabled()) {
            this.bentoBox = BentoBox.getInstance();
            this.islandsManager = bentoBox.getIslandsManager();
        }
    }

    @Override
    public void deinitialize() {
        bentoBox = null;
        islandsManager = null;
    }

    @Override
    public boolean isHooked() {
        return bentoBox != null && islandsManager != null;
    }

    /**
     * Returns "BentoBox".
     * @return "BentoBox"
     */
    @Override
    public @NotNull String getHookName() {
        return "BentoBox";
    }

    @Override
    public boolean canPlayerBuild(Player player, Location location) {
        if(bentoBox == null || islandsManager == null) return true;

        Optional<Island> optionalIsland = islandsManager.getIslandAt(location);
        if(optionalIsland.isEmpty()) return true;

        Island island = optionalIsland.orElseThrow();
        User user = bentoBox.getPlayers().getUser(player.getUniqueId());
        return island.isAllowed(user, Flags.BREAK_BLOCKS) && island.isAllowed(user, Flags.PLACE_BLOCKS);
    }

    @Override
    public boolean canPlayerOpen(Player player, Location location) {
        if(bentoBox == null || islandsManager == null) return true;

        Optional<Island> optionalIsland = islandsManager.getIslandAt(location);
        if(optionalIsland.isEmpty()) return true;

        Island island = optionalIsland.orElseThrow();
        User user = bentoBox.getPlayers().getUser(player.getUniqueId());
        return island.isAllowed(user, Flags.CONTAINER);
    }
}
