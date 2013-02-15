package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

/**
 * @author krinsdeath
 */
public class InfoCommand extends PrivilegesCommand {

    public InfoCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Info");
        setCommandUsage("/priv info [PLAYER]");
        setArgRange(0, 1);
        addKey("privileges info");
        addKey("priv info");
        addKey("pinfo");
        addKey("pi");
        setPermission("privileges.info", "Allows the user to check information about themselves or others.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        List<String> lines = new ArrayList<String>();
        CommandSender target = sender;
        if (args.size() == 1) {
            target = plugin.getServer().getPlayer(args.get(0));
            if (target == null) {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(ChatColor.RED + "Target must exist from Console.");
                    return;
                }
                target = sender;
            }
        } else {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(ChatColor.RED + "Target must exist from Console.");
                return;
            }
        }
        if (!target.equals(sender) && !sender.hasPermission("privileges.info.other")) {
            sender.sendMessage(ChatColor.RED + "You cannot view other peoples' information.");
            return;
        }
        lines.add("=== User Info: " + ChatColor.BLUE + target.getName() + ChatColor.WHITE + " ===");
        lines.add("Is " + ChatColor.AQUA + target.getName() + ChatColor.WHITE + " an op? " + ChatColor.GREEN + (target.isOp() ? "Yes." : "No."));
        if (!target.getName().equals(((Player)target).getDisplayName())) {
            lines.add(ChatColor.AQUA + target.getName() + ChatColor.WHITE + " is currently known as '" + ChatColor.AQUA + ((Player)target).getDisplayName() + ChatColor.WHITE + "'");
        }
        lines.add(ChatColor.AQUA + target.getName() + ChatColor.WHITE + "'s group is: " + ChatColor.GREEN + plugin.getGroupManager().getGroup((OfflinePlayer)target).getName());
        lines.add(ChatColor.AQUA + target.getName() + ChatColor.WHITE + "'s current world is: " + ChatColor.GREEN + ((Player)target).getWorld().getName() + ChatColor.WHITE + ".");
        for (String line : lines) {
            sender.sendMessage(line);
        }
    }
}
