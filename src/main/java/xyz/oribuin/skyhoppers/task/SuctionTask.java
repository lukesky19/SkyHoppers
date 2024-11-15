package xyz.oribuin.skyhoppers.task;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import xyz.oribuin.skyhoppers.SkyHoppersPlugin;
import xyz.oribuin.skyhoppers.hook.stacker.StackerHook;
import xyz.oribuin.skyhoppers.manager.ConfigurationManager.Settings;
import xyz.oribuin.skyhoppers.manager.HookManager;
import xyz.oribuin.skyhoppers.manager.HopperManager;
import xyz.oribuin.skyhoppers.util.PluginUtils;

public class SuctionTask extends BukkitRunnable {

    private final HopperManager manager;
    private final StackerHook stackerHook;
    private final double suctionRange;

    public SuctionTask(final SkyHoppersPlugin plugin) {
        this.manager = plugin.getManager(HopperManager.class);
        this.suctionRange = Settings.SUCTION_RANGE.getDouble();
        this.stackerHook = plugin.getManager(HookManager.class).getStackerHook();
    }

    @Override
    public void run() {
        for (var skyHopper : this.manager.getEnabledHoppers()) {
            if (skyHopper != null
                    && skyHopper.getLocation() != null
                    && skyHopper.getLocation().getBlock().getState() instanceof Hopper hopperBlock
                    && skyHopper.getLocation().isChunkLoaded()
                    && !hopperBlock.isLocked()) {

                final var world = skyHopper.getLocation().getWorld();
                final var block = skyHopper.getLocation().getBlock();

                @NotNull var rangeItems = world.getNearbyEntities(PluginUtils.centerLocation(skyHopper.getLocation()), suctionRange / 2.0, suctionRange / 2.0, suctionRange / 2.0, entity -> entity instanceof Item);

                for (var entity : rangeItems) {
                    var item = (Item) entity;

                    switch (skyHopper.getFilterType()) {
                        case NONE -> transferItem(item, hopperBlock, block);

                        case WHITELIST -> {
                            for (Material material : skyHopper.getFilterItems()) {
                                if (item.getItemStack().getType().equals(material)) {
                                    transferItem(item, hopperBlock, block);
                                }
                            }
                        }

                        case BLACKLIST -> {
                            for (Material material : skyHopper.getFilterItems()) {
                                if(!item.getItemStack().getType().equals(material)) {
                                    transferItem(item, hopperBlock, block);
                                }
                            }
                        }

                        case DESTROY -> {
                            for (Material material : skyHopper.getFilterItems()) {
                                if (item.getItemStack().getType().equals(material)) {
                                    item.getWorld().spawnParticle(Particle.SMOKE_NORMAL, item.getLocation(), 3, 0.0, 0.0, 0.0, 0.0);
                                    item.remove();
                                } else {
                                    transferItem(item, hopperBlock, block);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public int getItemAmount(Item item) {
        if (stackerHook != null) return stackerHook.getItemAmount(item);

        return item.getItemStack().getAmount();
    }

    public void setItemAmount(Item item, int amount) {
        if (stackerHook != null) {
            stackerHook.setItemAmount(item, amount);
            return;
        }

        item.getItemStack().setAmount(amount);
    }

    /**
     * Function for transferring items to hoppers and spawning particles
     * @param item The Item Entity to transfer
     * @param hopperBlock The Hopper to transfer the item(s) to
     * @param block The block to spawn particles at
     */
    private void transferItem(Item item, Hopper hopperBlock, Block block) {
        int itemAmount = this.getItemAmount(item);

        for (int i = 0; i < 5; i++) {
            if (itemAmount <= 0) return;

            final var hopperItem = hopperBlock.getInventory().getItem(i);
            if (hopperItem == null || hopperItem.getType() == Material.AIR) {
                // slot is empty, fill it with as many items as we can
                for (int x = 0; x < 5; x++)
                    block.getWorld().spawnParticle(Particle.REDSTONE, PluginUtils.centerLocation(block.getLocation()), 5, 0.3, 0.3, 0.3, 0.0, new Particle.DustOptions(Color.fromRGB(255, 192, 203), 1));

                // Create a copy of the item and set the amount to at most the max stack size of the material
                var copy = item.getItemStack().clone();
                copy.setAmount(Math.min(itemAmount, copy.getMaxStackSize()));
                itemAmount -= copy.getAmount();

                item.getWorld().spawnParticle(Particle.SPELL_WITCH, item.getLocation(), 3, 0.0, 0.0, 0.0, 0.0);
                hopperBlock.getInventory().setItem(i, item.getItemStack());

                if (itemAmount <= 0) item.remove();

                continue;
            }

            if (item.getItemStack().isSimilar(hopperItem)) {
                // slot has the exact same itemstack in it, can we increase the stack size any more?
                var amount = Math.min(hopperItem.getMaxStackSize() - hopperItem.getAmount(), itemAmount);
                if (amount > 0) {
                    // we sure can! add as much as we can from the chunk item to the existing hopper item
                    hopperItem.setAmount(hopperItem.getAmount() + amount);
                    if (itemAmount - amount <= 0) { // are we removing *all* the items?
                        item.remove();
                    }

                    itemAmount -= amount;

                    // ooo! pretty!
                    item.getWorld().spawnParticle(Particle.SPELL_WITCH, item.getLocation(), 3, 0.0, 0.0, 0.0, 0.0);

                    for (int x = 0; x < 5; x++)
                        block.getWorld().spawnParticle(Particle.REDSTONE, PluginUtils.centerLocation(block.getLocation()), 5, 0.3, 0.3, 0.3, 0.0, new Particle.DustOptions(Color.fromRGB(255, 192, 203), 1));
                }
            }
        }

        this.setItemAmount(item, itemAmount);
    }

}