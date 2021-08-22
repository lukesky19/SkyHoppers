package net.skycraftia.skyhoppers.listener;

import net.skycraftia.skyhoppers.SkyHoppersPlugin;
import net.skycraftia.skyhoppers.gui.HopperGUI;
import net.skycraftia.skyhoppers.manager.HopperManager;
import net.skycraftia.skyhoppers.manager.MessageManager;
import net.skycraftia.skyhoppers.obj.SkyHopper;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class PlayerListeners implements Listener {

    private final SkyHoppersPlugin plugin;
    private final HopperManager hopperManager;
    private final MessageManager msg;

    public PlayerListeners(final SkyHoppersPlugin plugin) {
        this.plugin = plugin;
        this.hopperManager = this.plugin.getManager(HopperManager.class);
        this.msg = this.plugin.getManager(MessageManager.class);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHopperMenuOpen(PlayerInteractEvent event) {

        final Player player = event.getPlayer();

        if (!event.hasBlock() || event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        final Block block = event.getClickedBlock();
        if (block == null)
            return;

        if (!(block.getState() instanceof Hopper hopper))
            return;

        final Optional<SkyHopper> customHopper = this.hopperManager.getHopperFromBlock(hopper);
        if (customHopper.isEmpty())
            return;

        if (player.isSneaking())
            return;

        event.setCancelled(true);

        if (this.plugin.getLinkingPlayers().containsKey(player.getUniqueId())) {
            return;
        }

        new HopperGUI(plugin).create(customHopper.get(), player);

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHopperLink(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!event.hasBlock() || event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        final Block block = event.getClickedBlock();
        if (block == null)
            return;

        if (!(block.getState() instanceof Container container))
            return;

        if (!SkyHopper.validContainers().contains(block.getType()))
            return;

        if (!this.plugin.getLinkingPlayers().containsKey(player.getUniqueId()))
            return;

        final SkyHopper skyHopper = this.plugin.getLinkingPlayers().get(player.getUniqueId());
        assert skyHopper != null;

        event.setCancelled(true);
        this.plugin.getLinkingPlayers().remove(player.getUniqueId());

        if (skyHopper.getLinked() != null && skyHopper.getLinked().getBlock().getLocation().equals(container.getBlock().getLocation())) {
            skyHopper.setLinked(null);
            this.hopperManager.saveHopper(skyHopper);
            msg.send(player, "unlinked-container");
            return;
        }

        skyHopper.setLinked(container);
        this.hopperManager.saveHopper(skyHopper);
        msg.send(player, "linked-container");
    }
}
