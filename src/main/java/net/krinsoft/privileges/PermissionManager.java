package net.krinsoft.privileges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private HashMap<String, PermissionAttachment> perms = new HashMap<String, PermissionAttachment>();

    public PermissionManager(Privileges plugin) {
        this.plugin = plugin;
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            registerPlayer(p.getName());
        }
    }


    final public void registerPlayer(String player) {
        // build attachment
        PermissionAttachment attachment = plugin.getServer().getPlayer(player).addAttachment(plugin);
        if (perms.containsKey(player)) {
            attachment = perms.get(player);
        }
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
        // iterate through the player's groups, and add them to a list
        String group = plugin.getUserNode(player).getString("group", plugin.getConfiguration().getString("default_group", null));
        List<String> groups = calculateGroupTree(group);
        plugin.debug("Group tree: " + groups.toString());
        plugin.getGroupManager().addPlayer(player, group);
        // calculate group's permissions
        for (String branch : groups) {
            plugin.debug("Calculating nodes: " + branch);
            calculateGroupPermissions(attachment, player, branch);
        }
        // calculate player's permissions
        // overrides group and world permissions
        calculatePlayerPermissions(attachment, player);
        plugin.getServer().getPlayer(player).recalculatePermissions();
        perms.put(player, attachment);
    }

    protected List<String> calculateGroupTree(String group) {
        List<String> groups = new ArrayList<String>();
        groups.add(0, group);
        for (String top : plugin.getGroupNode(group).getStringList("inheritance", new ArrayList<String>())) {
            if (top.equalsIgnoreCase(group)) { continue; }
            groups.add(0, top);
            for (String trunk : calculateBackwardsGroupTree(top)) {
                if (trunk.equalsIgnoreCase(top)) { continue; }
                groups.add(0, trunk);
            }
        }
        return groups;
    }

    protected List<String> calculateBackwardsGroupTree(String group) {
        List<String> groups = new ArrayList<String>();
        groups.add(0, group);
        for (String top : plugin.getGroupNode(group).getStringList("inheritance", new ArrayList<String>())) {
            if (top.equalsIgnoreCase(group)) { continue; }
            groups.add(top);
            for (String trunk : calculateGroupTree(top)) {
                if (trunk.equalsIgnoreCase(top)) { continue; }
                groups.add(trunk);
            }
        }
        return groups;
    }

    final protected void unregisterPlayer(String player) {
        if (players.containsKey(player)) {
            PermissionAttachment att = perms.get(player); // stupid, this reference has to exist to unset permissions on reload :|
            if (att == null) {
                plugin.debug("'" + player + "' unregistering: Attachment is null");
            } else {
                for (String key : att.getPermissions().keySet()) {
                    att.unsetPermission(key);
                }
            }
            players.remove(player);
            perms.remove(player);
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
        for (String node : plugin.getGroupNode(group).getStringList("permissions", new ArrayList<String>())) {
            // attach the node
            attachNode(attachment, node);
        }

        // calculate group's world permissions
        // overrides global permissions
        calculateGroupWorldPermissions(attachment, player, group);
    }

    private void calculateGroupWorldPermissions(PermissionAttachment attachment, String player, String group) {
        // Iterate through each world
        for (String node : plugin.getGroupNode(group).getStringList("worlds." + players.get(player), new ArrayList<String>())) {
            // iterate through this world's nodes
            attachNode(attachment, node);
        }
    }

    private void calculatePlayerPermissions(PermissionAttachment attachment, String player) {
        for (String node : plugin.getUserNode(player).getStringList("permissions", new ArrayList<String>())) {
            attachNode(attachment, node);
        }
    }

    private void attachNode(PermissionAttachment attachment, String node) {
        String debug = NEGATIVE.matcher(node).replaceAll("$1");
        if (node.startsWith("-")) {
            attachment.setPermission(NEGATIVE.matcher(node).replaceAll("$1"), false);
        } else {
            attachment.setPermission(node, true);
        }
        debug = (attachment.getPermissible().isPermissionSet(debug) ? "overriding " : "setting ") + debug + " to " + attachment.getPermissible().hasPermission(debug);
        plugin.debug(debug);
    }

}
