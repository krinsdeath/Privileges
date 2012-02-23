package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class DebugCommand extends PrivilegesCommand {

    public DebugCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Debug");
        setCommandUsage("/privileges debug [val]");
        addCommandExample(ChatColor.GREEN + "/priv debug" + ChatColor.WHITE + " -- flip the current debug setting");
        addCommandExample(ChatColor.GREEN + "/priv debug" + ChatColor.AQUA + " true" + ChatColor.WHITE + " -- turn debug mode on");
        setArgRange(0, 1);
        addKey("privileges debug");
        addKey("priv debug");
        addKey("pdebug");
        setPermission("privileges.debug", "Allows this user to toggle debug mode.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        try {
            if (args.isEmpty()) {
                plugin.toggleDebug();
            } else {
                plugin.toggleDebug(Boolean.parseBoolean(args.get(0)));
            }
        } catch (Exception e) {
            plugin.debug("Invalid argument.");
        }
    }

}
