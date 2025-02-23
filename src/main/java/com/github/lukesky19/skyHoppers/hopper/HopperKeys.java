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
package com.github.lukesky19.skyHoppers.hopper;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * The SkyHopper settings keys that are used to retrive and store SkyHopper data to the PersistentDataContainer
 */
public enum HopperKeys {
    ENABLED,
    PARTICLES,
    LINKED,
    LINKS,
    LOCATION,
    FILTER_TYPE,
    FILTER_ITEMS,
    OWNER,
    MEMBERS,
    TRANSFER_SPEED,
    MAX_TRANSFER_SPEED,
    TRANSFER_AMOUNT,
    MAX_TRANSFER_AMOUNT,
    SUCTION_SPEED,
    MAX_SUCTION_SPEED,
    SUCTION_AMOUNT,
    MAX_SUCTION_AMOUNT,
    SUCTION_RANGE,
    MAX_SUCTION_RANGE,
    MAX_CONTAINERS;

    private final NamespacedKey key;

    /**
     * Creates a new NamespacedKey for the given HopperKey.
     */
    HopperKeys() {
        this.key = new NamespacedKey("skyhoppers", this.name().toLowerCase());
    }

    /**
     * Gets the NamespacedKey for the given HopperKey.
     * @return A NamespacedKey
     */
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }
}
