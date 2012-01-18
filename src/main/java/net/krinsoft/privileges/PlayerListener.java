package net.krinsoft.privileges;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.player.*;

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
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        plugin.getPermissionManager().updatePlayerWorld(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) { return; }
        Block b = event.getClickedBlock();
        if (!event.getPlayer().hasPermission("privileges.interact") || (b != null && !event.getPlayer().hasPermission("privileges.interact." + b.getTypeId()))) {
            event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to do that!");
            event.setCancelled(true);
        }
    }

}
