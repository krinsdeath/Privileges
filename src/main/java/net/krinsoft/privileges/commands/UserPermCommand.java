package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public abstract class UserPermCommand extends UserCommand {

    public UserPermCommand(Privileges instance) {
        super(instance);
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);

    protected void reload(CommandSender sender) {
        if (plugin.getConfig().getBoolean("auto_reload")) {
            plugin.saveUsers();
            plugin.reload();
        } else {
            sender.sendMessage("When you're done editing permissions, run: " + ChatColor.GREEN + "/priv reload");
        }
    }

}
