package net.krinsoft.privileges;

import java.util.HashMap;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

/**
 *
 * @author krinsdeath
 */
public class PermissionManager {
    private final static Pattern NEGATIVE = Pattern.compile("-(.*)");

    private Privileges plugin;
    private HashMap<String, String> players = new HashMap<String, String>();

    public PermissionManager(Privileges plugin) {
        this.plugin = plugin;
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            registerPlayer(p.getName());
        }
    }


    final protected void registerPlayer(String player) {
        // build attachment
        PermissionAttachment attachment = plugin.getServer().getPlayer(player).addAttachment(plugin);
        if (!players.containsKey(player)) {
            players.put(player, plugin.getServer().getPlayer(player).getWorld().getName());
        }
        if (attachment == null) { // make sure nothing has gone awry
            plugin.debug("Attachment cannot be null.");
            return;
        }
        for (String node : attachment.getPermissions().keySet()) {
            plugin.debug("Removing '" + node + "' from '" + player + "'");
            attachment.unsetPermission(node);
        }
        // iterate through the player's groups
        for (String group : plugin.getUserNode(player).getStringList("groups", null)) {
            // calculate group's permissions
            calculateGroupPermissions(attachment, player, group);
        }
        // calculate player's permissions
        // overrides group and world permissions
        calculatePlayerPermissions(attachment, player);
        plugin.getServer().getPlayer(player).recalculatePermissions();
    }

    final protected void unregisterPlayer(String player) {
        if (players.containsKey(player)) {
            players.remove(player);
        } else {
            plugin.debug("Unregistering '" + player + "': player isn't registered.");
        }
    }

    final protected void disable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            unregisterPlayer(player.getName());
        }
    }

    protected void updatePlayerWorld(String player, String world) {
        if (players.containsKey(player)) {
            String w = players.get(player);
            if (!world.equals(w)) {
                plugin.debug(player + " moved to world '" + world + "'... recalculating");
                unregisterPlayer(player);
                players.put(player, world);
                registerPlayer(player);
            }
        }
    }

    private void calculateGroupPermissions(PermissionAttachment attachment, String player, String group) {
        // iterate through the group's global permissions
        for (String node : plugin.getGroupNode(group).getStringList("permissions", null)) {
            // attach the node
            attachNode(attachment, node);
        }

        // calculate group's world permissions
        // overrides global permissions
        calculateGroupWorldPermissions(attachment, player, group);

        // calculate group inheritance
        // inherited nodes are ignored if the child has already set them
        calculateGroupInheritance(attachment, player, group);
    }

    private void calculateGroupWorldPermissions(PermissionAttachment attachment, String player, String group) {
        // Iterate through each world
        for (String node : plugin.getGroupNode(group).getStringList("worlds." + players.get(player), null)) {
            // iterate through this world's nodes
            attachNode(attachment, node);
        }
    }

    private void calculateGroupInheritance(PermissionAttachment attachment, String player, String group) {
        // iterate through inherited groups
        plugin.debug("Calculating inheritance for '" + group + "'");
        for (String parent : plugin.getGroupNode(group).getStringList("inheritance", null)) {
            plugin.debug("-- parent -> " + parent);
            if (parent.equalsIgnoreCase(group)) {
                plugin.debug("Warning! '" + group + "' is inheriting from itself!");
                continue;
            }
            calculateGroupPermissions(attachment, player, parent);
        }
    }

    private void calculatePlayerPermissions(PermissionAttachment attachment, String player) {
        for (String node : plugin.getUserNode(player).getStringList("permissions", null)) {
            overrideNode(attachment, node);
        }
    }

    private void attachNode(PermissionAttachment attachment, String node) {
        if (attachment.getPermissible().isPermissionSet(NEGATIVE.matcher(node).replaceAll("$1"))) {
            plugin.debug("parent attempting to override child: " + node);
            return;
        }
        if (node.startsWith("-")) {
            attachment.setPermission(NEGATIVE.matcher(node).replaceAll("$1"), false);
        } else {
            attachment.setPermission(node, true);
        }
        plugin.debug("Setting " + node);
    }

    private void overrideNode(PermissionAttachment attachment, String node) {
        if (node.startsWith("-")) {
            attachment.setPermission(NEGATIVE.matcher(node).replaceAll("$1"), false);
        } else {
            attachment.setPermission(node, true);
        }
    }

}
