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
package com.github.lukesky19.skyHoppers.skyhopper.data;

import com.github.lukesky19.skyHoppers.skyhopper.data.Filterable.FilterType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * This enum contains the {@link NamespacedKey}s that are used to retrieve and store {@link SkyHopper} data to the {@link PersistentDataContainer}.
 */
public enum HopperKeys {
    /**
     * The key the {@link SkyHopper}'s version is stored at.
     */
    VERSION,
    /**
     * The key that stores whether the {@link SkyHopper} is enabled or not.
     */
    ENABLED,
    /**
     * The key that stores whether the {@link SkyHopper}'s particles are enabled or disabled.
     */
    PARTICLES,
    /**
     * The key that stores the legacy linked container.
     */
    LINKED,
    /**
     * The key that stores the linked {@link SkyContainer}s.
     */
    LINKS,
    /**
     * The key that stores the {@link Location} of the {@link SkyHopper} or {@link SkyContainer}.
     */
    LOCATION,
    /**
     * The key that stores the {@link FilterType} of the {@link SkyHopper} or {@link SkyContainer}
     */
    FILTER_TYPE,
    /**
     * The key that stores the {@link ItemType}s filtered for the {@link SkyHopper} or {@link SkyContainer}.
     */
    FILTER_ITEMS,
    /**
     * The key that stores the {@link UUID} of the player that owns the {@link SkyHopper}.
     */
    OWNER,
    /**
     * The key that stores the {@link List} of {@link UUID}s that can access the {@link SkyHopper}.
     */
    MEMBERS,
    /**
     * The key that stores the transfer speed for the {@link SkyHopper}.
     */
    TRANSFER_SPEED,
    /**
     * The key that stores the max transfer speed for the {@link SkyHopper}.
     */
    MAX_TRANSFER_SPEED,
    /**
     * The key that stores the transfer amount for the {@link SkyHopper}.
     */
    TRANSFER_AMOUNT,
    /**
     * The key that stores the max transfer amount for the {@link SkyHopper}.
     */
    MAX_TRANSFER_AMOUNT,
    /**
     * The key that stores the suction speed for the {@link SkyHopper}.
     */
    SUCTION_SPEED,
    /**
     * The key that stores the max suction speed for the {@link SkyHopper}.
     */
    MAX_SUCTION_SPEED,
    /**
     * The key that stores the suction amount for the {@link SkyHopper}.
     */
    SUCTION_AMOUNT,
    /**
     * The key that stores the max suction amount for the {@link SkyHopper}.
     */
    MAX_SUCTION_AMOUNT,
    /**
     * The key that stores the suction range for the {@link SkyHopper}.
     */
    SUCTION_RANGE,
    /**
     * The key that stores the max suction speed for the {@link SkyHopper}.
     */
    MAX_SUCTION_RANGE,
    /**
     * The key that stores the maximum number of linked containers allowed for the {@link SkyHopper}.
     */
    MAX_CONTAINERS;

    /**
     * The {@link NamespacedKey} for the {@link SkyHopper} or {@link SkyContainer} setting.
     */
    private final @NotNull NamespacedKey key;

    /**
     * Creates a new {@link NamespacedKey} for the setting.
     */
    HopperKeys() {
        this.key = new NamespacedKey("skyhoppers", this.name().toLowerCase());
    }

    /**
     * Gets the {@link NamespacedKey} for the setting.
     * @return A {@link NamespacedKey}.
     */
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
