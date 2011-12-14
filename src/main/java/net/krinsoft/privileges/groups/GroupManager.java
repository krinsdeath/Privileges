package net.krinsoft.privileges.groups;

import java.util.HashMap;
import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.events.GroupChangeEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author krinsdeath
 */
public class GroupManager {
    private Privileges plugin;
    private String DEFAULT;
    private HashMap<String, Group> groupList = new HashMap<String, Group>();
    private HashMap<String, Group> players = new HashMap<String, Group>();

    public GroupManager(Privileges plugin) {
        this.plugin = plugin;
        this.DEFAULT = plugin.getConfiguration().getString("default_group", "default");
    }

    public Group getDefaultGroup() {
        return getGroup(this.DEFAULT);
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
     * Removes all groups from the specified player.
     * @param player
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
            return players.get(((Player)sender).getName()).getRank();
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
     * @see getGroup(Player)
     */
    public Group getGroup(String group) {
        try {
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
     * @param player
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
            groupList.put(group.toLowerCase(), new Group(group, plugin.getGroupNode(group).getInt("rank", 1)));
            return groupList.get(group.toLowerCase());
        }
    }

}
