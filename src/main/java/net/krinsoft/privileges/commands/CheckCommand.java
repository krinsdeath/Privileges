package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class CheckCommand extends PrivilegesCommand {

    public CheckCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Check");
        setCommandUsage("/pc [player] [node]");
        addCommandExample(ChatColor.GREEN + "/pc" + ChatColor.AQUA + " Njodi privileges.build " + ChatColor.WHITE + "-- Checks Njodi's 'privileges.build' node");
        addCommandExample(ChatColor.GREEN + "/pc" + ChatColor.AQUA + " privileges.reload " + ChatColor.WHITE + "-- Checks your own 'privileges.reload' node");
        setArgRange(1, 2);
        addKey("privileges check");
        addKey("priv check");
        addKey("pcheck");
        addKey("pc");
        setPermission("privileges.check", "Allows this user to use '/perm check'", PermissionDefault.FALSE);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        CommandSender target = sender;
        String node;
        if (args.size() == 2) {
            if (plugin.getServer().getPlayer(args.get(0)) != null) {
                target = plugin.getServer().getPlayer(args.get(0));
            } else {
                String m = "&CThe player &A" + args.get(0) + "&C does not exist.";
                sender.sendMessage(m.replaceAll("(?i)&([0-F])", "\u00A7$1"));
                return;
            }
            node = args.get(1);
        } else {
            node = args.get(0);
        }
        if (!target.equals(sender) && !sender.hasPermission("privileges.check.other")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to view other peoples' nodes.");
            return;
        }
        Permission perm = plugin.getServer().getPluginManager().getPermission(node);
        String name = (target instanceof ConsoleCommandSender) ? "Console" : (sender.equals(target) ? "Your" : target.getName() + "&A's");
        String msg = "&B" + name + "&A node " + "&B" + node + "&A is &B" + target.hasPermission(node) + " ";
        msg = (msg + "&A(" + (target.isPermissionSet(node) ? "&3set" : "&3default") + "&A)").replaceAll("&([0-9A-F])", "\u00A7$1");
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.stripColor(msg));
        } else {
            sender.sendMessage(msg);
        }
        if (perm != null) {
            sender.sendMessage("Description: " + perm.getDescription() != null ? perm.getDescription() : perm.getName()); // We dont need a random var here
        }
    }

}
