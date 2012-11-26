package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author krinsdeath
 */
public abstract class GroupCommand extends PrivilegesCommand {

    public GroupCommand(Privileges plugin) {
        super(plugin);
    }

    protected void reload(CommandSender sender) {
        if (plugin.getConfig().getBoolean("auto_reload")) {
            plugin.saveGroups();
            plugin.reload();
        } else {
            sender.sendMessage("When you're done editing permissions, run: " + ChatColor.GREEN + "/priv reload");
        }
    }
}
