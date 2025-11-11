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
package com.github.lukesky19.skyHoppers.transfer.impl.brewing;

import com.github.lukesky19.skyHoppers.transfer.TransferLogic;

/**
 * This class can be extended to implement transfer logic for a brewing stand.
 * Contains slot numbers for the fuel slot, ingredient slot, and bottle slots.
 */
public abstract class BrewingTransferLogic extends TransferLogic {
    /**
     * The slot number for the first bottle in the brewing stand.
     */
    protected static final int BOTTLE_1_SLOT_NUMBER = 0;
    /**
     * The slot number for the second bottle in the brewing stand.
     */
    protected static final int BOTTLE_2_SLOT_NUMBER = 1;
    /**
     * The slot number for the third bottle in the brewing stand.
     */
    protected static final int BOTTLE_3_SLOT_NUMBER = 2;
    /**
     * The slot number for the ingredients in the brewing stand.
     */
    protected static final int INGREDIENT_SLOT_NUMBER = 3;
    /**
     * The slot number for the fuel in the brewing stand.
     */
    protected static final int FUEL_SLOT_NUMBER = 4;

    /**
     * Default Constructor.
     */
    public BrewingTransferLogic() {}
}
