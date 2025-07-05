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
package com.github.lukesky19.skyHoppers.config.record;

import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Contains all the plugin's settings configuration.
 * @param configVersion The config version of the file.
 * @param locale The locale string to use.
 * @param dropToInventory Should SkyHoppers when broken be added to the player's inventory directly?
 * @param disabledHooks A list of disabled hooks.
 * @param skyHopperConfig The configuration for the SkyHopper ItemStack and starting upgrades.
 * @param upgrades The configuration for the SkyHopper upgrades.
 */
@ConfigSerializable
public record Settings(
        @Nullable String configVersion,
        @Nullable String locale,
        boolean dropToInventory,
        @NotNull List<String> disabledHooks,
        @NotNull SkyHopperConfig skyHopperConfig,
        @NotNull Upgrades upgrades) {

    /**
     * The configuration for the SkyHopper ItemStack.
     * @param startingTransferSpeed The starting transfer speed.
     * @param startingTransferAmount The starting transfer amount.
     * @param startingSuctionSpeed The starting suction speed.
     * @param startingSuctionAmount The starting suction amount.
     * @param startingSuctionRange The starting suction range.
     * @param startingMaxContainers The starting number of linked containers.
     * @param item The {@link ItemStackConfig} configuration for the SkyHopper ItemStack
     */
    @ConfigSerializable
    public record SkyHopperConfig(
            double startingTransferSpeed,
            int startingTransferAmount,
            double startingSuctionSpeed,
            int startingSuctionAmount,
            int startingSuctionRange,
            int startingMaxContainers,
            @NotNull ItemStackConfig item,
            @NotNull Placeholders placeholders) {}

    /**
     * Configuration for the Strings to be displayed when a SkyHopper or SkyHopper particles are enabled or disabled.
     * @param enabled The string to show when something is enabled.
     * @param disabled The string to show when something is disabled.
     */
    @ConfigSerializable
    public record Placeholders(@CheckForNull String enabled, @CheckForNull String disabled) {}

    /**
     * The upgrade configuration.
     * @param transferSpeed The transfer speed upgrade configuration.
     * @param transferAmount The transfer amount upgrade configuration.
     * @param suctionSpeed The suction speed upgrade configuration.
     * @param suctionAmount The suction amount upgrade configuration.
     * @param suctionRange The suction range upgrade configuration.
     * @param containers The linked containers upgrade configuration.
     */
    @ConfigSerializable
    public record Upgrades(
            TransferSpeed transferSpeed,
            TransferAmount transferAmount,
            SuctionSpeed suctionSpeed,
            SuctionAmount suctionAmount,
            SuctionRange suctionRange,
            Containers containers) {}

    /**
     * The transfer speed upgrade configuration.
     * @param upgrades The Mapping of upgrades to prices.
     */
    @ConfigSerializable
    public record TransferSpeed(Map<Double, Double> upgrades) {}

    /**
     * The transfer amount upgrade configuration.
     * @param upgrades The Mapping of upgrades to prices.
     */
    @ConfigSerializable
    public record TransferAmount(Map<Integer, Double> upgrades) {}

    /**
     * The suction speed upgrade configuration.
     * @param upgrades The Mapping of upgrades to prices.
     */
    @ConfigSerializable
    public record SuctionSpeed(Map<Double, Double> upgrades) {}

    /**
     * The suction amount upgrade configuration.
     * @param upgrades The Mapping of upgrades to prices.
     */
    @ConfigSerializable
    public record SuctionAmount(Map<Integer, Double> upgrades) {}

    /**
     * The suction range upgrade configuration.
     * @param upgrades The Mapping of upgrades to prices.
     */
    @ConfigSerializable
    public record SuctionRange(Map<Integer, Double> upgrades) {}

    /**
     * The linked containers upgrade configuration.
     * @param upgrades The Mapping of upgrades to prices.
     */
    @ConfigSerializable
    public record Containers(Map<Integer, Double> upgrades) {}
}
