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
package com.github.lukesky19.skyHoppers.gui;

import com.github.lukesky19.skyHoppers.gui.menu.filter.SkyContainerFilterGUI;
import com.github.lukesky19.skyHoppers.gui.menu.skycontainer.PriorityGUI;
import com.github.lukesky19.skyHoppers.gui.menu.skycontainer.SkyContainerGUI;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skyHoppers.util.ImmutableLocation;
import com.github.lukesky19.skyHoppers.util.LocationUUIDKey;
import com.github.lukesky19.skylib.paper.api.gui.interfaces.BaseGUI;
import com.github.lukesky19.skylib.paper.api.gui.interfaces.IGUIManager;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class manages open GUIs for SkyHoppers.
 */
public class GUIManager implements IGUIManager<LocationUUIDKey> {
    private final @NotNull Map<ImmutableLocation, Map<UUID, BaseGUI<LocationUUIDKey>>> openGUIsByLocationAndPlayer = new HashMap<>();

    /**
     * Constructor
     */
    public GUIManager() {}

    @Override
    public void addOpenGUI(@NotNull LocationUUIDKey identifier, @NotNull BaseGUI<LocationUUIDKey> data) {
        Map<UUID, BaseGUI<LocationUUIDKey>> uuidGuiMap = openGUIsByLocationAndPlayer.computeIfAbsent(identifier.location(), _ -> new HashMap<>());

        uuidGuiMap.put(identifier.uuid(), data);
    }

    @Override
    public void removeOpenGUI(@NotNull LocationUUIDKey identifier) {
        Map<UUID, BaseGUI<LocationUUIDKey>> uuidGuiMap = openGUIsByLocationAndPlayer.get(identifier.location());
        if(uuidGuiMap == null) return;

        uuidGuiMap.remove(identifier.uuid());

        if(uuidGuiMap.isEmpty()) {
            openGUIsByLocationAndPlayer.remove(identifier.location());
        }
    }

    /**
     * Refresh all guis with the same location and uuid.
     * @param locationUUIDKey The {@link LocationUUIDKey}.
     */
    @Override
    public void refreshGUIs(@NotNull LocationUUIDKey locationUUIDKey) {
        openGUIsByLocationAndPlayer.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(locationUUIDKey.location()))
                .map(Map.Entry::getValue)
                .filter(map -> map.containsKey(locationUUIDKey.uuid()))
                .map(map -> map.get(locationUUIDKey.uuid()))
                .forEach(BaseGUI::refresh);
    }

    @Override
    public @Nullable BaseGUI<LocationUUIDKey> getOpenGUI(@NotNull LocationUUIDKey locationUUIDKey) {
        return openGUIsByLocationAndPlayer
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(locationUUIDKey.location()))
                .map(Map.Entry::getValue)
                .filter(map -> map.containsKey(locationUUIDKey.uuid()))
                .findFirst()
                .map(entry -> entry.get(locationUUIDKey.uuid()))
                .orElse(null);
    }

    /**
     * Refresh all guis with the same location.
     * @param location The {@link ImmutableLocation}.
     */
    public void refreshGUIsByLocation(@NotNull ImmutableLocation location) {
        openGUIsByLocationAndPlayer.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(location))
                .map(Map.Entry::getValue)
                .map(Map::values)
                .forEach(collection -> collection.forEach(BaseGUI::refresh));
    }

    /**
     * Get the {@link BaseGUI} that is open by the provided player's {@link UUID}.
     * @param uuid The {@link UUID} of the player.
     * @return The {@link BaseGUI} the player is viewing or null.
     */
    public @Nullable BaseGUI<LocationUUIDKey> getGuiByUUID(@NotNull UUID uuid) {
        return openGUIsByLocationAndPlayer
                .values()
                .stream()
                .filter(map -> map.containsKey(uuid))
                .findFirst()
                .map(entry -> entry.get(uuid))
                .orElse(null);
    }

    /**
     * Closes any open GUIS related to a {@link SkyContainer} for the provided {@link SkyHopper}'s {@link ImmutableLocation}.
     * @param location The {@link ImmutableLocation} of the {@link SkyHopper}.
     */
    public void closeSkyContainerRelatedGUIs(@NotNull ImmutableLocation location) {
        openGUIsByLocationAndPlayer.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(location))
                .map(Map.Entry::getValue)
                .map(Map::values)
                .map(collection -> collection.stream()
                        .filter(baseGUI ->
                                baseGUI instanceof SkyContainerGUI
                                        || baseGUI instanceof PriorityGUI
                                        || baseGUI instanceof SkyContainerFilterGUI))
                .forEach(collection -> collection.forEach(BaseGUI::close));
    }

    /**
     * Close any open {@link SkyHopperGUI}s for the {@link Location} provided.
     * @param location The {@link Location} of the {@link SkyHopper} to close GUIs for.
     */
    public void closeOpenGUIsForLocation(@NotNull ImmutableLocation location) {
        openGUIsByLocationAndPlayer.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(location))
                .map(Map.Entry::getValue)
                .map(Map::values)
                .forEach(collection -> collection.forEach(gui -> gui.unload(false)));
    }

    /**
     * Close all open GUIs.
     * @param onDisable Is the plugin being disabled?
     */
    public void closeOpenGUIs(boolean onDisable) {
        openGUIsByLocationAndPlayer.entrySet().iterator().forEachRemaining(entry1 ->
                entry1.getValue().entrySet().iterator().forEachRemaining((entry2) -> entry2.getValue().unload(onDisable)));
    }
}
