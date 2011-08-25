package net.krinsoft.privileges;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author krinsdeath
 */
class BlockListener extends org.bukkit.event.block.BlockListener {
    private Privileges plugin;

    public BlockListener(Privileges plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) { return; }
        if (!event.getPlayer().hasPermission("privileges.build")) {
            event.getPlayer().sendMessage("You don't have permission to do that!");
            event.setCancelled(true);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) { return; }
        if (!event.getPlayer().hasPermission("privileges.build")) {
            event.getPlayer().sendMessage("You don't have permission to do that!");
            event.setCancelled(true);
        }
    }

}
