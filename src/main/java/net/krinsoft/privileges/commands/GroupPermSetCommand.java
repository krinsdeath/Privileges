package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class GroupPermSetCommand extends GroupPermCommand {

    public GroupPermSetCommand(Privileges plugin) {
        super(plugin);
        this.setName("Privileges Group Perm Set");
        this.setCommandUsage("/privileges group perm set [group] [world:]node [val]");
        this.addCommandExample("/pgps user privileges.version true -- sets 'privileges.version' to true for the group 'user'");
        this.addCommandExample("/pgps user world:example.node false -- sets 'example.node' to false for 'user' on the world 'world'");
        this.setArgRange(3, 3);
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
        String world = null;
        String node = args.get(1);
        boolean val;
        try {
            val = Boolean.parseBoolean(args.get(2));
        } catch (NumberFormatException e) {
            sender.sendMessage("Value must be a boolean, true or false.");
            return;
        }
        if (group == null) {
            sender.sendMessage("That group does not exist.");
            return;
        }
        if (group.getRank() >= groupManager.getRank(sender)) {
            sender.sendMessage("That group's rank is too high.");
            return;
        }
        if (args.get(1).contains(":")) {
            try {
                world = args.get(1).split(":")[0];
                node = args.get(1).split(":")[1];
                if (plugin.getServer().getWorld(world) == null) {
                    sender.sendMessage("Unknown world.");
                    return;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                sender.sendMessage("Invalid node string.");
                return;
            }
        }
        if (world == null) {
            List<String> nodes = plugin.getGroupNode(group.getName()).getStringList("permissions", null);
            nodes.remove(node);
            nodes.remove("-" + node);
            nodes.add((val ? "" : "-") + node);
            plugin.getGroupNode(group.getName()).setProperty("permissions", nodes);
        } else {
            List<String> nodes = plugin.getGroupNode(group.getName()).getStringList("worlds." + world, null);
            nodes.remove(node);
            nodes.remove("-" + node);
            nodes.add((val ? "" : "-") + node);
            plugin.getGroupNode(group.getName()).setProperty("worlds." + world, nodes);
        }
        plugin.getGroups().save();
        sender.sendMessage("Node '" + ChatColor.GREEN + node + ChatColor.WHITE + "' is now " + (val ? ChatColor.GREEN : ChatColor.RED) + val + ChatColor.WHITE + " for " + group.getName());
        permManager.reload();
    }

}
