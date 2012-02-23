package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class BaseCommand extends PrivilegesCommand {

    public BaseCommand(Privileges instance) {
        super(instance);
        setName("Privileges: Main");
        setCommandUsage("/priv");
        addCommandExample(ChatColor.GREEN + "/priv" + ChatColor.AQUA + " group   " + ChatColor.RED  + "?");
        addCommandExample(ChatColor.GREEN + "/priv" + ChatColor.AQUA + " user    " + ChatColor.RED  + "?");
        addCommandExample(ChatColor.GREEN + "/priv" + ChatColor.AQUA + " list    " + ChatColor.GOLD + "[player] [page]");
        addCommandExample(ChatColor.GREEN + "/priv" + ChatColor.AQUA + " check   " + ChatColor.GOLD + "[player] [node]");
        addCommandExample(ChatColor.GREEN + "/priv" + ChatColor.AQUA + " version ");
        addCommandExample(ChatColor.GREEN + "/priv" + ChatColor.AQUA + " reload  ");
        addCommandExample(ChatColor.GREEN + "/priv" + ChatColor.AQUA + " save    ");
        addCommandExample(ChatColor.GREEN + "/priv" + ChatColor.AQUA + " load    ");
        addCommandExample(ChatColor.GREEN + "/priv" + ChatColor.AQUA + " backup  " + ChatColor.GOLD + "[name]");
        addCommandExample(ChatColor.GREEN + "/priv" + ChatColor.AQUA + " restore " + ChatColor.GOLD + "[name]");
        setArgRange(0, 1);
        addKey("privileges");
        addKey("priv");
        setPermission("privileges", "Welcome to Privileges' help system! Try a command to see usage examples.", PermissionDefault.TRUE);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        showHelp(sender);
    }
}
