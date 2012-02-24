package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class LoadCommand extends PrivilegesCommand {

    public LoadCommand(Privileges instance) {
        super(instance);
        setName("Privileges: Load");
        setCommandUsage("/priv load");
        setArgRange(0, 0);
        addKey("privileges load");
        addKey("priv load");
        addKey("pload");
        setPermission("privileges.load", "Immediately loads Privileges currently saved configuration, without saving any changes to disk beforehand.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        long t = System.currentTimeMillis();
        plugin.registerConfiguration(true);
        plugin.registerPermissions();
        t = System.currentTimeMillis() - t;
        sender.sendMessage(ChatColor.GREEN + "Privileges was loaded successfully. (" + t + "ms)");
        plugin.log(">> " + sender.getName() + ": Privileges was loaded successfully. (" + t + "ms)");
    }

}
