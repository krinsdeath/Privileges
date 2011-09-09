package net.krinsoft.privileges.groups;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author krinsdeath
 */
public class GroupManager {
    private Privileges plugin;
    private HashMap<String, Group> groupList = new HashMap<String, Group>();
    private HashMap<String, Integer> ranks = new HashMap<String, Integer>();
    private HashMap<String, Group> players = new HashMap<String, Group>();

    public GroupManager(Privileges plugin) {
        this.plugin = plugin;
    }

    /**
     * Adds the specified player to the specified group
     * @param player The player to change
     * @param group The group to set
     */
    public void addPlayer(String player, String group) {
        Group g = createGroup(group);
        if (ranks.get(player) == null || g.getRank() > ranks.get(player)) {
            ranks.put(player, g.getRank());
        }
        players.put(player, g);
    }

    /**
     * Removes all groups from the specified player.
     * @param player
     */
    public void removePlayer(String player) {
        this.players.remove(player);
        this.ranks.remove(player);
    }

    public int getHighestRank(CommandSender sender) {
        if (sender instanceof Player) {
            return ranks.get(((Player) sender).getName());
        } else if (sender instanceof ConsoleCommandSender) {
            return Integer.MAX_VALUE;
        } else {
            return 0;
        }
    }

    public void setGroup(String player, String group) {
        plugin.getUsers().setProperty("users." + player + ".group", group);
        plugin.getUsers().save();
        players.put(player, createGroup(group));
        ranks.put(player, players.get(player).getRank());
        plugin.getServer().dispatchCommand(new ConsoleCommandSender(plugin.getServer()), "priv reload");
    }

    public Group getGroup(String name) {
        return createGroup(name);
    }

    protected Group createGroup(String group) {
        if (groupList.containsKey(group.toLowerCase())) {
            return groupList.get(group.toLowerCase());
        } else {
            groupList.put(group.toLowerCase(), new Group(group, plugin.getGroupNode(group).getInt("rank", 1)));
            return groupList.get(group.toLowerCase());
        }
    }

}
