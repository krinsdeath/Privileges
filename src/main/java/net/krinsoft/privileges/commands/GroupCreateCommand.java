package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class GroupCreateCommand extends GroupCommand {

    public GroupCreateCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Group Create");
        setCommandUsage("/pg create [name] [rank]");
        addCommandExample("/pgc admin 10 -- creates the 'admin' group at rank 10");
        setArgRange(2, 2);
        addKey("privileges group create");
        addKey("priv group create");
        addKey("pgroup create");
        addKey("pg create");
        addKey("pgroupc");
        addKey("pgc");
        setPermission("privileges.group.create", "Allows the user to create new groups.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        ConfigurationSection groups = plugin.getGroups().getConfigurationSection("groups");
        if (groups.getKeys(false).contains(args.get(0))) {
            // already have a group called that!
            sender.sendMessage(ChatColor.RED + "Groups can have any name you want EXCEPT ones that already exist.");
            return;
        }
        int rank;
        try {
            rank = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            // sender provided an invalid argument
            sender.sendMessage(ChatColor.RED + "You must specify a rank for the new group.");
            return;
        }
        // make sure the rank is not already taken
        if (plugin.getGroupManager().isRankTaken(rank)) {
            sender.sendMessage(ChatColor.RED + "A group with that rank already exists.");
            return;
        }
        // check that the user can create a group of this rank
        if (!plugin.getGroupManager().checkRank(sender, rank)) {
            sender.sendMessage("That rank is too high for you.");
            return;
        }
        // alright, group name is unique and rank isn't used!
        groups.set(args.get(0) + ".rank", rank);
        groups.set(args.get(0) + ".permissions", null);
        groups.set(args.get(0) + ".worlds", null);
        groups.set(args.get(0) + ".inheritance", null);
        plugin.getGroupManager().getGroup(args.get(0));
        sender.sendMessage("The group " + colorize(ChatColor.GREEN, args.get(0)) + " has been created.");
        sender.sendMessage("When you're done editing permissions, run: " + ChatColor.GREEN + "/priv reload");
        plugin.log(">> " + sender.getName() + ": Created group '" + args.get(0) + "'");
    }

}
