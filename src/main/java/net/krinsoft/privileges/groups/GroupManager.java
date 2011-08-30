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
    private HashMap<String, LinkedList<Group>> players = new HashMap<String, LinkedList<Group>>();

    public GroupManager(Privileges plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(String player, List<String> groups) {
        LinkedList<Group> list = new LinkedList<Group>();
        for (String name : groups) {
            list.add(new Group(name, plugin.getGroupNode(name).getInt("rank", 1), plugin.getGroupNode(name).getStringList("permissions", null)));
            if (groupList.get(name) == null) {
                groupList.put(name, list.getLast());
            }
            if (ranks.get(player) == null || list.getLast().getRank() > ranks.get(player)) {
                ranks.put(player, list.getLast().getRank());
            }
        }
        this.players.put(player, list);
    }

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

    public boolean addGroup(String player, String group) {
        if (players.containsKey(player)) {
            return players.get(player).add(getGroup(group));
        } else {
            return false;
        }
    }

    public boolean removeGroup(String player, String group) {
        if (players.containsKey(player)) {
            return players.get(player).remove(getGroup(group));
        } else {
            return false;
        }
    }

    public Group getGroup(String name) {
        return groupList.get(name);
    }

    public Group createGroup(String group) {
        if (groupList.containsKey(group)) {
            return groupList.get(group);
        } else {
            groupList.put(group, new Group(group, plugin.getGroupNode(group).getInt("rank", 1), plugin.getGroupNode(group).getStringList("permissions", null)));
            return groupList.get(group);
        }
    }
}
