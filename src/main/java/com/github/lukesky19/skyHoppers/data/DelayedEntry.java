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
package com.github.lukesky19.skyHoppers.data;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Contains the data necessary to complete a transfer from the HopperMoveItemEvent for SkyHoppers.
 * @param sourceLocation The location of the source container.
 * @param destinationLocation The Location of the destination container.
 * @param isSuction Whether the transfer occurred from a suction or a transfer.
 * @param initiatorIsSource Whether the source container initiated the transfer or not.
 */
public record DelayedEntry(
        @NotNull Location sourceLocation,
        @NotNull Location destinationLocation,
        boolean isSuction,
        boolean initiatorIsSource) {}
