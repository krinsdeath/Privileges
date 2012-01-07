package net.krinsoft.privileges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.World;
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
        reload();
    }

    final public void reload() {
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
        String group = plugin.getUserNode(player).getString("group", plugin.getConfig().getString("default_group", null));
        List<String> groups = calculateGroupTree(group, "");
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

    public List<String> calculateGroupTree(String group, String next) {
        System.out.println(next + "> " + group);
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
            for (String trunk : calculateBackwardTree(top, next + "-")) {
                tree.add(0, trunk);
            }
        }
        return tree;
    }
    
    private List<String> calculateBackwardTree(String group, String next) {
        System.out.println(next + "> " + group);
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
            for (String trunk : calculateBackwardTree(top, next + "-")) {
                tree.add(trunk);
            }
        }
        return tree;
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
        try {
            for (String node : plugin.getGroupNode(group).getStringList("permissions")) {
                // attach the node
                attachNode(attachment, node);
            }
        } catch (NullPointerException e) {
            plugin.debug("Encountered null path at '" + group + ".permissions' in groups.yml");
            plugin.debug("Bug Bukkit about pull request #455");
        }
        // calculate group's world permissions
        // overrides global permissions
        calculateGroupWorldPermissions(attachment, player, group);
    }

    private void calculateGroupWorldPermissions(PermissionAttachment attachment, String player, String group) {
        // Iterate through each world
        String world = players.get(player);
        try {
            for (String node : plugin.getGroupNode(group).getStringList("worlds." + world)) {
                // iterate through this world's nodes
                attachNode(attachment, node);
            }
        } catch (NullPointerException e) {
            plugin.debug("Encountered null path at '" + group + ".worlds." + world + "' in groups.yml");
            plugin.debug("Bug Bukkit about pull request #455");
        }
    }

    private void calculatePlayerPermissions(PermissionAttachment attachment, String player) {
        try {
            for (String node : plugin.getUserNode(player).getStringList("permissions")) {
                attachNode(attachment, node);
            }
        } catch (NullPointerException e) {
            plugin.debug("Encountered null path at '" + player + ".permissions' in users.yml");
            plugin.debug("Bug Bukkit about pull request #455");
        }
        try {
            if (plugin.getUserNode(player).getConfigurationSection("worlds").getKeys(false) == null) {
                for (World w : plugin.getServer().getWorlds()) {
                    plugin.getUsers().set("users." + player + ".worlds." + w.getName(), new ArrayList<String>(0));
                }
                plugin.saveUsers();
            }
        } catch (NullPointerException e) {
            plugin.debug("Encounter null path at '" + player + ".worlds' in users.yml");
        }
        try {
            for (String world : plugin.getUserNode(player).getConfigurationSection("worlds").getKeys(false)) {
                try {
                    for (String node : plugin.getUserNode(player).getStringList("worlds." + world)) {
                        attachNode(attachment, node);
                    }
                } catch (NullPointerException e) {
                    plugin.debug("Encountered null path at '" + player + ".worlds." + world + "' in users.yml");
                    plugin.debug("Bug Bukkit about pull request #455");
                }
            }
        } catch (NullPointerException e) {
            plugin.debug("Encounter null path at '" + player + ".worlds' in users.yml");
            plugin.debug("Bug Bukkit about pull request #455");
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
