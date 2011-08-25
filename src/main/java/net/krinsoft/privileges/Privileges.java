package net.krinsoft.privileges;

import com.pneumaticraft.commandhandler.CommandHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import net.krinsoft.privileges.commands.ListCommand;
import net.krinsoft.privileges.commands.ReloadCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */
public class Privileges extends JavaPlugin {

    private final static Logger LOGGER = Logger.getLogger("Privileges");
    private boolean debug = false;
    private PermissionManager pm;
    private CommandHandler commandHandler;

    @Override
    public void onEnable() {
        registerConfiguration();
        registerPermissions();
        registerEvents();
        registerCommands();
        LOGGER.info(this + " is now enabled.");
    }

    @Override
    public void onDisable() {
        this.pm.disable();
        LOGGER.info(this + " is now disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> allArgs = null;
        allArgs.addAll(Arrays.asList(args));
        allArgs.add(0, label);
        return commandHandler.locateAndRunCommand(sender, allArgs);
    }

    public void registerPermissions() {
        this.pm = null;
        this.pm = new PermissionManager(this);
    }

    public PermissionManager getPermissionManager() {
        return this.pm;
    }

    private void registerConfiguration() {
        Configuration c = getConfiguration();
        if (c.getProperty("default_group") == null) {
            c.setHeader(
                    "# Group ranks determine the order they are promoted in.",
                    "# Lowest rank is 1, highest rank is 2,147,483,647.",
                    "# Visit https://github.com/krinsdeath/Privileges/wiki for help with configuration");
            c.setProperty("default_group", "default");
            c.setProperty("debug", false);
            c.setProperty("groups.default.rank", 1);
            c.setProperty("groups.default.permissions", Arrays.asList("-privileges.build", "-privileges.interact"));
            c.setProperty("groups.default.worlds.world", Arrays.asList("-example.basic.node2"));
            c.setProperty("groups.default.worlds.world_nether", Arrays.asList("-example.basic.node1"));
            c.setProperty("groups.default.inheritance", new ArrayList<String>());
            c.setProperty("groups.user.rank", 2);
            c.setProperty("groups.user.permissions", Arrays.asList("privileges.build", "privileges.check"));
            c.setProperty("groups.user.inheritance", Arrays.asList("default"));
            c.setProperty("groups.admin.rank", 3);
            c.setProperty("groups.admin.permissions", Arrays.asList("privileges.promote"));
            c.setProperty("groups.admin.inheritance", Arrays.asList("user"));
            c.save();
        }
        debug = c.getBoolean("debug", false);
    }

    private void registerEvents() {
        PluginManager manager = this.getServer().getPluginManager();
        PlayerListener pListener = new PlayerListener(this);
        BlockListener bListener = new BlockListener(this);
        manager.registerEvent(Type.PLAYER_JOIN, pListener, Priority.Lowest, this);
        manager.registerEvent(Type.PLAYER_QUIT, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_KICK, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_TELEPORT, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_PORTAL, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_INTERACT, pListener, Priority.Lowest, this);
        manager.registerEvent(Type.BLOCK_PLACE, bListener, Priority.Lowest, this);
        manager.registerEvent(Type.BLOCK_BREAK, bListener, Priority.Lowest, this);
    }

    private void registerCommands() {
        commandHandler = new CommandHandler(this, new PermissionHandler(this));
        commandHandler.registerCommand(new ReloadCommand(this));
        commandHandler.registerCommand(new ListCommand(this));
        //commandHandler.registerCommand(new CheckCommand(this));
        //commandHandler.registerCommand(new PromoteCommand(this));
    }

    protected ConfigurationNode getUserNode(String player) {
        if (getConfiguration().getNode("users." + player) == null) {
            Configuration c = getConfiguration();
            String path = "users." + player;
            c.setProperty(path + ".permissions", null);
            c.setProperty(path + ".groups", Arrays.asList(c.getString("default_group", "default")));
            c.save();
            debug("Empty user node for '" + player + "' created.");
        }
        return getConfiguration().getNode("users." + player);
    }

    protected ConfigurationNode getGroupNode(String group) {
        if (getConfiguration().getNode("groups." + group) == null) {
            debug("Empty group node detected.");
            return null;
        }
        return getConfiguration().getNode("groups." + group);
    }

    protected void debug(String message) {
        if (debug) {
            message = "[" + this + "] [Debug] " + message;
            LOGGER.info(message);
        }
    }

}
