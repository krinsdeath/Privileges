package net.krinsoft.privileges.players;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.OfflinePlayer;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * An player manager that handles the creation and removal of player permissions within Privileges
 * @author krinsdeath
 */
public class PlayerManager {
    private final Privileges plugin;
    private final Map<String, Player> players = new HashMap<String, Player>();

    public PlayerManager(Privileges plugin) {
        this.plugin = plugin;
    }

    public boolean register(String player) {
        return register(plugin.getServer().getOfflinePlayer(player));
    }

    public boolean register(OfflinePlayer ply) {
        if (ply == null || ply.getPlayer() == null) {
            plugin.debug("Attempted permission registration of a player that was offline or didn't exist!");
            return false;
        }
        Player priv = players.get(ply.getName());
        if (priv == null) {
            priv = players.put(ply.getName(), new PrivilegesPlayer(plugin, ply));
        }
        Group group = plugin.getGroupManager().getGroup(ply);
        if (group == null) {
            plugin.debug("The specified player's group doesn't exist!");
            return false;
        }
        org.bukkit.entity.Player player = ply.getPlayer();
        // clear the player's permissions
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            PermissionAttachment att = info.getAttachment();
            if (att == null) { continue; }
            att.unsetPermission(info.getPermission());
        }
        // build the attachment
        PermissionAttachment attachment = player.addAttachment(plugin);
        attachment.setPermission(group.getMasterPermission(player.getWorld().getName()), true);
        attachment.setPermission(priv.getMasterPermission(player.getWorld().getName()), true);
        return true;
    }

    public void unregister(String name) {
        Player player = players.remove(name);
        if (player != null) {
            plugin.debug(name + " was successfully unregistered.");
        } else {
            plugin.debug(name + " was already unregistered!");
        }
    }

    public Player getPlayer(String name) {
        return players.get(name);
    }

}
