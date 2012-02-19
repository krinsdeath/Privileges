package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
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
        this.setName("Privileges: Group Set");
        this.setCommandUsage("/privileges group set [player] [group]");
        this.setArgRange(2, 2);
        this.addKey("privileges group set");
        this.addKey("priv group set");
        this.addKey("pg set");
        this.addKey("pgs");
        this.setPermission("privileges.group.set", "Allows this user to change other users' groups.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        CommandSender target = plugin.getServer().getPlayer(args.get(0));
        if (target == null) { return; }
        if (!groupManager.checkRank(sender, target)) {
            sender.sendMessage(ChatColor.RED + "That user's rank is too high.");
            return;
        }
        if (!groupManager.checkRank(sender, groupManager.getGroup(args.get(1)).getRank())) {
            sender.sendMessage(ChatColor.RED + "That group's rank is too high.");
            return;
        }
        groupManager.setGroup(target.getName(), args.get(1));
        sender.sendMessage(colorize(ChatColor.GREEN, target.getName()) + "'s group has been set to " + colorize(ChatColor.GREEN, args.get(1)));
        plugin.log(">> " + sender.getName() + ": Set " + target.getName() + "'s group to '" + args.get(1) + "'");
    }

}
