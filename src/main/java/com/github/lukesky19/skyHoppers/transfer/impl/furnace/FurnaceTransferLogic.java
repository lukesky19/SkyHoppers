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
package com.github.lukesky19.skyHoppers.transfer.impl.furnace;

import com.github.lukesky19.skyHoppers.transfer.TransferLogic;

/**
 * This class can be extended to implement transfer logic for a furnace.
 * Contains slot numbers for the input slot, fuel slot, and output slot.
 */
public abstract class FurnaceTransferLogic extends TransferLogic {
    /**
     * The slot number for the input of the furnace. This is where items are placed to be smelted.
     */
    protected static final int INPUT_SLOT_NUMBER = 0;
    /**
     * The slot number for the fuel of the furnace. This is where items are placed to be burned or consumed.
     */
    protected static final int FUEL_SLOT_NUMBER = 1;
    /**
     * The slot number for the output of the furnace. This is where items are placed when they are done being smelted.
     */
    protected static final int OUTPUT_SLOT_NUMBER = 2;

    /**
     * Default Constructor.
     */
    public FurnaceTransferLogic() {}
}
