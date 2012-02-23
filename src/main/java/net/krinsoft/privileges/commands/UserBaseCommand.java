package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class UserBaseCommand extends UserCommand {

    public UserBaseCommand(Privileges instance) {
        super(instance);
        setName("Privileges: User");
        setCommandUsage("/priv user");
        addCommandExample(ChatColor.GREEN + "/pu" + ChatColor.AQUA + " perm  " + ChatColor.RED  + "set     " + ChatColor.GOLD + "[world:]node [val]");
        addCommandExample(ChatColor.GREEN + "/pu" + ChatColor.AQUA + " perm  " + ChatColor.RED  + "remove  " + ChatColor.GOLD + "[world:]node [val]");
        addCommandExample(ChatColor.GREEN + "/pu" + ChatColor.AQUA + " list  " + ChatColor.GOLD + "[group] ");
        addCommandExample(ChatColor.GREEN + "/pu" + ChatColor.AQUA + " reset " + ChatColor.GOLD + "[user]  ");
        setArgRange(0, 0);
        addKey("privileges user");
        addKey("priv user");
        addKey("puser");
        addKey("pu");
        setPermission("privileges.user", "The following commands are all used to edit or view user information.", PermissionDefault.TRUE);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        showHelp(sender);
    }
}
