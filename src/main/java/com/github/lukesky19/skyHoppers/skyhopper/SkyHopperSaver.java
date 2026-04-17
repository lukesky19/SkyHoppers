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
package com.github.lukesky19.skyHoppers.skyhopper;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.skyhopper.data.Filterable.FilterType;
import com.github.lukesky19.skyHoppers.skyhopper.data.HopperKeys;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.util.ImmutableLocation;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.libs.morepersistentdatatypes.DataType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.block.Hopper;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to save {@link SkyHopper}s.
 */
public class SkyHopperSaver {
    private final @NotNull ComponentLogger logger;

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     */
    public SkyHopperSaver(@NotNull SkyHoppers skyHoppers) {
        this.logger = skyHoppers.getComponentLogger();
    }

    /**
     * Save the {@link SkyHopper} provided.
     * @param skyHopper The {@link SkyHopper} to save.
     */
    public void saveSkyHopper(@NotNull SkyHopper skyHopper) {
        ImmutableLocation location = skyHopper.getLocation();
        if(location == null) {
            logger.warn(AdventureUtility.plain("Unable to save SkyHopper to a Hopper's PDC due to a null location for the SkyHopper."));
            return;
        }

        if(location.getBlock().getState(false) instanceof Hopper hopper) {
            saveSkyHopper(skyHopper, hopper);
        } else {
            logger.warn(AdventureUtility.plain("Unable to save SkyHopper to a Hopper's PDC as the block at the SkyHopper's location is not a Hopper."));
        }
    }

    /**
     * Save the {@link SkyHopper} provided.
     * @param skyHopper The {@link SkyHopper} to save.
     * @param hopper The {@link Hopper} the SkyHopper is attached to.
     */
    public void saveSkyHopper(@NotNull SkyHopper skyHopper, @NotNull Hopper hopper) {
        saveSkyHopper(skyHopper, hopper.getPersistentDataContainer());

        hopper.update();
    }

    /**
     * Save the {@link SkyHopper} provided.
     * @param skyHopper The {@link SkyHopper} to save.
     * @param pdc The {@link PersistentDataContainer} to save the SkyHopper to.
     */
    public void saveSkyHopper(@NotNull SkyHopper skyHopper, @NotNull PersistentDataContainer pdc) {
        pdc.set(HopperKeys.ENABLED.getKey(), PersistentDataType.INTEGER, skyHopper.isSkyHopperEnabled() ? 1 : 0);

        pdc.set(HopperKeys.VERSION.getKey(), PersistentDataType.INTEGER, skyHopper.getVersion());

        pdc.set(HopperKeys.PARTICLES.getKey(), PersistentDataType.INTEGER, skyHopper.isParticlesEnabled() ? 1 : 0);

        // Save the SkyHopper's owner
        if (skyHopper.getOwner() != null) {
            pdc.set(HopperKeys.OWNER.getKey(), DataType.UUID, skyHopper.getOwner());
        }

        // Save the SkyHopper's members
        pdc.set(HopperKeys.MEMBERS.getKey(), PersistentDataType.LIST.listTypeFrom(DataType.UUID), skyHopper.getMembers());

        // Save the input filter type
        pdc.set(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING, skyHopper.getFilterType().name());

        // Get a list of the input filter ItemType's NamespacedKeys as a String to save.
        List<String> inputFilterItemNames = skyHopper.getFilterItems().stream().map(itemType -> itemType.getKey().toString()).toList();
        // Save the input filter items
        pdc.set(HopperKeys.FILTER_ITEMS.getKey(),
                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING), inputFilterItemNames);

        // Save the linked containers
        List<PersistentDataContainer> pdcList = new ArrayList<>();

        for (SkyContainer skyContainer : skyHopper.getLinkedContainers()) {
            Location linkedLocation = skyContainer.getLocation().toBukkitLocation();

            FilterType filterType = skyContainer.getFilterType();
            PersistentDataContainer persistentDataContainer = pdc.getAdapterContext().newPersistentDataContainer();
            persistentDataContainer.set(HopperKeys.LOCATION.getKey(), DataType.LOCATION, linkedLocation);
            persistentDataContainer.set(HopperKeys.FILTER_TYPE.getKey(), PersistentDataType.STRING, filterType.name());

            // Get a list of the output filter ItemType's NamespacedKeys as a String to save.
            List<String> containerFilterItemNames = skyContainer.getFilterItems().stream().map(itemType -> itemType.getKey().toString()).toList();
            // Save the output filter items
            persistentDataContainer.set(HopperKeys.FILTER_ITEMS.getKey(),
                    PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING), containerFilterItemNames);

            // Save the priority
            persistentDataContainer.set(HopperKeys.PRIORITY.getKey(), PersistentDataType.INTEGER, skyContainer.getPriority());

            pdcList.add(persistentDataContainer);
        }

        pdc.set(HopperKeys.LINKS.getKey(), PersistentDataType.LIST.listTypeFrom(PersistentDataType.TAG_CONTAINER), pdcList);

        // Save the SkyHopper upgrades
        pdc.set(HopperKeys.TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.getTransferSpeed());

        pdc.set(HopperKeys.MAX_TRANSFER_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.getMaxTransferSpeed());

        pdc.set(HopperKeys.TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.getTransferAmount());

        pdc.set(HopperKeys.MAX_TRANSFER_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.getMaxTransferAmount());

        pdc.set(HopperKeys.SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.getSuctionSpeed());

        pdc.set(HopperKeys.MAX_SUCTION_SPEED.getKey(), PersistentDataType.DOUBLE, skyHopper.getMaxSuctionSpeed());

        pdc.set(HopperKeys.SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.getSuctionAmount());

        pdc.set(HopperKeys.MAX_SUCTION_AMOUNT.getKey(), PersistentDataType.INTEGER, skyHopper.getMaxSuctionAmount());

        pdc.set(HopperKeys.SUCTION_RANGE.getKey(), PersistentDataType.INTEGER, skyHopper.getSuctionRange());

        pdc.set(HopperKeys.MAX_SUCTION_RANGE.getKey(), PersistentDataType.INTEGER, skyHopper.getMaxSuctionRange());

        pdc.set(HopperKeys.MAX_CONTAINERS.getKey(), PersistentDataType.INTEGER, skyHopper.getMaxContainers());
    }
}
