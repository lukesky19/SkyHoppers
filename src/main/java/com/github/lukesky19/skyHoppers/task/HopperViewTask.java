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
package com.github.lukesky19.skyHoppers.task;

import com.github.lukesky19.skyHoppers.hopper.SkyContainer;
import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
import com.github.lukesky19.skyHoppers.util.PluginUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This Task handles the spawning of particles to highlight
 */
public class HopperViewTask extends BukkitRunnable {
    private final HopperManager hopperManager;
    private final Location location;
    private final Player player;
    private int ticks = 0;

    /**
     * Constructor
     * @param hopperManager A HopperManager instance.
     * @param location The location of the SkyHopper.
     * @param player The player to show the particles to.
     */
    public HopperViewTask(HopperManager hopperManager, Location location, Player player) {
        this.hopperManager = hopperManager;
        this.location = location;
        this.player = player;
    }

    /**
     * The function ran every time this task is ran.
     */
    @Override
    public void run() {
        SkyHopper skyHopper = hopperManager.getSkyHopper(location);
        if(skyHopper == null) {
            cancel();
            return;
        }

        int suctionRange = skyHopper.getSuctionRange();

        double range = suctionRange + 0.5;
        if (skyHopper.getLocation() != null) {
            var hopperCorner1 = skyHopper.getLocation().clone();
            var hopperCorner2 = hopperCorner1.clone().add(1, 1, 1);
            PluginUtils.getHollowCube(hopperCorner1, hopperCorner2, 0.5).stream()
                    .filter(loc -> loc.getWorld() != null)
                    .forEach(location -> player.spawnParticle(Particle.DUST, location.clone(), 1, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.LIME, 1)));

            for(SkyContainer skyContainer : skyHopper.getLinkedContainers()) {
                Location corner1 = skyContainer.getLocation().clone();
                Location corner2 = corner1.clone().add(1, 1, 1);
                PluginUtils.getHollowCube(corner1, corner2, 0.5).stream()
                        .filter(loc -> loc.getWorld() != null)
                        .forEach(location -> player.spawnParticle(Particle.DUST, location.clone(), 1, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.RED, 1)));
            }

            // Visualize Suction Range
            Location centered = skyHopper.getLocation().clone().add(0.5, 0.5, 0.5);
            Location min = centered.clone().subtract(range, range, range);
            Location max = centered.clone().add(range, range, range);

            PluginUtils.getHollowCube(min, max, 1).stream()
                    .filter(loc -> loc.getWorld() != null)
                    .forEach(loc -> player.spawnParticle(Particle.DUST, loc.clone(), 1, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.AQUA, 1)));
        }

        // Only visualize for 10 seconds
        if (ticks >= 200) {
            cancel();
        } else {
            ticks++;
        }
    }
}
