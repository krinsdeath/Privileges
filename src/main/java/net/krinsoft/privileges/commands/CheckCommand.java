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
        this.setName("Privileges: Check");
        this.setCommandUsage("/privileges check [player] [node]");
        this.addCommandExample("/priv check Player privileges.build -- Checks Player's 'privileges.build' node");
        this.addCommandExample("/priv check privileges.reload -- Checks your own 'privileges.reload' node");
        this.setArgRange(1, 2);
        this.addKey("privileges check");
        this.addKey("priv check");
        this.addKey("pc");
        this.setPermission("privileges.check", "Allows this user to use '/perm check'", PermissionDefault.FALSE);
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
        Permission perm = plugin.getServer().getPluginManager().getPermission(node);
        if (perm == null) {
            sender.sendMessage(ChatColor.RED + "That node doesn't exist.");
            return;
        }
        String name = (target instanceof ConsoleCommandSender) ? "Console" : (sender.equals(target) ? "Your" : target.getName() + "&A's");
        String msg = "&B" + name + "&A node " + "&B" + node + "&A is &B" + target.hasPermission(node) + " ";
        msg = msg + "&A(" + (target.isPermissionSet(node) ? "&3set" : "&3default") + "&A)";
        msg = msg.replaceAll("&([0-9A-F])", "\u00A7$1");
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.stripColor(msg));
        } else {
            sender.sendMessage(msg);
        }
        String desc = (perm.getDescription() != null ? perm.getDescription() : perm.getName());
        sender.sendMessage("Description: " + desc);
    }

}
