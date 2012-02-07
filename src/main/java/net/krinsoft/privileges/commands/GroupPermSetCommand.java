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
        this.setName("Privileges: Group Perm Set");
        this.setCommandUsage("/privileges group perm set [group] [world:]node [val]");
        this.addCommandExample("/pgps user privileges.version true -- sets 'privileges.version' to true for the group 'user'");
        this.addCommandExample("/pgps user world:example.node false -- sets 'example.node' to false for 'user' on the world 'world'");
        this.setArgRange(2, 3);
        this.addKey("privileges group perm set");
        this.addKey("priv group perm set");
        this.addKey("pg perm set");
        this.addKey("pgp set");
        this.addKey("pgps");
        this.setPermission("privileges.group.perm.set", "Allows this user to set permission nodes.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Group group = groupManager.getGroup(args.get(0));
        if (group == null) {
            sender.sendMessage(ChatColor.RED + "That group does not exist.");
            return;
        }
        boolean val;
        try {
            val = Boolean.parseBoolean(args.get(2));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Value must be a boolean, true or false.");
            return;
        }
        if (!groupManager.checkRank(sender, group.getRank())) {
            sender.sendMessage(ChatColor.RED + "That group's rank is too high.");
            return;
        }
        String[] param = validateParam(args.get(1));
        if (param == null) {
            sender.sendMessage(ChatColor.RED + "Invalid node string.");
            return;
        }
        String world = param[0];
        String node = param[1];
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
        plugin.saveGroups();
        sender.sendMessage("Node '" + colorize(ChatColor.GREEN, node) + "' is now " + (val ? ChatColor.GREEN : ChatColor.RED) + val + ChatColor.WHITE + " for " + group.getName());
        sender.sendMessage("When you're done editing permissions, run: " + ChatColor.GREEN + "/priv reload");
        plugin.log(sender.getName() + " set " + node + " to " + val + " for '" + group.getName() + "'");
    }

}
