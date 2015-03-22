package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class GroupSetCommand extends GroupCommand {

    public GroupSetCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Group Set");
        setCommandUsage("/pgs [player] [group]");
        setArgRange(2, 2);
        addKey("privileges group set");
        addKey("priv group set");
        addKey("pgroup set");
        addKey("pgroups");
        addKey("pg set");
        addKey("pgs");
        setPermission("privileges.group.set", "Allows this user to change other users' groups.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args.get(0));
        if (target == null) {
            sender.sendMessage("No player with the name '" + args.get(0) + "' could be found.");
            return;
        }
        if (!plugin.getGroupManager().checkRank(sender, target)) {
            sender.sendMessage(ChatColor.RED + "That user's rank is too high.");
            return;
        }
        try {
            if (!plugin.getGroupManager().checkRank(sender, plugin.getGroupManager().getGroup(args.get(1)).getRank())) {
                sender.sendMessage(ChatColor.RED + "That group's rank is too high.");
                return;
            }
        } catch (NullPointerException e) {
            sender.sendMessage(ChatColor.RED + "No such group exists.");
            return;
        }
        plugin.getGroupManager().setGroup(target.getUniqueId(), args.get(1));
        sender.sendMessage(colorize(ChatColor.GREEN, target.getName()) + "'s group has been set to " + colorize(ChatColor.GREEN, args.get(1)));
        plugin.log(">> " + sender.getName() + ": Set " + target.getName() + "'s group to '" + args.get(1) + "'");
    }

}
