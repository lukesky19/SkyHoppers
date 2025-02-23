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
import com.github.lukesky19.skyHoppers.config.manager.GUIManager;
import com.github.lukesky19.skyHoppers.config.manager.LocaleManager;
import com.github.lukesky19.skyHoppers.config.manager.SettingsManager;
import com.github.lukesky19.skyHoppers.config.record.Locale;
import com.github.lukesky19.skyHoppers.gui.HopperGUI;
import com.github.lukesky19.skyHoppers.hopper.FilterType;
import com.github.lukesky19.skyHoppers.hopper.SkyContainer;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HookManager;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skylib.format.FormatUtil;
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
    private final SkyHoppers plugin;
    private final SettingsManager settingsManager;
    private final LocaleManager localeManager;
    private final GUIManager guiManager;
    private final HopperManager hopperManager;
    private final HookManager hookManager;

    private final Map<UUID, Location> linkingPlayers = new HashMap<>();

    /**
     * Constructor
     * @param plugin The SkyHoppers Plugin.
     * @param settingsManager A SettingsManager instance.
     * @param localeManager A LocaleManager instance.
     * @param guiManager A GUIManager instance.
     * @param hopperManager A HopperManager instance.
     * @param hookManager A HookManager instance.
     */
    public HopperClickListener(
            SkyHoppers plugin,
            SettingsManager settingsManager,
            LocaleManager localeManager,
            GUIManager guiManager,
            HopperManager hopperManager,
            HookManager hookManager) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.hopperManager = hopperManager;
        this.hookManager = hookManager;
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
    public void addLinkingPlayer(Player player, Location location) {
        linkingPlayers.put(player.getUniqueId(), location);
    }

    /**
     * Handles when a SkyHopper is clicked to open the settings GUI or to link a container.
     * @param playerInteractEvent A PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperClick(PlayerInteractEvent playerInteractEvent) {
        final Locale locale = localeManager.getLocale();
        final Player player = playerInteractEvent.getPlayer();
        final UUID uuid = player.getUniqueId();
        final Block block = playerInteractEvent.getClickedBlock();

        if (!playerInteractEvent.hasBlock() || playerInteractEvent.getAction() != Action.LEFT_CLICK_BLOCK || block == null) return;

        if(isPlayerLinking(uuid)) {
            if (!(block.getState(false) instanceof Container container)) return;

            if (hookManager.canNotOpen(player, container.getLocation())) {
                player.sendMessage(FormatUtil.format(locale.prefix() + locale.containerNoAccess()));

                playerInteractEvent.setCancelled(true);

                return;
            }

            SkyHopper linkingSkyHopper = hopperManager.getSkyHopper(linkingPlayers.get(uuid));
            if (linkingSkyHopper == null) {
                linkingPlayers.remove(player.getUniqueId());

                return;
            }

            playerInteractEvent.setCancelled(true);

            SkyHopper targetSkyHopper = hopperManager.getSkyHopper(container.getLocation());
            if(targetSkyHopper != null
                    && targetSkyHopper.location() != null
                    && linkingSkyHopper.location() != null
                    && linkingSkyHopper.location().equals(targetSkyHopper.location())) {
                linkingPlayers.remove(player.getUniqueId());

                player.sendMessage(FormatUtil.format(locale.prefix() + locale.linkingDisabled()));

                return;
            }

            if(linkingSkyHopper.location() != null && linkingSkyHopper.location().getBlock().getState(false) instanceof Hopper hopper) {
                List<SkyContainer> skyContainerList = linkingSkyHopper.containers();

                Iterator<SkyContainer> skyContainerIterator = skyContainerList.iterator();
                while(skyContainerIterator.hasNext()) {
                    SkyContainer skyContainer = skyContainerIterator.next();
                    if (skyContainer.location().equals(container.getLocation())) {
                        skyContainerIterator.remove();

                        SkyHopper updatedSkyHopper = getUpdatedSkyHopper(linkingSkyHopper, skyContainerList);

                        hopperManager.saveSkyHopperToBlockPDC(updatedSkyHopper, hopper);

                        hopperManager.cacheSkyHopper(updatedSkyHopper.location(), updatedSkyHopper);

                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.containerUnlinked()));

                        return;
                    }
                }

                if(linkingSkyHopper.containers().size() != linkingSkyHopper.maxContainers()) {
                    skyContainerList.add(new SkyContainer(container.getLocation(), FilterType.NONE, new ArrayList<>()));

                    SkyHopper updatedSkyHopper = getUpdatedSkyHopper(linkingSkyHopper, skyContainerList);

                    hopperManager.saveSkyHopperToBlockPDC(updatedSkyHopper, hopper);

                    hopperManager.cacheSkyHopper(updatedSkyHopper.location(), updatedSkyHopper);

                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.containerLinked()));
                } else {
                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.containerLinksMaxed()));
                }
            }
        } else {
            if (!(block.getState(false) instanceof Hopper hopperBlock)) return;

            SkyHopper skyHopper = hopperManager.getSkyHopper(hopperBlock.getLocation());
            if (skyHopper == null || player.isSneaking()) return;

            playerInteractEvent.setCancelled(true);

            if (hookManager.canNotOpen(player, hopperBlock.getLocation())) {
                player.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperNoAccess()));

                return;
            }

            if(player.hasPermission("skyhoppers.admin") || (skyHopper.owner() != null && skyHopper.owner().equals(player.getUniqueId())) || skyHopper.members().contains(player.getUniqueId())) {
                new HopperGUI(plugin, settingsManager, localeManager, guiManager, hopperManager, this, hopperBlock.getLocation(), player).open(plugin, player);
            } else {
                player.sendMessage(FormatUtil.format(locale.prefix() + locale.hopperNoAccess()));
            }
        }
    }

    /**
     * Updates the list of linked containers for the SkyHopper and returns the updated SkyHopper.
     * @param currentSkyHopper The SkyHopper to update the list of Linked Containers for.
     * @param containers The List of updated containers.
     * @return The updated SkyHopper.
     */
    private SkyHopper getUpdatedSkyHopper(@NotNull SkyHopper currentSkyHopper, @NotNull List<SkyContainer> containers) {
        return new SkyHopper(
                currentSkyHopper.enabled(),
                currentSkyHopper.particles(),
                currentSkyHopper.owner(),
                currentSkyHopper.members(),
                currentSkyHopper.location(),
                containers,
                currentSkyHopper.filterType(),
                currentSkyHopper.filterItems(),
                currentSkyHopper.transferSpeed(),
                currentSkyHopper.maxTransferSpeed(),
                currentSkyHopper.transferAmount(),
                currentSkyHopper.maxTransferAmount(),
                currentSkyHopper.suctionSpeed(),
                currentSkyHopper.maxSuctionSpeed(),
                currentSkyHopper.suctionAmount(),
                currentSkyHopper.maxSuctionAmount(),
                currentSkyHopper.suctionRange(),
                currentSkyHopper.maxSuctionRange(),
                currentSkyHopper.maxContainers(),
                currentSkyHopper.nextSuctionTime(),
                currentSkyHopper.nextTransferTime());
    }
}
