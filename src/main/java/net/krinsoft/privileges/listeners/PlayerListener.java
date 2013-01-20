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
import org.bukkit.event.player.PlayerLoginEvent;
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
    public void playerLogin(PlayerLoginEvent event) {
        plugin.getPlayerManager().register(event.getPlayer()); // register player for early perm checks
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerFailLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            plugin.getPlayerManager().unregister(event.getPlayer().getName()); // unregister if player is prevented from joining
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoin(PlayerJoinEvent event) {
        plugin.getPlayerManager().register(event.getPlayer()); // re-register player for world permissions
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(PlayerQuitEvent event) {
        plugin.getPlayerManager().unregister(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerChangedWorld(PlayerChangedWorldEvent event) {
        plugin.getPlayerManager().register(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        Block b = event.getClickedBlock();
        if (b == null) { return; }
        String[] perm = new String[] { "privileges.interact." + b.getTypeId(), "privileges.interact." + b.getType().name() };
        Player p = event.getPlayer();
        if (!p.hasPermission("privileges.interact")) {
            //plugin.debug("Checking " + perm[0] + "...");
            if (p.isPermissionSet(perm[0])) {
                if (!p.hasPermission(perm[0])) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to interact with " + ChatColor.GOLD + b.getType().name() + ChatColor.RED + "!");
                    event.setCancelled(true);
                    return;
                } else {
                    return;
                }
            }
            //plugin.debug("Checking " + perm[1] + "...");
            if (p.isPermissionSet(perm[1])) {
                if (!p.hasPermission(perm[1])) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to interact with " + ChatColor.GOLD + b.getType().name() + ChatColor.RED + "!");
                    event.setCancelled(true);
                } else {
                    return;
                }
            }
            p.sendMessage(ChatColor.RED + "You do not have permission to interact with that!");
            event.setCancelled(true);
        } else {
            //plugin.debug("Checking " + perm[0] + "...");
            if (p.isPermissionSet(perm[0])) {
                if (!p.hasPermission(perm[0])) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to interact with " + ChatColor.GOLD + b.getType().name() + ChatColor.RED + "!");
                    event.setCancelled(true);
                    return;
                }
            }
            //plugin.debug("Checking " + perm[1] + "...");
            if (p.isPermissionSet(perm[1])) {
                if (!p.hasPermission(perm[1])) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to interact with " + ChatColor.GOLD + b.getType().name() + ChatColor.RED + "!");
                    event.setCancelled(true);
                }
            }
        }
    }

}
