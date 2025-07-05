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
package com.github.lukesky19.skyHoppers.manager;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.gui.SkyHopperGUI;
import com.github.lukesky19.skyHoppers.gui.menu.filter.OutputFilterGUI;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class manages open GUIs for SkyHoppers.
 */
public class GUIManager {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull Map<Location, Map<UUID, SkyHopperGUI>> openGUIsByLocationAndPlayer = new HashMap<>();

    /**
     * Constructor
     * @param skyHoppers A {@link SkyHoppers} instance.
     */
    public GUIManager(@NotNull SkyHoppers skyHoppers) {
        this.skyHoppers = skyHoppers;
    }

    /**
     * Get the {@link SkyHopperGUI} that is open by the provided player's {@link UUID}.
     * @param uuid The {@link UUID} of the player.
     * @return The {@link SkyHopperGUI} the player is viewing or null.
     */
    public @Nullable SkyHopperGUI getGuiByUUID(@NotNull UUID uuid) {
        for(Map.Entry<Location, Map<UUID, SkyHopperGUI>> locationEntry : openGUIsByLocationAndPlayer.entrySet()) {
            Map<UUID, SkyHopperGUI> viewers = locationEntry.getValue();

            for(Map.Entry<UUID, SkyHopperGUI> viewerEntry : viewers.entrySet()) {
                UUID viewerUuid = viewerEntry.getKey();
                if(viewerUuid.equals(uuid)) {
                    return viewerEntry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Adds a viewer for a SkyHopper's Location.
     * @param location The Location of the SkyHopper.
     * @param viewer The Player's UUID viewing the SkyHopper's settings.
     * @param gui The GUI the Player is viewing.
     */
    public void addViewer(@NotNull Location location, @NotNull UUID viewer, @NotNull SkyHopperGUI gui) {
        Map<UUID, SkyHopperGUI> uuidGuiMap = openGUIsByLocationAndPlayer.getOrDefault(location, new HashMap<>());

        uuidGuiMap.put(viewer, gui);
        openGUIsByLocationAndPlayer.put(location, uuidGuiMap);
    }

    /**
     * Removes a viewer for a SkyHopper's Location.
     * @param location The Location of the SkyHopper.
     * @param viewer The Player's UUID who was viewing the SkyHopper's settings.
     */
    public void removeViewer(Location location, UUID viewer) {
        Map<UUID, SkyHopperGUI> uuidGuiMap = openGUIsByLocationAndPlayer.get(location);
        if(uuidGuiMap == null) return;

        uuidGuiMap.remove(viewer);

        if(uuidGuiMap.isEmpty()) {
            openGUIsByLocationAndPlayer.remove(location);
        } else {
            openGUIsByLocationAndPlayer.put(location, uuidGuiMap);
        }
    }

    /**
     * Refreshes all players viewing a SkyHopper's settings at the given Location.
     * @param location The Location of the SkyHopper.
     */
    public void refreshViewersGUI(@NotNull Location location) {
        Map<UUID, SkyHopperGUI> uuidGuiMap = openGUIsByLocationAndPlayer.get(location);
        if(uuidGuiMap == null) return;

        if(uuidGuiMap.isEmpty()) {
            openGUIsByLocationAndPlayer.remove(location);
            return;
        }

        for(Map.Entry<UUID, SkyHopperGUI> entry : uuidGuiMap.entrySet()) {
            UUID uuid = entry.getKey();
            SkyHopperGUI gui = entry.getValue();

            Player player = skyHoppers.getServer().getPlayer(uuid);
            if(player != null && player.isOnline() && player.isConnected()) {
                gui.refresh();
            }
        }
    }

    /**
     * Closes any open {@link OutputFilterGUI}s for the provided {@link SkyHopper}'s {@link Location}.
     * @param location The {@link Location} of the {@link SkyHopper}.
     */
    public void closeOutputFilterGUIs(@NotNull Location location) {
        Map<UUID, SkyHopperGUI> uuidGuiMap = openGUIsByLocationAndPlayer.get(location);
        if(uuidGuiMap == null) return;

        if(uuidGuiMap.isEmpty()) {
            openGUIsByLocationAndPlayer.remove(location);
            return;
        }

        for(Map.Entry<UUID, SkyHopperGUI> entry : uuidGuiMap.entrySet()) {
            UUID uuid = entry.getKey();
            SkyHopperGUI gui = entry.getValue();

            Player player = skyHoppers.getServer().getPlayer(uuid);
            if(player != null && player.isOnline() && player.isConnected()) {
                if(gui instanceof OutputFilterGUI) {
                    gui.close();
                }
            }
        }
    }

    /**
     * Close any open {@link SkyHopperGUI}s for the {@link Location} provided.
     * @param location The {@link Location} of the {@link SkyHopper} to close GUIs for.
     */
    public void closeOpenGUIsForLocation(@NotNull Location location) {
        Map<UUID, SkyHopperGUI> uuidGuiMap = openGUIsByLocationAndPlayer.get(location);
        if(uuidGuiMap == null) return;

        if(uuidGuiMap.isEmpty()) {
            openGUIsByLocationAndPlayer.remove(location);
            return;
        }

        for(Map.Entry<UUID, SkyHopperGUI> entry : uuidGuiMap.entrySet()) {
            UUID uuid = entry.getKey();
            SkyHopperGUI gui = entry.getValue();

            Player player = skyHoppers.getServer().getPlayer(uuid);
            if(player != null && player.isOnline() && player.isConnected()) {
                gui.unload(false);
            }
        }
    }

    /**
     * Close all open GUIs.
     * @param onDisable Is the plugin being disabled?
     */
    public void closeOpenGUIs(boolean onDisable) {
        for(Map.Entry<Location, Map<UUID, SkyHopperGUI>> locationMapEntry : openGUIsByLocationAndPlayer.entrySet()) {
            Map<UUID, SkyHopperGUI> uuidGuiMap = locationMapEntry.getValue();

            if(uuidGuiMap.isEmpty()) continue;

            for(Map.Entry<UUID, SkyHopperGUI> guiEntry : uuidGuiMap.entrySet()) {
                SkyHopperGUI gui = guiEntry.getValue();

                gui.unload(onDisable);
            }
        }

        openGUIsByLocationAndPlayer.clear();
    }
}
