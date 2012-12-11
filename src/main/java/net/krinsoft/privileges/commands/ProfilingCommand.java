package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class ProfilingCommand extends PrivilegesCommand {

    public ProfilingCommand(Privileges instance) {
        super(instance);
        setName("Privileges: Profiling");
        setCommandUsage("/priv profiling");
        setArgRange(0, 0);
        addKey("privileges profiling");
        addKey("priv profiling");
        setPermission("privileges.profiling", "Shows profiling data for the various events.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        ConfigurationSection events = plugin.getConfig().getConfigurationSection("profiling");
        if (events != null) {
            sender.sendMessage(ChatColor.GREEN + "=== " + ChatColor.AQUA + events.getKeys(false).size() + ChatColor.WHITE + " Events" + ChatColor.GREEN + " ===");
            for (String event : events.getKeys(false)) {
                long time = events.getLong(event);
                sender.sendMessage("Event [" + ChatColor.GREEN + event + ChatColor.WHITE + "]: " + ChatColor.AQUA + (time / 1000000L) + ChatColor.WHITE + "ms (" + ChatColor.GRAY + time + ChatColor.WHITE + "ns)");
            }
        } else {
            sender.sendMessage("No profiling data available.");
        }
    }
}
