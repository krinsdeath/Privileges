package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.Map;

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
        addCommandExample(ChatColor.GREEN + "/pc" + ChatColor.AQUA + " privileges.interact " + ChatColor.GOLD + " -v " + ChatColor.WHITE + "-- Shows a verbose listing of the node and all child nodes");
        setArgRange(1, 3);
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
        if (args.size() >= 2) {
            if (plugin.getServer().getPlayer(args.get(0)) != null) {
                target = plugin.getServer().getPlayer(args.get(0));
            } else {
                String m = "&CThe player &A" + args.get(0) + "&C does not exist.";
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
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
        msg = msg + "&A(" + (target.isPermissionSet(node) ? "&3set" : "&3default") + "&A)";
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.stripColor(msg));
        } else {
            sender.sendMessage(msg);
        }
        if (perm != null) {
            String desc = (perm.getDescription() != null ? perm.getDescription() : "No description defined.");
            sender.sendMessage("Description: " + desc);
            if (args.size() == 3 && args.get(2).equalsIgnoreCase("-v") && perm.getChildren().size() > 0) {
                sender.sendMessage("=== Child Nodes ===");
                for (Map.Entry<String, Boolean> entry : perm.getChildren().entrySet()) {
                    sender.sendMessage(entry.getKey() + " - " + entry.getValue());
                }
            }
        }
    }

}
