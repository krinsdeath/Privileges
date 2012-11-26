package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class GroupPermSetCommand extends GroupPermCommand {

    public GroupPermSetCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Group Perm Set");
        setCommandUsage("/pg perm set [group] [world:]node [val]");
        addCommandExample("/pgps user privileges.version true -- sets 'privileges.version' to true for the group 'user'");
        addCommandExample("/pgps admin world:example.node false -- sets 'example.node' to false for 'user' on the world 'world'");
        setArgRange(2, 3);
        addKey("privileges group perm set");
        addKey("priv group perm set");
        addKey("pgroup perm set");
        addKey("pg perm set");
        addKey("pgp set");
        addKey("pgps");
        setPermission("privileges.group.perm.set", "Allows this user to set permission nodes.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Group group = plugin.getGroupManager().getGroup(args.get(0));
        if (group == null) {
            sender.sendMessage(ChatColor.RED + "That group does not exist.");
            return;
        }
        boolean val = true;
        if (args.size() > 2) {
            try {
                val = Boolean.parseBoolean(args.get(2));
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Value must be a boolean, true or false.");
                return;
            }
        }
        if (!plugin.getGroupManager().checkRank(sender, group.getRank())) {
            sender.sendMessage(ChatColor.RED + "That group's rank is too high.");
            return;
        }
        String[] param = validateNode(args.get(1));
        if (param == null) {
            sender.sendMessage(ChatColor.RED + "Invalid node string.");
            return;
        }
        String node = param[0];
        String world = param[1];
        if (node.equalsIgnoreCase("privileges.self.edit") && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Only the console can set that node.");
            return;
        }
        if (world == null) {
            List<String> nodes = plugin.getGroupNode(group.getName()).getStringList("permissions");
            nodes.remove(node);
            nodes.remove("-" + node);
            nodes.add((val ? "" : "-") + node);
            plugin.getGroupNode(group.getName()).set("permissions", nodes);
        } else {
            List<String> nodes = plugin.getGroupNode(group.getName()).getStringList("worlds." + world);
            nodes.remove(node);
            nodes.remove("-" + node);
            nodes.add((val ? "" : "-") + node);
            plugin.getGroupNode(group.getName()).set("worlds." + world, nodes);
        }
        sender.sendMessage("Node '" + colorize(ChatColor.GREEN, node) + "' is now " + (val ? ChatColor.GREEN : ChatColor.RED) + val + ChatColor.WHITE + " for " + group.getName());
        plugin.log(">> " + sender.getName() + ": " + group.getName() + "'s node '" + node + "' is now '" + val + "'");
        reload(sender);
    }

}
