package net.krinsoft.privileges;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

/**
 *
 * @author krinsdeath
 */
@SuppressWarnings("unused")
class PlayerListener implements Listener {

    private Privileges plugin;

    public PlayerListener(Privileges plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoin(PlayerJoinEvent event) {
        plugin.getPermissionManager().registerPlayer(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(PlayerQuitEvent event) {
        plugin.getPermissionManager().unregisterPlayer(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerChangedWorld(PlayerChangedWorldEvent event) {
        plugin.getPermissionManager().updatePlayerWorld(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) { return; }
        Block b = event.getClickedBlock();
        if (!event.getPlayer().hasPermission("privileges.interact") || (b != null && !event.getPlayer().hasPermission("privileges.interact." + b.getTypeId()))) {
            event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to do that!");
            event.setCancelled(true);
        }
    }

}
