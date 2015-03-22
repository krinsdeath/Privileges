package net.krinsoft.privileges.players;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;

/**
 * An player manager that handles the creation and removal of player permissions within Privileges
 * @author krinsdeath
 */
public class PlayerManager {
    private final Privileges plugin;
    private final Map<UUID, Player> players = new HashMap<UUID, Player>();
    private final Map<UUID, PermissionAttachment> attachment_cache = new HashMap<UUID, PermissionAttachment>();

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
        Player priv = players.get(ply.getUniqueId());
        if (priv == null) {
            priv = new PrivilegesPlayer(plugin, ply);
            players.put(ply.getUniqueId(), priv);
        }
        Group group = plugin.getGroupManager().addPlayerToGroup(ply.getUniqueId(), plugin.getUserNode(ply.getUniqueId()).getString("group"));
        if (group == null) {
            // no group was found for the player, so set them to default
            group = plugin.getGroupManager().setGroup(ply.getUniqueId(), plugin.getGroupManager().getDefaultGroup().getName());
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
        attachment_cache.put(player.getUniqueId(), attachment);
        return true;
    }

    public void changeWorld(org.bukkit.entity.Player player, World world) {
        Player priv = players.get(player.getUniqueId());
        Validate.notNull(priv);
        Validate.notNull(priv.getGroup());
        PermissionAttachment attachment = attachment_cache.get(player.getUniqueId());
        Validate.notNull(attachment);
        attachment.unsetPermission(priv.getGroup().getMasterPermission(world.getName()));
        attachment.unsetPermission(priv.getMasterPermission(world.getName()));
        attachment.setPermission(priv.getGroup().getMasterPermission(player.getWorld().getName()), true);
        attachment.setPermission(priv.getMasterPermission(player.getWorld().getName()), true);
    }

    public void disable() {
        for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
            unregister(p.getUniqueId());
        }
    }

    public void reload() {
        for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
            register(p);
        }
    }

    public void unregister(UUID UUID) {
        Player player = players.remove(UUID);
        if (player != null) {
            attachment_cache.remove(UUID);
            for (World world : plugin.getServer().getWorlds()) {
                String node = player.getMasterPermission(world.getName());
                plugin.getServer().getPluginManager().removePermission(node);
            }
            plugin.debug(UUID + " was successfully unregistered.");
        } else {
            plugin.debug(UUID + " was already unregistered!");
        }
    }

    public Player getPlayer(UUID UUID) {
        Player player = players.get(UUID);
        if (player == null) {
            player = new PrivilegesPlayer(plugin, plugin.getServer().getOfflinePlayer(UUID));
        }
        return player;
    }


    /**
     * Calculates the group tree for the player by starting at the base inherited group and calculating upwards
     * @param group The name of the group whose inheritance tree we're calculating
     * @return The calculated group inheritance tree
     */
    public List<String> calculateGroupTree(String group) {
        List<String> tree = new ArrayList<String>();
        tree.add(0, group);
        List<String> inheritance;
        try {
            inheritance = plugin.getGroupNode(group).getStringList("inheritance");
        } catch (NullPointerException e) {
            return tree;
        }
        for (String top : inheritance) {
            if (top.equalsIgnoreCase(group)) { continue; }
            for (String trunk : calculateBackwardTree(top)) {
                tree.add(0, trunk);
            }
        }
        return tree;
    }

    /**
     * Calculates the reverse sorted inheritance tree for the specified group
     * @param group The name of the group that we're reverse sorting
     * @return The backwards inheritance tree
     */
    private List<String> calculateBackwardTree(String group) {
        List<String> tree = new ArrayList<String>();
        tree.add(group);
        List<String> inheritance;
        try {
            inheritance = plugin.getGroupNode(group).getStringList("inheritance");
        } catch (NullPointerException e) {
            return tree;
        }
        for (String top : inheritance) {
            if (top.equalsIgnoreCase(group)) { continue; }
            for (String trunk : calculateBackwardTree(top)) {
                tree.add(trunk);
            }
        }
        return tree;
    }
}
