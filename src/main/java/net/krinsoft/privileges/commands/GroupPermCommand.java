package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.PermissionManager;
import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;

/**
 *
 * @author krinsdeath
 */
public abstract class GroupPermCommand extends GroupCommand {

    protected PermissionManager permManager;

    public GroupPermCommand(Privileges plugin) {
        super(plugin);
        permManager = plugin.getPermissionManager();
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);
    
    public String[] validateParam(String param) {
        String[] thing = new String[2];
        if (param.contains(":")) {
            try {
                thing[0] = param.split(":")[0];
                thing[1] = param.split(":")[1];
                if (plugin.getServer().getWorld(thing[0]) == null) {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            thing[0] = null;
            thing[1] = param;
        }
        return thing;
    }

}
