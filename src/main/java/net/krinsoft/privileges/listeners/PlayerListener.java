package net.krinsoft.privileges.listeners;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author krinsdeath
 */
@SuppressWarnings("unused")
public class PlayerListener implements Listener {

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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        if (!p.hasPermission("privileges.interact")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to interact with things!");
            event.setCancelled(true);
        }
        if (b != null && p.isPermissionSet("privileges.interact." + b.getTypeId()) && !p.hasPermission("privileges.interact." + b.getTypeId())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to interact with " + b.getType().name());
            event.setCancelled(true);
        }
    }

}
