package net.krinsoft.privileges;

import com.pneumaticraft.commandhandler.CommandHandler;
import net.krinsoft.privileges.commands.*;
import net.krinsoft.privileges.groups.GroupManager;
import net.krinsoft.privileges.importer.ImportManager;
import net.krinsoft.privileges.listeners.BlockListener;
import net.krinsoft.privileges.listeners.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class Privileges extends JavaPlugin {

    private boolean debug = false;
    private boolean profile = false;

    // managers and handlers
    private PermissionManager permissionManager;
    private GroupManager groupManager;
    private CommandHandler commandHandler;
    private FileConfiguration   configuration;
    private File                configFile;
    private FileConfiguration   users;
    private File                userFile;
    private FileConfiguration   groups;
    private File                groupFile;

    @Override
    public void onEnable() {
        long time = System.nanoTime();
        registerConfiguration();
        performImports();
        registerPermissions();
        getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                //boolean tmp = debug;
                //debug = false;
                registerPermissions();
                updatePermissions();
                //debug = tmp;
            }
        }, 5L);
        registerEvents();
        registerCommands();
        getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                log("Removing old users from users.yml...");
                long timeout = 1000L * 60L * 60L * 24L * 30L;
                for (OfflinePlayer player : getServer().getOfflinePlayers()) {
                    if (System.currentTimeMillis() - player.getLastPlayed() >= timeout) {
                        if (getUsers().get(player.getName()) != null && !getUsers().getString(player.getName() + ".group").equals(getConfig().getString("default_group", "default"))) {
                            getUsers().set(player.getName(), null);
                            debug("'" + player.getName() + "' removed from users.yml");
                        }
                    }
                }
                saveUsers();
                log("... done!");
            }
        }, 1L);
        try {
            getServer().getPluginManager().getPermission("privileges.*").setDefault(PermissionDefault.OP);
        } catch (NullPointerException e) {
            debug("Error setting default permission for 'privileges.*'");
        }

        try {
            // initialize the plugin metrics tracker
            Metrics metrics = new Metrics();

            // track the number of groups
            metrics.addCustomData(this, new Metrics.Plotter() {
                @Override
                public String getColumnName() {
                    return "Groups";
                }

                @Override
                public int getValue() {
                    return getGroups().getConfigurationSection("groups").getKeys(false).size();
                }
            });

            // track the number of users
            metrics.addCustomData(this, new Metrics.Plotter() {
                @Override
                public String getColumnName() {
                    return "Users";
                }

                @Override
                public int getValue() {
                    return getUsers().getConfigurationSection("users").getKeys(false).size();
                }
            });

            metrics.beginMeasuringPlugin(this);
        } catch (IOException e) {
            log("An error occurred while posting results to the Metrics.");
            warn(e.getLocalizedMessage());
        }
        time = System.nanoTime() - time;
        profile("Startup took: " + time + "ns (" + (time / 1000000L) + "ms)");
    }

    @Override
    public void onDisable() {
        permissionManager.disable();
        log("Is now disabled.");
    }

    @Override
    public FileConfiguration getConfig() {
        if (configuration == null) {
            configuration = YamlConfiguration.loadConfiguration(configFile);
        }
        return configuration;
    }

    @Override
    public void saveConfig() {
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> allArgs = new ArrayList<String>();
        allArgs.addAll(Arrays.asList(args));
        allArgs.add(0, label);
        return commandHandler.locateAndRunCommand(sender, allArgs);
    }

    public void registerPermissions() {
        permissionManager = new PermissionManager(this);
        groupManager = new GroupManager(this);
        registerDynamicPermissions();
    }

    private void registerDynamicPermissions() {
        Permission root = new Permission("privileges.*");
        if (getServer().getPluginManager().getPermission(root.getName()) == null) {
            getServer().getPluginManager().addPermission(root);
        }
        root.getChildren().put("privileges.admins", true);
        root.recalculatePermissibles();
    }

    public void updatePermissions() {
        groupManager.reload();
        permissionManager.reload();
    }

    public void registerConfiguration(boolean val) {
        if (val) {
            configuration = null;
            users = null;
            groups = null;
            registerConfiguration();
        }
    }
    
    public void registerConfiguration() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getConfig().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/config.yml")));
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        userFile = new File(getDataFolder(), "users.yml");
        if (!userFile.exists()) {
            getUsers().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/users.yml")));
            getUsers().options().copyDefaults(true);
            saveUsers();
        }

        groupFile = new File(getDataFolder(), "groups.yml");
        if (!groupFile.exists()) {
            getGroups().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/groups.yml")));
            getGroups().options().header(
                    "Group ranks determine the order they are promoted in.\n" +
                            "Lowest rank is 1, highest rank is 2,147,483,647.\n" +
                            "Visit https://github.com/krinsdeath/Privileges/wiki for help with configuration\n" +
                            "World nodes override global nodes for that group\n" +
                            "Inherited groups are calculated first. Each group in the tree overrides any nodes\n" +
                            "from the previous group.");
            getGroups().options().copyDefaults(true);
            saveGroups();
        }

        if (getConfig().get("default_group") == null) {
            getConfig().set("default_group", "default");
            getConfig().set("debug", false);
            getConfig().set("profiler", false);
            saveConfig();
        }
        debug = getConfig().getBoolean("debug", false);
        profile = getConfig().getBoolean("profiler", false);
    }

    private void performImports() {
        // broken until I can improve it
        new ImportManager(this);
    }

    private void registerEvents() {
        PluginManager manager = this.getServer().getPluginManager();
        PlayerListener pListener = new PlayerListener(this);
        BlockListener bListener = new BlockListener(this);
        manager.registerEvents(pListener, this);
        manager.registerEvents(bListener, this);
    }

    private void registerCommands() {
        PermissionHandler permissionHandler = new PermissionHandler();
        commandHandler = new CommandHandler(this, permissionHandler);
        // base & informational
        commandHandler.registerCommand(new BaseCommand(this));
        commandHandler.registerCommand(new GroupBaseCommand(this));
        commandHandler.registerCommand(new GroupPermBaseCommand(this));
        commandHandler.registerCommand(new UserBaseCommand(this));
        // miscellaneous commands
        commandHandler.registerCommand(new BackupCommand(this));
        commandHandler.registerCommand(new CheckCommand(this));
        commandHandler.registerCommand(new DebugCommand(this));
        commandHandler.registerCommand(new DemoteCommand(this));
        commandHandler.registerCommand(new InfoCommand(this));
        commandHandler.registerCommand(new ListCommand(this));
        commandHandler.registerCommand(new LoadCommand(this));
        commandHandler.registerCommand(new PromoteCommand(this));
        commandHandler.registerCommand(new ReloadCommand(this));
        commandHandler.registerCommand(new RestoreCommand(this));
        commandHandler.registerCommand(new SaveCommand(this));
        commandHandler.registerCommand(new VersionCommand(this));
        // group related commands
        commandHandler.registerCommand(new GroupCreateCommand(this));
        commandHandler.registerCommand(new GroupRemoveCommand(this));
        commandHandler.registerCommand(new GroupRenameCommand(this));
        commandHandler.registerCommand(new GroupSetCommand(this));
        commandHandler.registerCommand(new GroupListCommand(this));
        commandHandler.registerCommand(new GroupPermSetCommand(this));
        commandHandler.registerCommand(new GroupPermRemoveCommand(this));
        // user related commands
        commandHandler.registerCommand(new UserListCommand(this));
        commandHandler.registerCommand(new UserPermSetCommand(this));
        commandHandler.registerCommand(new UserPermRemoveCommand(this));
        commandHandler.registerCommand(new UserResetCommand(this));
    }

    public ConfigurationSection getUserNode(String player) {
        if (getUsers().getConfigurationSection("users." + player) == null || getUsers().getString("users." + player + ".group") == null) {
            String path = "users." + player;
            getUsers().set(path + ".group", getConfig().getString("default_group", "default"));
            getUsers().set(path + ".permissions", null);
            for (World w : getServer().getWorlds()) {
                getUsers().set(path + ".worlds." + w.getName(), null);
            }
            saveUsers();
            debug("New user node for '" + player + "' created with default group '" + getConfig().getString("default_group", "default") + "'.");
        }
        return getUsers().getConfigurationSection("users." + player);
    }

    public ConfigurationSection getGroupNode(String group) {
        if (getGroups().getConfigurationSection("groups." + group) == null) {
            debug("Empty group node '" + group + "' detected.");
            return null;
        }
        return getGroups().getConfigurationSection("groups." + group);
    }

    public void buildGroup(String group) {
        if (getGroups().getConfigurationSection("groups." + group) == null) {
            getGroups().set("groups." + group + ".permissions", null);
            getGroups().set("groups." + group + ".worlds", null);
            getGroups().set("groups." + group + ".inheritance", null);
            saveGroups();
        }
    }

    public FileConfiguration getUsers() {
        if (users == null) {
            users = YamlConfiguration.loadConfiguration(userFile);
        }
        return users;
    }

    public void saveUsers() {
        try {
            users.save(userFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getGroups() {
        if (groups == null) {
            groups = YamlConfiguration.loadConfiguration(groupFile);
        }
        return groups;
    }

    public void saveGroups() {
        try {
            groups.save(groupFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String message) {
        getLogger().info(message);
    }
    
    public void warn(String message) {
        getLogger().warning(message);
    }
    
    public void debug(String message) {
        if (debug) {
            message = "[Debug] " + message;
            getLogger().info(message);
        }
    }

    public void profile(String message) {
        if (profile) {
            message = "[Profiler] " + message;
            getLogger().info(message);
        }
    }

    public void toggleDebug(boolean val) {
        debug = !val;
        toggleDebug();
    }
    
    public void toggleDebug() {
        debug = !debug;
        getConfig().set("debug", debug);
        saveConfig();
        log("Debug mode is now " + (debug ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.WHITE + ".");
    }

    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    public GroupManager getGroupManager() {
        return this.groupManager;
    }

}
