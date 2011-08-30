package net.krinsoft.privileges;

import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author krinsdeath
 */
class PlayerListener extends org.bukkit.event.player.PlayerListener {

    private Privileges plugin;

    public PlayerListener(Privileges plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getPermissionManager().registerPlayer(event.getPlayer().getName());
    }

    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.isCancelled()) { return; }
        plugin.getPermissionManager().unregisterPlayer(event.getPlayer().getName());
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPermissionManager().unregisterPlayer(event.getPlayer().getName());
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) { return; }
        plugin.getPermissionManager().updatePlayerWorld(event.getPlayer().getName(), event.getTo().getWorld().getName());
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) { return; }
        plugin.getPermissionManager().updatePlayerWorld(event.getPlayer().getName(), event.getTo().getWorld().getName());
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) { return; }
        if (!event.getPlayer().hasPermission("privileges.interact")) {
            event.getPlayer().sendMessage("You do not have permission to do that!");
            event.setCancelled(true);
        }
    }

}
