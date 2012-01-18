package net.krinsoft.privileges.groups;

import java.util.*;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.events.GroupChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

/**
 *
 * @author krinsdeath
 */
public class GroupManager {
    private Privileges plugin;
    private String DEFAULT;
    private Map<String, Group> groupList = new HashMap<String, Group>();
    private Map<String, Group> players = new HashMap<String, Group>();
    private Map<Integer, String> promotion = new TreeMap<Integer, String>();

    public GroupManager(Privileges plugin) {
        this.plugin = plugin;
        RankedGroup.plugin = plugin;
        this.DEFAULT = plugin.getConfig().getString("default_group", "default");
        for (String group : plugin.getGroups().getConfigurationSection("groups").getKeys(false)) {
            Group g = getGroup(group);
            if (g == null) { continue; }
            if (promotion.get(g.getRank()) != null) {
                plugin.debug("Duplicate rank found! " + group + "->" + promotion.get(g.getRank()));
            }
            promotion.put(g.getRank(), g.getName());
        }
    }

    public Group getDefaultGroup() {
        return getGroup(this.DEFAULT);
    }

    /**
     * Promotes the specified player to the next higher ranked group
     * @param sender The CommandSender issuing the promotion
     * @param player The player to promote
     * @return The new rank of the player after promotion
     */
    public int promote(CommandSender sender, Player player) {
        int send = getRank(sender);
        int rank = getRank(player);
        boolean next = false;
        if (rank >= send) {
            sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + "'s rank is too high for you to promote him/her.");
            return -1;
        }
        for (Integer i : promotion.keySet()) {
            if (next) { rank = i; break; }
            if (i == rank) { next = true; }
        }
        if (rank < send) {
            setGroup(player.getName(), promotion.get(rank));
            return getRank(player);
        } else {
            sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + "'s rank is too high for you to promote him/her.");
            return -1;
        }
    }
    
    /**
     * Adds the specified player to the specified group
     * @param player The player to change
     * @param group The group to set
     */
    public void addPlayer(String player, String group) {
        Group g = createGroup(group);
        players.put(player, g);
    }

    /**
     *
     * Removes all groups from the specified player.
     * @param player
     * The player whose groups we're removing
     */
    public void removePlayer(String player) {
        this.players.remove(player);
    }

    /**
     * Gets the specified sender's rank
     * @param sender The sender (player or console) to get
     * @return the sender's group rank, 2^32-1 for console, or 0 for unknown
     */
    public int getRank(CommandSender sender) {
        if (sender instanceof Player) {
            return players.get(sender.getName()).getRank();
        } else if (sender instanceof ConsoleCommandSender) {
            return Integer.MAX_VALUE;
        } else {
            return 0;
        }
    }

    /**
     * Set a player's group to the specified group by name
     * @param player The player whose group we're changing
     * @param group The name of the group (case-insensitive) to switch to
     */
    public void setGroup(String player, String group) {
        // make sure the group is valid
        if (getGroup(group) == null) { return; }

        // update the player's group in the configuration
        plugin.getUsers().set("users." + player + ".group", group);
        plugin.saveUsers();

        // update the player's values
        players.put(player, getGroup(group));

        // reload the permissions
        plugin.getPermissionManager().registerPlayer(player);

        // throw a group change event to let other plugins know of the change
        plugin.getServer().getPluginManager().callEvent(new GroupChangeEvent(getGroup(group), plugin.getServer().getPlayer(player)));
    }

    /**
     * Gets the specified group by name (case-insensitive)
     * @param group The group's name.
     * @return the group instance, or null
     * @see #getGroup(org.bukkit.entity.Player)
     */
    public Group getGroup(String group) {
        try {
            plugin.debug("-> trying Group.getName() for '" + group + "'");
            createGroup(group).getName();
        } catch (NullPointerException e) {
            return null;
        }
        return groupList.get(group.toLowerCase());
    }

    public Group removeGroup(String group) {
        Group g = getGroup(group);
        if (g == null) { return null; }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (players.get(player.getName()).equals(g)) {
                players.put(player.getName(), getDefaultGroup());
                plugin.getServer().getPluginManager().callEvent(new GroupChangeEvent(getDefaultGroup(), player));
            }
        }
        plugin.getPermissionManager().reload();
        return groupList.remove(group.toLowerCase());
    }

    /**
     * Gets the specified player's group
     * @param player The player whose group we're fetching
     * @return the group associated with this player
     */
    public Group getGroup(Player player) {
        try {
            return players.get(player.getName());
        } catch (NullPointerException e) {
            return null;
        }
    }

    protected Group createGroup(String group) {
        if (groupList.containsKey(group.toLowerCase())) {
            return groupList.get(group.toLowerCase());
        } else {
            if (plugin.getGroupNode(group) == null) {
                return null;
            }
            List<String> tree = plugin.getPermissionManager().calculateGroupTree(group, "");
            groupList.put(group.toLowerCase(), new RankedGroup(group, plugin.getGroupNode(group).getInt("rank", 1), tree));
            Permission perm = new Permission("group." + group);
            perm.setDescription("A permission node that relates directly to the group: " + group);
            if (plugin.getServer().getPluginManager().getPermission(perm.getName()) == null) {
                plugin.getServer().getPluginManager().addPermission(perm);
            }
            return groupList.get(group.toLowerCase());
        }
    }

}
