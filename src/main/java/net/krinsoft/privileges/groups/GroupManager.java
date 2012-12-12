package net.krinsoft.privileges.groups;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.event.GroupChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A group manager that handles the creation and removal of group permissions in Privileges
 * @author krinsdeath
 */
public class GroupManager {
    private Privileges plugin;
    private String DEFAULT;
    private Map<String, Group> groupList = new HashMap<String, Group>();
    private Map<String, String> players = new HashMap<String, String>();

    public GroupManager(Privileges plugin) {
        this.plugin = plugin;
        this.DEFAULT = plugin.getConfig().getString("default_group", "default");
    }

    public void clean() {
        groupList.clear();
        players.clear();
    }

    public void reload() {
        StringBuilder line = new StringBuilder();
        Set<String> groups = plugin.getGroups().getConfigurationSection("groups").getKeys(false);
        for (String group : groups) {
            if (line.length() > 0) { line.append(", "); }
            Group g = getGroup(group);
            line.append(g.getName()).append(" (").append(g.getRank()).append(")");
        }
    }

    public Group getDefaultGroup() {
        Group g = groupList.get(DEFAULT);
        if (g == null) {
            throw new NullPointerException("An invalid default group is defined in config.yml.");
        }
        return g;
    }

    /**
     * Checks whether the specified sender's rank is higher than or equal to the target's rank
     * @param sender The sender issuing the command
     * @param target The target whose rank we're checking
     * @return true if the sender's rank is high enough, otherwise false
     * @see #checkRank(CommandSender, int)
     */
    public boolean checkRank(CommandSender sender, CommandSender target) {
        return getRank(sender) >= getRank(target) || sender instanceof ConsoleCommandSender || sender.hasPermission("privileges.self.edit");
    }

    /**
     * Checks whether the specified sender has a high enough rank to edit the specified target
     * @param sender The sender issuing the command
     * @param target The target whose rank we're comparing
     * @return true if the sender's rank is high enough, otherwise false
     * @see #checkRank(CommandSender, CommandSender)
     */
    public boolean checkRank(CommandSender sender, OfflinePlayer target) {
        return checkRank(sender, getRank(target));
    }

    /**
     * Checks whether the specified sender's rank is higher than or equal to the given rank
     * @param sender The sender issuing the command
     * @param rank The rank that we're checking against
     * @return true if the sender's rank is high enough, otherwise false
     */
    public boolean checkRank(CommandSender sender, int rank) {
        return getRank(sender) >= rank || sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender || sender.hasPermission("privileges.self.edit");
    }

    /**
     * Checks if the specified rank is already taken by a group
     * @param rank The rank to check
     * @return true if the rank is taken, otherwise false
     */
    public boolean isRankTaken(int rank) {
        for (Group g : groupList.values()) {
            if (g.getRank() == rank) {
                return true;
            }
        }
        return false;
    }

