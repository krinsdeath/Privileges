package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class DebugCommand extends PrivilegesCommand {

    public DebugCommand(Privileges plugin) {
        super(plugin);
        this.setName("Privileges: Debug");
        this.setCommandUsage("/privileges debug [val]");
        this.addCommandExample("/priv debug -- flip the current debug setting");
        this.addCommandExample("/priv debug true -- turn debug mode on");
        this.setArgRange(0, 1);
        this.addKey("privileges debug");
        this.addKey("priv debug");
        this.setPermission("privileges.debug", "Allows this user to toggle debug mode.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            plugin.toggleDebug("--flip");
        } else {
            plugin.toggleDebug(args.get(0));
        }
    }

}
