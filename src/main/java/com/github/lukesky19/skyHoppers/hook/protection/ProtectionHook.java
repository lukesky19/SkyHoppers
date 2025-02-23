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
package com.github.lukesky19.skyHoppers.hook.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * An interface for hooking into plugins that offer protection.
 */
public interface ProtectionHook {
    /**
     * Checks if the Player can build at the given Location.
     * @param player The Player trying to build.
     * @param location The Location the Player is trying to build.
     * @return true if the player can, false if not.
     */
    boolean canPlayerBuild(Player player, Location location);

    /**
     * Checks if the Player can open Containers (i.e., Chests) at the given Location.
     * @param player The Player trying to open containers.
     * @param location The Location the Player is trying to access.
     * @return true if the player can, false if not.
     */
    boolean canPlayerOpen(Player player, Location location);
}
