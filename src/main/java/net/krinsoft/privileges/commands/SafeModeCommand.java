package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class SafeModeCommand extends PrivilegesCommand {

    public SafeModeCommand(Privileges instance) {
        super(instance);
        plugin = instance;
        setName("Privileges: Safe Mode");
        setCommandUsage("/priv safemode");
        setArgRange(0, 0);
        addKey("privileges safemode");
        addKey("priv safemode");
        addKey("psm");
        setPermission("privileges.safemode", "Turns safe mode on.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {

    }
}
