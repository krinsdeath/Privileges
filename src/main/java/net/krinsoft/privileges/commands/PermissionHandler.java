package net.krinsoft.privileges.commands;

import com.pneumaticraft.commandhandler.PermissionsInterface;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class PermissionHandler implements PermissionsInterface {

    public boolean hasPermission(CommandSender sender, String node, boolean isOpRequired) {
        return sender instanceof ConsoleCommandSender || sender  instanceof RemoteConsoleCommandSender || sender.hasPermission(node);
    }

    public boolean hasAnyPermission(CommandSender sender, List<String> allPermissionStrings, boolean opRequired) {
        for (String node : allPermissionStrings) {
            if (hasPermission(sender, node, opRequired)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllPermission(CommandSender sender, List<String> allPermissionStrings, boolean opRequired) {
        for (String node : allPermissionStrings) {
            if (!hasPermission(sender, node, opRequired)) {
                return false;
            }
        }
        return true;
    }

}