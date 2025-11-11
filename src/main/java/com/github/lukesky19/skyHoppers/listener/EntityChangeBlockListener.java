package com.github.lukesky19.skyHoppers.listener;

import com.github.lukesky19.skyHoppers.skyhopper.SkyHopperManager;
import com.github.lukesky19.skyHoppers.skyhopper.data.SkyHopper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for when an Entity changes a block.
 */
public class EntityChangeBlockListener implements Listener {
    private final @NotNull SkyHopperManager hopperManager;

    /**
     * Constructor
     * @param hopperManager A {@link SkyHopperManager} instance.
     */
    public EntityChangeBlockListener(@NotNull SkyHopperManager hopperManager) {
        this.hopperManager = hopperManager;
    }

    /**
     * Handles when an entity changes a block and checks if a block is a {@link SkyHopper} to properly remove it and drop the item.
     * @param entityChangeBlockEvent An {@link EntityChangeBlockEvent}
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBlockChange(EntityChangeBlockEvent entityChangeBlockEvent) {
        Block block = entityChangeBlockEvent.getBlock();
        Location location = block.getLocation();

        SkyHopper skyHopper = hopperManager.getSkyHopperDataManager().getSkyHopper(location);
        if(skyHopper == null) {
            if(hopperManager.isLocationSkyHopper(location)) {
                entityChangeBlockEvent.setCancelled(true);
            }

            return;
        }

        entityChangeBlockEvent.setCancelled(true);

        block.setType(Material.AIR);

        if(skyHopper.getLocation() != null) hopperManager.getSkyHopperDataManager().removeSkyHopper(skyHopper.getLocation());

        ItemStack skyHopperItem = hopperManager.getSkyHopperCreator().createSkyHopperItemStack(skyHopper, 1);
        if(skyHopperItem != null) {
            block.getWorld().dropItem(block.getLocation(), skyHopperItem);
        }
    }
}
