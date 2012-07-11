package net.krinsoft.privileges;

import net.krinsoft.privileges.groups.Group;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class PermissionManager {
    private Privileges plugin;

    private HashMap<String, String> players = new HashMap<String, String>();

    public PermissionManager(Privileges plugin) {
        this.plugin = plugin;
    }

    public void clean() {
        players.clear();
    }

    public void reload() {
        long time = System.nanoTime();
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            registerPlayer(p.getName());
        }
        time = System.nanoTime() - time;
        plugin.profile(time, "registration_player_all");
    }

    /**
     * Attempts to register a player by calculating their groups and permissions nodes
     * @param player The name of the player we're registering
     * @return true if the registration succeeds, otherwise false
     */
    final public boolean registerPlayer(String player) {
        plugin.debug("Attempting registration of player " + player + "...");
        long time = System.nanoTime();
        // validate the player
        Player ply = plugin.getServer().getPlayer(player);
        if (ply == null) {
            plugin.debug("Player '" + player + "' couldn't be found.");
            return false;
        }
        player = ply.getName();
        // build attachment
        try {
            Field f = org.bukkit.craftbukkit.entity.CraftHumanEntity.class.getDeclaredField("perm");
            f.setAccessible(true);
            PermissibleBase permissible = (PermissibleBase) f.get(ply);
            permissible.clearPermissions();
        } catch (NoSuchFieldException e) {
            plugin.warn("Unknown field: " + e.getMessage());
        } catch (IllegalAccessException e) {
            plugin.warn("Illegal access: " + e.getMessage());
        }
        PermissionAttachment attachment = ply.addAttachment(plugin);
        // iterate through the player's groups, and add them to a list
        String g = plugin.getUserNode(player).getString("group");
        Group group = plugin.getGroupManager().addPlayerToGroup(player, g);
        // calculate group's permissions
        attachNode(attachment, group.getMasterPermission(ply.getWorld().getName()), true);
        // calculate player's permissions
        // overrides group and world permissions
        calculatePlayerPermissions(attachment, player);
        ply.recalculatePermissions();
        time = System.nanoTime() - time;
        plugin.profile(time, "registration_player");
        return true;
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

    /**
     * Removes a player from the internal map and clears out any possibly stale references
     * @param player The name of the player
     */
    public void unregisterPlayer(String player) {
        if (players.containsKey(player)) {
            players.remove(player);
        } else {
            plugin.debug("Unregistering '" + player + "': player isn't registered.");
        }
    }

    /**
     * Unregisters all players.
     */
    public void disable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            unregisterPlayer(player.getName());
        }
    }

    /**
     * Changes a player's world entry in the player map and recalculates their permissions
     * @param player The name of the player who is changing worlds
     * @param world The world to which the player is changing
     */
    public void updatePlayerWorld(String player, String world) {
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

    /**
     * Calculates player specific permissions
     * @param attachment The attachment to which we're adding the permissions
     * @param player The name of the player we're fetching permissions for
     */
    private void calculatePlayerPermissions(PermissionAttachment attachment, String player) {
        plugin.debug("Calculating player-specific permissions...");
        try {
            for (String node : plugin.getUserNode(player).getStringList("permissions")) {
                attachNode(attachment, node);
            }
        } catch (NullPointerException e) {
            plugin.debug("Encountered null path at '" + player + ".permissions' in users.yml");
        }
        plugin.debug("Calculating player-specific world permissions...");
        try {
            if (plugin.getUserNode(player).getConfigurationSection("worlds").getKeys(false) == null) {
                for (World w : plugin.getServer().getWorlds()) {
                    plugin.getUsers().set("users." + player + ".worlds." + w.getName(), new ArrayList<String>(0));
                }
                plugin.saveUsers();
            }
        } catch (NullPointerException e) {
            plugin.debug("Encountered null path at '" + player + ".worlds' in users.yml");
        }
        try {
            for (String world : plugin.getUserNode(player).getConfigurationSection("worlds").getKeys(false)) {
                try {
                    for (String node : plugin.getUserNode(player).getStringList("worlds." + world)) {
                        attachNode(attachment, node);
                    }
                } catch (NullPointerException e) {
                    plugin.debug("Encountered null path at '" + player + ".worlds." + world + "' in users.yml");
                }
            }
        } catch (NullPointerException e) {
            plugin.debug("Encounter null path at '" + player + ".worlds' in users.yml");
        }
    }

    /**
     * Attempts to add a permission to the specified attachment
     * @param attachment The attachment we're adding the permission to
     * @param node The name of the permission (prefixed with a - for false) we're adding to the attachment
     */
    private void attachNode(PermissionAttachment attachment, String node) {
        boolean val;
        String debug;
        if (node.startsWith("-")) {
            val = false;
            debug = node.substring(1);
        } else {
            val = true;
            debug = node;
        }
        String mod = (attachment.getPermissions().containsKey(debug) ? "overriding" : "setting");
        attachment.unsetPermission(debug);
        attachment.setPermission(debug, val);
        debug = mod + " " + debug + " to " + val;
        plugin.debug(debug);
    }

    /**
     * Attempts to attach a permission node to the specified attachment, with the specified value (true for on, false for off)
     * @param attachment The attachment we're adding the permission to
     * @param node The name of the permission node we're attaching
     * @param value The value associated with the permission node (true or false)
     */
    private void attachNode(PermissionAttachment attachment, String node, boolean value) {
        String mod = (attachment.getPermissions().containsKey(node) ? "overriding" : "setting");
        String msg = mod + " " + node + " to " + value;
        attachment.unsetPermission(node);
        attachment.setPermission(node, value);
        plugin.debug(msg);
    }

}
