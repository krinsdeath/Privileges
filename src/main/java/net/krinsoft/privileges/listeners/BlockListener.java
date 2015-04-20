package net.krinsoft.privileges.listeners;

import net.krinsoft.privileges.Privileges;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author krinsdeath
 */
public class BlockListener implements Listener {

    public BlockListener(Privileges plugin) {
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void blockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().hasPermission("privileges.build")) {
            event.getPlayer().sendMessage("You don't have permission to do that!");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().hasPermission("privileges.build")) {
            event.getPlayer().sendMessage("You don't have permission to do that!");
            event.setCancelled(true);
        }
    }

}
