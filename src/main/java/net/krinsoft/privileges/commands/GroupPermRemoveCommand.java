package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class GroupPermRemoveCommand extends GroupPermCommand {

    public GroupPermRemoveCommand(Privileges plugin) {
        super(plugin);
        this.setName("Privileges: Group Perm Remove");
        this.setCommandUsage("/privileges group perm remove [group] [world:]node");
        this.addCommandExample("/priv group perm remove user privileges.build");
        this.addCommandExample("/pgpr user world_nether:privileges.interact");
        this.setArgRange(2, 2);
        this.addKey("privileges group perm remove");
        this.addKey("priv group perm remove");
        this.addKey("pg perm remove");
        this.addKey("pgp remove");
        this.addKey("pgpr");
        this.setPermission("privileges.group.perm.remove", "Allows this user to remove permissions nodes.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Group group = groupManager.getGroup(args.get(0));
        if (group == null) {
            sender.sendMessage(ChatColor.RED + "That group does not exist.");
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
        if (node.startsWith("-")) { node = node.substring(1); }
        List<String> nodes;
        if (world == null) {
            nodes = plugin.getGroupNode(group.getName()).getStringList("permissions");
            nodes.remove(node);
            nodes.remove("-" + node);
            plugin.getGroupNode(group.getName()).set("permissions", nodes);
        } else {
            nodes = plugin.getGroupNode(group.getName()).getStringList("worlds." + world);
            nodes.remove(node);
            nodes.remove("-" + node);
            plugin.getGroupNode(group.getName()).set("worlds." + world, nodes);
        }
        plugin.saveGroups();
        sender.sendMessage("Node '" + colorize(ChatColor.GREEN, node) + "' has been removed from the group " + group.getName());
        sender.sendMessage("When you're done editing permissions, run: " + ChatColor.GREEN + "/priv reload");
        plugin.log(sender.getName() + " removed the node '" + node + "' from the group '" + group.getName() + "'");
    }

}
