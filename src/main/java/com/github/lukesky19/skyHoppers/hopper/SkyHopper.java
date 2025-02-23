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

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

/**
 * Contains the data for a SkyHopper.
 * @param enabled Whether the SkyHopper is enabled or not.
 * @param particles Whether the SkyHopper should show particles or not.
 * @param owner The UUID of the Player that owns the SkyHopper.
 * @param members The List of UUIDs that can access the SkyHopper.
 * @param location The Location of the SkyHopper.
 * @param containers The list of SkyContainers that the SkyHopper is linked to.
 * @param filterType The SkyHopper's FilterType.
 * @param filterItems The SkyHopper's list of items to filter.
 * @param transferSpeed The SkyHopper's current transfer speed.
 * @param maxTransferSpeed The SkyHopper's max transfer speed.
 * @param transferAmount The SkyHopper's current transfer amount.
 * @param maxTransferAmount The SkyHopper's max transfer amount.
 * @param suctionSpeed The SkyHopper's current suction speed.
 * @param maxSuctionSpeed The SkyHopper's max suction speed.
 * @param suctionAmount The SkyHopper's current suction amount.
 * @param maxSuctionAmount The SkyHopper's max suction amount.
 * @param suctionRange The SkyHopper's current suction range.
 * @param maxSuctionRange The SkyHopper's max suction range.
 * @param maxContainers The SkyHopper's max number of linked containers.
 * @param nextSuctionTime When the SkyHopper can suction items next.
 * @param nextTransferTime When the SkyHopper can transfer items next.
 */
public record SkyHopper(
        boolean enabled,
        boolean particles,
        UUID owner,
        List<UUID> members,
        Location location,
        List<SkyContainer> containers,
        FilterType filterType,
        List<Material> filterItems,
        double transferSpeed,
        double maxTransferSpeed,
        int transferAmount,
        int maxTransferAmount,
        double suctionSpeed,
        double maxSuctionSpeed,
        int suctionAmount,
        int maxSuctionAmount,
        int suctionRange,
        int maxSuctionRange,
        int maxContainers,
        long nextSuctionTime,
        long nextTransferTime) {}
