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

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.List;

/**
 * The locale configuration for plugin messages.
 * @param configVersion The config version of the file.
 * @param prefix The plugin's prefix.
 * @param help The plugin's help message.
 * @param reload The plugin's reload message.
 * @param hopperGiven The message to display when a player is given a SkyHopper.
 * @param hopperGivenTo The message to display to the person who gave another player a SkyHopper.
 * @param noBuild The message to display when a player cannot build in an area.
 * @param noBreak The message to display when a player cannot break in an area.
 * @param hopperNoAccess The message to display when a player cannot access containers in an area.
 * @param hopperPlaced The message to display when a SkyHopper is placed.
 * @param hopperBroken The message to display when a SkyHopper is broken.
 * @param linkingEnabled The message to display when a player starts linking.
 * @param linkingDisabled The message to display when a player stops linking.
 * @param linkingHowToExit The message to display on how to exit linking mode.
 * @param containerLinked The message to display when a container is linked.
 * @param containerUnlinked The message to display when a container is unlinked.
 * @param containerLinksMaxed The message to display when the SkyHopper already has a maximum number of linked containers.
 * @param containerNoAccess The message to display when a player cannot access a container to link it.
 * @param suctionSpeedUpgrade The message to display when a player upgrades suction speed.
 * @param suctionAmountUpgrade The message to display when a player upgrades suction amount.
 * @param suctionRangeUpgrade The message to display when a player upgrades suction range.
 * @param maxLinksUpgrade The message to display when a player upgrades the number of linked containers.
 * @param transferSpeedUpgrade The message to display when a player upgrades transfer speed.
 * @param transferAmountUpgrade The message to display when a player upgrades transfer amount.
 * @param notEnoughMoney The message to display when a player lacks the funds to purchase an upgrade.
 * @param upgradeMaxed The message to display when an upgrade is maxed out.
 * @param skyhopperCreationFailed The message to display when SkyHopper creation failed.
 * @param skyhoppersLoaded The message to display when SkyHoppers are loaded.
 * @param skyhoppersForceLoaded The message to display when SkyHoppers are force loaded.
 * @param skyHoppersPaused The message to display when SkyHoppers are paused.
 * @param skyhoppersUnpaused The message to display when SkyHoppers are unpaused.
 * @param invalidSettings The message to display when the plugin's settings are invalid.
 * @param failedSkyHopperLoad The message to display when a SkyHopper failed to load.
 */
@ConfigSerializable
public record Locale(
        String configVersion,
        String prefix,
        List<String> help,
        String reload,
        String hopperGiven,
        String hopperGivenTo,
        String noBuild,
        String noBreak,
        String hopperNoAccess,
        String hopperPlaced,
        String hopperBroken,
        String linkingEnabled,
        String linkingDisabled,
        String linkingHowToExit,
        String containerLinked,
        String containerUnlinked,
        String containerLinksMaxed,
        String containerNoAccess,
        String suctionSpeedUpgrade,
        String suctionAmountUpgrade,
        String suctionRangeUpgrade,
        String maxLinksUpgrade,
        String transferSpeedUpgrade,
        String transferAmountUpgrade,
        String notEnoughMoney,
        String upgradeMaxed,
        String skyhopperCreationFailed,
        String skyhoppersLoaded,
        String skyhoppersForceLoaded,
        String skyHoppersPaused,
        String skyhoppersUnpaused,
        String invalidSettings,
        String failedSkyHopperLoad,
        String itemNotSkyHopper,
        String noAccessOwnerChange,
        String newOwner) {}
