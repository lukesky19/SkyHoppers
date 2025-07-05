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
package com.github.lukesky19.skyHoppers;

import com.github.lukesky19.skyHoppers.hopper.*;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * This class contains methods to interface with the SkyHoppers plugin.
 */
public class SkyHoppersAPI {
    private final @NotNull HopperManager hopperManager;

    /**
     * Constructor
     * @param hopperManager A {@link HopperManager} instance.
     */
    public SkyHoppersAPI(@NotNull HopperManager hopperManager) {
        this.hopperManager = hopperManager;
    }

    /**
     * Crates an {@link ItemStack} from a {@link SkyHopper}.
     * @param skyHopper The {@link SkyHopper}.
     * @param amount The amount in the {@link ItemStack}.
     * @return An {@link ItemStack} or null.
     */
    public @Nullable ItemStack createItemStackFromSkyHopper(@NotNull SkyHopper skyHopper, int amount) {
        return hopperManager.createItemStackFromSkyHopper(skyHopper, amount);
    }

    /**
     * Creates a {@link SkyHopper}.
     * @param enabled Is the SkyHopper enabled?
     * @param particles Is particles enabled for the SkyHopper?
     * @param owner The {@link UUID} of the player who owns the SkyHopper.
     * @param members A {@link List} of {@link UUID}s that can also access the SkyHopper.
     * @param linkedContainers A {@link List} of {@link SkyContainer}s that are linked to the SkyHopper.
     * @param filterType The {@link FilterType} of the SkyHopper.
     * @param filterItems A {@link List} of {@link ItemType}s to filter.
     * @param transferSpeed The transfer speed of the SkyHopper.
     * @param maxTransferSpeed The max transfer speed of the SkyHopper.
     * @param transferAmount The transfer amount of the SkyHopper.
     * @param maxTransferAmount The max transfer amount of the SkyHopper.
     * @param suctionSpeed The suction speed of the SkyHopper.
     * @param maxSuctionSpeed The max suction speed of the SkyHopper.
     * @param suctionAmount The suction amount of the SkyHopper.
     * @param maxSuctionAmount The max suction amount of the SkyHopper.
     * @param suctionRange The suction range of the SkyHopper.
     * @param maxSuctionRange The max suction range of the SkyHopper.
     * @param maxContainers The maximum number of containers that can be linked.
     * @return A {@link SkyHopper} or null.
     */
    public @Nullable SkyHopper createSkyHopper(
            boolean enabled,
            boolean particles,
            @Nullable UUID owner,
            @NotNull List<UUID> members,
            @Nullable Location location,
            @NotNull List<SkyContainer> linkedContainers,
            @NotNull FilterType filterType,
            @NotNull List<ItemType> filterItems,
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
            int maxContainers) {
        return new SkyHopper(
                enabled,
                particles,
                owner,
                members,
                location,
                linkedContainers,
                filterType,
                filterItems,
                transferSpeed,
                maxTransferSpeed,
                transferAmount,
                maxTransferAmount,
                suctionSpeed,
                maxSuctionSpeed,
                suctionAmount,
                maxSuctionAmount,
                suctionRange,
                maxSuctionRange,
                maxContainers,
                System.currentTimeMillis(),
                System.currentTimeMillis());
    }

    /**
     * Creates a {@link SkyHopper}'s {@link ItemStack}.
     * @param enabled Is the SkyHopper enabled?
     * @param particles Is particles enabled for the SkyHopper?
     * @param owner The {@link UUID} of the player who owns the SkyHopper.
     * @param members A {@link List} of {@link UUID}s that can also access the SkyHopper.
     * @param linkedContainers A {@link List} of {@link SkyContainer}s that are linked to the SkyHopper.
     * @param filterType The {@link FilterType} of the SkyHopper.
     * @param filterItems A {@link List} of {@link ItemType}s to filter.
     * @param transferSpeed The transfer speed of the SkyHopper.
     * @param maxTransferSpeed The max transfer speed of the SkyHopper.
     * @param transferAmount The transfer amount of the SkyHopper.
     * @param maxTransferAmount The max transfer amount of the SkyHopper.
     * @param suctionSpeed The suction speed of the SkyHopper.
     * @param maxSuctionSpeed The max suction speed of the SkyHopper.
     * @param suctionAmount The suction amount of the SkyHopper.
     * @param maxSuctionAmount The max suction amount of the SkyHopper.
     * @param suctionRange The suction range of the SkyHopper.
     * @param maxSuctionRange The max suction range of the SkyHopper.
     * @param maxContainers The maximum number of containers that can be linked.
     * @param amount The amount of SkyHoppers to create.
     * @return An {@link ItemStack} or null.
     */
    public @Nullable ItemStack createSkyHopperItemStack(
            boolean enabled,
            boolean particles,
            @Nullable UUID owner,
            @NotNull List<UUID> members,
            @NotNull List<SkyContainer> linkedContainers,
            @NotNull FilterType filterType,
            @NotNull List<ItemType> filterItems,
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
            int amount) {
        SkyHopper skyHopper = new SkyHopper(
                enabled,
                particles,
                owner,
                members,
                null,
                linkedContainers,
                filterType,
                filterItems,
                transferSpeed,
                maxTransferSpeed,
                transferAmount,
                maxTransferAmount,
                suctionSpeed,
                maxSuctionSpeed,
                suctionAmount,
                maxSuctionAmount,
                suctionRange,
                maxSuctionRange,
                maxContainers,
                System.currentTimeMillis(),
                System.currentTimeMillis());

        return hopperManager.createItemStackFromSkyHopper(skyHopper, amount);
    }

    /**
     * Checks if an {@link ItemStack} is that of a SkyHopper.
     * @param itemStack The {@link ItemStack} to check.
     * @return true if a SkyHopper, otherwise false.
     */
    public boolean isItemStackSkyHopper(@NotNull ItemStack itemStack) {
        return hopperManager.isItemStackSkyHopper(itemStack);
    }
}
