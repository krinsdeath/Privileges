package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class DemoteCommand extends GroupCommand {

    public DemoteCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Demote");
        setCommandUsage("/demote [user]");
        setArgRange(1, 1);
        addKey("privileges demote");
        addKey("priv demote");
        addKey("demote");
        addKey("dem");
        setPermission("privileges.demote", "Allows the sender to demote other users.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args.get(0));
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "The target '" + ChatColor.DARK_RED + args.get(0) + ChatColor.RED + "' doesn't exist.");
            return;
        }
        plugin.getGroupManager().demote(sender, target);
    }
}
