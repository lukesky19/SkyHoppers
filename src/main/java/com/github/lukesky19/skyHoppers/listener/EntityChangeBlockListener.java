package com.github.lukesky19.skyHoppers.listener;

import com.github.lukesky19.skyHoppers.hopper.SkyHopper;
import com.github.lukesky19.skyHoppers.manager.HopperManager;
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
    private final @NotNull HopperManager hopperManager;

    /**
     * Constructor
     * @param hopperManager A HopperManager instance.
     */
    public EntityChangeBlockListener(@NotNull HopperManager hopperManager) {
        this.hopperManager = hopperManager;
    }

    /**
     * Handles when an entity changes a block and checks if a block is a {@link SkyHopper} to properly remove it and drop the item.
     * @param entityChangeBlockEvent An {@link EntityChangeBlockEvent}
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBlockChange(EntityChangeBlockEvent entityChangeBlockEvent) {
        Block block = entityChangeBlockEvent.getBlock();

        SkyHopper skyHopper = hopperManager.getSkyHopper(block.getLocation());
        if(skyHopper != null) {
            entityChangeBlockEvent.setCancelled(true);

            block.setType(Material.AIR);

            if(skyHopper.getLocation() != null) hopperManager.removeSkyHopper(skyHopper.getLocation());

            ItemStack skyHopperItem = hopperManager.createItemStackFromSkyHopper(skyHopper, 1);
            if(skyHopperItem != null) {
                block.getWorld().dropItem(block.getLocation(), skyHopperItem);
            }
        }
    }
}