    /**
     * Promotes the specified player to the next higher ranked group
     * @param sender The person issuing the promotion
     * @param player The player being promoted
     */
    public void promote(CommandSender sender, OfflinePlayer player) {
        plugin.debug(sender.getName() + ": Running promotion for " + player.getName() + ".");
        int send = getRank(sender);
        int rank = getRank(player);
        if (rank >= send) {
            sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + "'s rank is too high for you to promote him/her.");
            return;
        }
        Group currentGroup = getGroup(player);
        if (currentGroup != null && currentGroup.hasPromotion()) {
            Group proGroup = createGroup(currentGroup.getPromotion());
            if (proGroup != null && (proGroup.getRank() < send || sender.hasPermission("privileges.self.edit"))) {
                sender.sendMessage("You have promoted " + ChatColor.GREEN + player.getName() + ChatColor.WHITE + " to the group " + ChatColor.AQUA + proGroup.getName() + ChatColor.WHITE + ".");
                if (player.getPlayer() != null) {
                    player.getPlayer().sendMessage("You have been promoted to " + ChatColor.AQUA + proGroup.getName() + ChatColor.WHITE + ".");
                }
                setGroup(player.getName(), proGroup.getName());
                return;
            }
        }
        int diff = Integer.MAX_VALUE;
        Group group = null;
        for (Group g : groupList.values()) {
            plugin.debug("Checking viability of " + g.getName() + "...");
            // check the iteration's rank - player's current rank
            // if greater than 0 and less than current difference, we have a promotion possibility
            if (g.getRank() - rank > 0 && g.getRank() - rank < diff) {
                plugin.debug("Promotion viability determined for player " + player.getName() + " to group " + g.getName() + "!");
                diff = g.getRank() - rank;
                group = g;
            }
        }
        // check that we have a possible rank, and make sure the new player's rank is less than the sender's rank
        if (group != null && (group.getRank() < send || sender.hasPermission("privileges.self.edit"))) {
            sender.sendMessage("You have promoted " + ChatColor.GREEN + player.getName() + ChatColor.WHITE + " to the group " + ChatColor.AQUA + group.getName() + ChatColor.WHITE + ".");
            if (player.getPlayer() != null) {
                player.getPlayer().sendMessage("You have been promoted to " + ChatColor.AQUA + group.getName() + ChatColor.WHITE + ".");
            }
            setGroup(player.getName(), group.getName());
            return;
        }
        sender.sendMessage("Promotion failed.");
    }

    /**
     * Promotes the specified player to the next higher ranked group
     * @param sender The person issuing the promotion
     * @param player The player being promoted
     * @see #promote(CommandSender, OfflinePlayer)
     * @deprecated since 1.6.1
     */
    @Deprecated
    public void promote(CommandSender sender, Player player) {
        promote(sender, (OfflinePlayer) player);
    }

    /**
     * Demotes the specified player to the next lowest ranked group
     * @param sender The person issuing the demotion
     * @param player The player being demoted
     */
    public void demote(CommandSender sender, OfflinePlayer player) {
        plugin.debug(sender.getName() + ": Running demotion for " + player.getName() + ".");
        int send = getRank(sender);
        int rank = getRank(player);
        if (rank >= send) {
            sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + "'s rank is too high.");
            return;
        }
        Group currentGroup = getGroup(player);
        if (currentGroup != null && currentGroup.hasDemotion()) {
            Group demGroup = createGroup(currentGroup.getDemotion());
            if (demGroup != null && (demGroup.getRank() < send || sender.hasPermission("privileges.self.edit"))) {
                sender.sendMessage("You have demoted " + ChatColor.RED + player.getName() + ChatColor.WHITE + " to the group " + ChatColor.DARK_RED + demGroup.getName() + ChatColor.WHITE + ".");
                if (player.getPlayer() != null) {
                    player.getPlayer().sendMessage("You have been demoted to " + ChatColor.DARK_RED + demGroup.getName() + ChatColor.WHITE + ".");
                }
                setGroup(player.getName(), demGroup.getName());
                return;
            }
        }
        int diff = Integer.MAX_VALUE;
        Group group = null;
        for (Group g : groupList.values()) {
            plugin.debug("Checking viability of " + g.getName() + "...");
            // check the player's current rank - the current iteration's rank
            // if greater than 0 and less than current difference, we have a demotion possibility
            if (rank - g.getRank() > 0 && rank - g.getRank() < diff) {
                plugin.debug("Demotion viability determined for player " + player.getName() + " to group " + g.getName() + "!");
                diff = rank - g.getRank();
                group = g;
            }
        }
        // check that we have a possible rank, and make sure the new player's rank is less than the sender's rank
        if (group != null && (group.getRank() < send || sender.hasPermission("privileges.self.edit"))) {
            sender.sendMessage("You have demoted " + ChatColor.RED + player.getName() + ChatColor.WHITE + " to the group " + ChatColor.DARK_RED + group.getName() + ChatColor.WHITE + ".");
            if (player.getPlayer() != null) {
                player.getPlayer().sendMessage("You have been demoted to " + ChatColor.DARK_RED + group.getName() + ChatColor.WHITE + ".");
            }
            setGroup(player.getName(), group.getName());
            return;
        }
        sender.sendMessage("Demotion failed.");
    }

    /**
     * Demotes the specified player to the next lowest ranked group
     * @param sender The person issuing the demotion
     * @param player The player being promoted
     * @see #demote(CommandSender, OfflinePlayer)
     * @deprecated since 1.6.1
     */
    @Deprecated
    public void demote(CommandSender sender, Player player) {
        demote(sender, (OfflinePlayer) player);
    }

    /**
     * Adds the specified player to the specified group
     * @param player The player to change
     * @param group The group to set
     * @return The new group for the player
     */
    public Group addPlayerToGroup(String player, String group) {
        plugin.debug("Adding player " + player + " to group " + group + "...");
        Group g = createGroup(group);
        players.put(player, g.getName());
        return g;
    }

    /**
     * Attempts to get the rank of a player
     * @param player The player whose rank is being fetched
     * @return The rank of the player
     * @see #getRank(CommandSender)
     */
    public int getRank(OfflinePlayer player) {
        try {
            return getGroup(player).getRank();
        } catch (Exception e) {
            if (player == null) {
                plugin.warn("It seems that the target was null! Did you make a typographical error?");
            }
            plugin.warn("An exception was thrown while checking a player's group rank: " + e.getLocalizedMessage());
            plugin.warn("Defaulting to rank 0.");
            return 0;
        }
    }

    /**
     * Gets the specified sender's rank
     * @param sender The sender (player or console) to get
     * @return the sender's group rank, 2^32-1 for console, or 0 for unknown
     */
    public int getRank(CommandSender sender) {
        try {
            if (sender instanceof Player) {
                return getGroup((OfflinePlayer)sender).getRank();
            } else if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
                return Integer.MAX_VALUE;
            } else {
                return Integer.MIN_VALUE;
            }
        } catch (Exception e) {
            if (sender == null) {
                plugin.warn("It seems that the sender was null! (Possible causes: offline, didn't exist)");
            }
            plugin.warn("An exception was thrown while fetching a group rank; check groups.yml.");
            plugin.warn("Defaulting to 0: " + e.getLocalizedMessage());
            return 0;
        }
    }

    /**
     * Set a player's group to the specified group by name
     * @param player The player whose group we're changing
     * @param group The name of the group (case-insensitive) to switch to
     * @return The new group of the player, or null if the specified group doesn't exist
     */
    public Group setGroup(String player, String group) {
        plugin.debug("Setting player " + player + " to group " + group + "...");
        // make sure the group is valid
        OfflinePlayer ply = plugin.getServer().getOfflinePlayer(player);
        Group orig = getGroup(ply);
        Group test = getGroup(group);
        if (test == null) { return null; }

        // update the player's group in the configuration
        plugin.getUserNode(player).set("group", test.getName());
        plugin.saveUsers();

        // update the player's values
        players.put(player, test.getName());

        // reload the permissions
        plugin.getPlayerManager().register(player);

        // add a metadata tag to the player
        if (ply.getPlayer() != null) {
            ply.getPlayer().setMetadata("group", new FixedMetadataValue(plugin, test.getName()));
        }

        // tell other plugins about the group change
        plugin.getServer().getPluginManager().callEvent(new GroupChangeEvent(ply, orig.getName(), test.getName()));

        // return the new group
        return test;
    }

    /**
     * Gets the specified group by name (case-insensitive)
     * @param group The group's name.
     * @return the group instance, or null
     * @see #getGroup(org.bukkit.OfflinePlayer)
     */
    public Group getGroup(String group) {
        plugin.debug("Searching for group " + group + "...");
        try {
            createGroup(group).getName();
        } catch (NullPointerException e) {
            plugin.debug("No group by the name '" + group + "' was found; returning default...");
            return getDefaultGroup();
        }
        return groupList.get(group.toLowerCase());
    }

    /**
     * Gets the specified player's group
     * @param player The player whose group we're fetching
     * @return The player's group
     * @deprecated since 1.5
     */
    @Deprecated
    public Group getGroup(Player player) {
        return getGroup((OfflinePlayer)player);
    }

    /**
     * Gets the specified player's group
     * @param player The player whose group we're fetching
     * @return the group associated with this player
     */
    public Group getGroup(OfflinePlayer player) {
        try {
            String group = players.get(player.getName());
            if (group == null) {
                group = plugin.getUserNode(player.getName()).getString("group");
            }
            return getGroup(group);
        } catch (Exception e) {
            return getDefaultGroup();
        }
    }

    /**
     * Creates the specified group, by name
     * @param group The name of the group to create
     * @return The new group object, or the default Privileges group if no groups by that name exist
     */
    protected Group createGroup(String group) {
        if (groupList.containsKey(group.toLowerCase())) {
            return groupList.get(group.toLowerCase());
        } else {
            if (plugin.getGroupNode(group) == null) {
                plugin.debug("Group node for '" + group + "' was null.");
                return getDefaultGroup();
            }
            List<String> tree = plugin.getPlayerManager().calculateGroupTree(group);
            Permission perm = new Permission("group." + group);
            perm.setDescription("If true, the attached player is a member of the group: " + group);
            perm.setDefault(PermissionDefault.FALSE);
            if (plugin.getServer().getPluginManager().getPermission(perm.getName()) == null) {
                plugin.getServer().getPluginManager().addPermission(perm);
            }
            Group nGroup = new RankedGroup(plugin, group, plugin.getGroupNode(group).getInt("rank", 1), tree);
            nGroup.addPermission(null, perm.getName());
            groupList.put(group.toLowerCase(), nGroup);
            return nGroup;
        }
    }

    /**
     * Returns a set of groups of which the plugin is currently aware
     * @return The current groups, as a Set
     */
    public Set<Group> getGroups() {
        return new HashSet<Group>(groupList.values());
    }

}
