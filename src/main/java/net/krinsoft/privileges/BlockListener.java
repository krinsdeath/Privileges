package net.krinsoft.privileges;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author krinsdeath
 */
@SuppressWarnings("unused")
class BlockListener implements Listener {
    private Privileges plugin;

    public BlockListener(Privileges plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void blockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) { return; }
        if (!event.getPlayer().hasPermission("privileges.build")) {
            event.getPlayer().sendMessage("You don't have permission to do that!");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void blockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) { return; }
        if (!event.getPlayer().hasPermission("privileges.build")) {
            event.getPlayer().sendMessage("You don't have permission to do that!");
            event.setCancelled(true);
        }
    }

}
