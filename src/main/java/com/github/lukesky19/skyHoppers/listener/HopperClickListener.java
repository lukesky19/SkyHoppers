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
package com.github.lukesky19.skyHoppers.listener;

import com.github.lukesky19.skyHoppers.SkyHoppers;
import com.github.lukesky19.skyHoppers.config.GUIConfigManager;
import com.github.lukesky19.skyHoppers.config.LocaleManager;
import com.github.lukesky19.skyHoppers.config.SettingsManager;
import com.github.lukesky19.skyHoppers.config.data.Locale;
import com.github.lukesky19.skyHoppers.gui.GUIManager;
import com.github.lukesky19.skyHoppers.gui.menu.HopperGUI;
import com.github.lukesky19.skyHoppers.hook.HookManager;
import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.Filterable.FilterType;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyContainer;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class listens to when a SkyHopper is clicked to open the SkyHopper settings GUI or a Container is clicked to link to.
 */
public class HopperClickListener implements Listener {
    private final @NotNull SkyHoppers skyHoppers;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull SkyHopperManager hopperManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull GUIManager guiManager;

    private final @NotNull Map<UUID, Location> linkingPlayers = new HashMap<>();

    /**
     * Constructor
     * @param skyHoppers The SkyHoppers Plugin.
     * @param settingsManager A SettingsManager instance.
     * @param localeManager A LocaleManager instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param hopperManager A HopperManager instance.
     * @param hookManager A HookManager instance.
     * @param guiManager A {@link GUIManager} instance.
     */
    public HopperClickListener(
            @NotNull SkyHoppers skyHoppers,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull SkyHopperManager hopperManager,
            @NotNull HookManager hookManager,
            @NotNull GUIManager guiManager) {
        this.skyHoppers = skyHoppers;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.hopperManager = hopperManager;
        this.hookManager = hookManager;
        this.guiManager = guiManager;
    }

    /**
     * Checks if a player is linking Containers to a SkyHopper.
     * @param uuid The UUID of the Player to check.
     * @return true if the player is linking, false if not.
     */
    public boolean isPlayerLinking(UUID uuid) {
        return linkingPlayers.containsKey(uuid);
    }

    /**
     * Adds the Player to the list of linking players.
     * @param player The Player linking.
     * @param location The Location of the SkyHopper being linked to.
     */
    public void addLinkingPlayer(@NotNull Player player, @NotNull Location location) {
        linkingPlayers.put(player.getUniqueId(), location);
    }

    /**
     * Disables any players in linking mode for a particular {@link Location}
     * @param location The {@link Location} of the SkyHopper being linked to.
     */
    public void disableLinkingForLocation(@NotNull Location location) {
        Locale locale = localeManager.getLocale();

        Iterator<Map.Entry<UUID, Location>> iterator = linkingPlayers.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<UUID, Location> entry = iterator.next();
            UUID uuid = entry.getKey();
            Location iteratorLocation = entry.getValue();

            if(iteratorLocation.equals(location)) {
                iterator.remove();

                Player player = skyHoppers.getServer().getPlayer(uuid);
                if(player != null && player.isOnline() && player.isConnected()) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.linkingDisabled()));
                }
            }
        }
    }

    /**
     * Handles when a SkyHopper is clicked to open the settings GUI or to link a container.
     * @param playerInteractEvent A {@link PlayerInteractEvent}
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperClick(PlayerInteractEvent playerInteractEvent) {
        Locale locale = localeManager.getLocale();
        Player player = playerInteractEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        if(playerInteractEvent.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = playerInteractEvent.getClickedBlock();
        if(block == null || !playerInteractEvent.hasBlock()) return;
        Location location = block.getLocation();

        // Extra check for if there is a SkyHopper at the location, but it isn't loaded.
        // This should be kept due to an issue where locations weren't saved properly in Version 1.1.0.0 to 1.1.1.1.
        if(!hopperManager.getSkyHopperDataManager().isSkyHopperLoaded(location)) {
            hopperManager.getSkyHopperProcessor().loadSkyHopperAtLocationDirectly(location);
        }

        if(isPlayerLinking(uuid)) {
            if (!(block.getState(false) instanceof Container container)) return;

            if (hookManager.canNotOpen(player, container.getLocation())) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.containerNoAccess()));

                playerInteractEvent.setCancelled(true);

                return;
            }

            SkyHopper linkingSkyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(linkingPlayers.get(uuid));
            if(linkingSkyHopper == null) {
                linkingPlayers.remove(player.getUniqueId());
                return;
            }

            playerInteractEvent.setCancelled(true);

            SkyHopper targetSkyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(container.getLocation());
            if(targetSkyHopper != null
                    && targetSkyHopper.getLocation() != null
                    && linkingSkyHopper.getLocation() != null
                    && linkingSkyHopper.getLocation().equals(targetSkyHopper.getLocation())) {
                linkingPlayers.remove(player.getUniqueId());

                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.linkingDisabled()));

                return;
            }

            if(linkingSkyHopper.getLocation() != null) {
                Iterator<SkyContainer> iterator = linkingSkyHopper.getLinkedContainers().iterator();
                while(iterator.hasNext()) {
                    SkyContainer skyContainer = iterator.next();

                    if(skyContainer.getLocation().equals(container.getLocation())) {
                        iterator.remove();

                        hopperManager.getSkyHopperSaver().saveSkyHopper(linkingSkyHopper);

                        guiManager.refreshViewersGUI(location);

                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.containerUnlinked()));

                        return;
                    }
                }
            }

            if(linkingSkyHopper.getLinkedContainers().size() != linkingSkyHopper.getMaxContainers()) {
                linkingSkyHopper.addLinkedContainer(new SkyContainer(container.getLocation(), FilterType.NONE, new ArrayList<>()));

                hopperManager.getSkyHopperSaver().saveSkyHopper(linkingSkyHopper);

                guiManager.refreshViewersGUI(location);

                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.containerLinked()));
            } else {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.containerLinksMaxed()));
            }
        } else {
            if (!(block.getState(false) instanceof Hopper hopperBlock)) return;

            SkyHopper skyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(hopperBlock.getLocation());
            if(skyHopper == null || player.isSneaking()) return;

            playerInteractEvent.setCancelled(true);

            if(hookManager.canNotOpen(player, hopperBlock.getLocation())) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.hopperNoAccess()));
                return;
            }

            if(player.hasPermission("skyhoppers.admin")
                    || (skyHopper.getOwner() != null && skyHopper.getOwner().equals(player.getUniqueId()))
                    || skyHopper.getMembers().contains(player.getUniqueId())) {
                HopperGUI hopperGUI = new HopperGUI(skyHoppers, guiManager, location, skyHopper, player, settingsManager, localeManager, guiConfigManager, hopperManager, hookManager, this);

                boolean creationResult = hopperGUI.create();
                if(!creationResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = hopperGUI.update();
                if(!updateResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = hopperGUI.open();
                if(!openResult) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                }
            } else {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.hopperNoAccess()));
            }
        }
    }
}
