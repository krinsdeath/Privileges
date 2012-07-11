package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
        long time = System.nanoTime();
        plugin.reload();
        time = System.nanoTime() - time;
        if (!(sender instanceof ConsoleCommandSender)) {
            plugin.log(">> " + sender.getName() + ": Privileges was loaded successfully.");
        }
        sender.sendMessage(ChatColor.GREEN + "Privileges was loaded successfully.");
        plugin.profile(time, "load");
    }

}
