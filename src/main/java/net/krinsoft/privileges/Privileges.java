package net.krinsoft.privileges;

import com.google.common.io.Files;
import com.pneumaticraft.commandhandler.CommandHandler;
import net.krinsoft.privileges.commands.BackupCommand;
import net.krinsoft.privileges.commands.BaseCommand;
import net.krinsoft.privileges.commands.CheckCommand;
import net.krinsoft.privileges.commands.DebugCommand;
import net.krinsoft.privileges.commands.DemoteCommand;
import net.krinsoft.privileges.commands.GroupBaseCommand;
import net.krinsoft.privileges.commands.GroupCreateCommand;
import net.krinsoft.privileges.commands.GroupListCommand;
import net.krinsoft.privileges.commands.GroupPermBaseCommand;
import net.krinsoft.privileges.commands.GroupPermRemoveCommand;
import net.krinsoft.privileges.commands.GroupPermSetCommand;
import net.krinsoft.privileges.commands.GroupRemoveCommand;
import net.krinsoft.privileges.commands.GroupRenameCommand;
import net.krinsoft.privileges.commands.GroupSetCommand;
import net.krinsoft.privileges.commands.InfoCommand;
import net.krinsoft.privileges.commands.ListCommand;
import net.krinsoft.privileges.commands.LoadCommand;
import net.krinsoft.privileges.commands.ProfilingCommand;
import net.krinsoft.privileges.commands.PromoteCommand;
import net.krinsoft.privileges.commands.ReloadCommand;
import net.krinsoft.privileges.commands.RestoreCommand;
import net.krinsoft.privileges.commands.SaveCommand;
import net.krinsoft.privileges.commands.UserBaseCommand;
import net.krinsoft.privileges.commands.UserListCommand;
import net.krinsoft.privileges.commands.UserPermRemoveCommand;
import net.krinsoft.privileges.commands.UserPermSetCommand;
import net.krinsoft.privileges.commands.UserResetCommand;
import net.krinsoft.privileges.commands.VersionCommand;
import net.krinsoft.privileges.groups.GroupManager;
import net.krinsoft.privileges.importer.ImportManager;
import net.krinsoft.privileges.listeners.BlockListener;
import net.krinsoft.privileges.listeners.PlayerListener;
import net.krinsoft.privileges.players.PlayerManager;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private boolean on_start_clean = false;

    // managers and handlers
    private PlayerManager playerManager;
    //private PermissionManager permissionManager;
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
        registerPermissions();
        performImports();
        getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
            public void run() {
                boolean tmp = debug;
                debug = false;
                registerPermissions();
                updatePermissions();
                debug = tmp;
            }
        }, 5L);
        registerEvents();
        registerCommands();
        if (on_start_clean) {
            getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
                public void run() {
                    log("Removing old users from users.yml...");
                    long timeout = 1000L * 60L * 60L * 24L * 30L;
                    for (OfflinePlayer player : getServer().getOfflinePlayers()) {
                        if (System.currentTimeMillis() - player.getLastPlayed() >= timeout || player.isBanned()) {
                            if ((getUsers().get(player.getName()) != null && !getUsers().getString(player.getName() + ".group").equals(getConfig().getString("default_group", "default"))) || player.isBanned()) {
                                getUsers().set(player.getName(), null);
                                debug("'" + player.getName() + "' removed from users.yml");
                            }
                        }
                    }
                    saveUsers();
                    log("... done!");
                }
            }, 1L);
        }
        try {
            getServer().getPluginManager().getPermission("privileges.*").setDefault(PermissionDefault.OP);
        } catch (NullPointerException e) {
            debug("Error setting default permission for 'privileges.*'");
        }

        if (getConfig().getBoolean("metrics")) {
            try {
                // initialize the plugin metrics tracker
                Metrics metrics = new Metrics(this);
                // track the number of groups
                metrics.addCustomData(new Metrics.Plotter() {
                    @Override
                    public String getColumnName() {
                        return "Groups";
                    }
                    @Override
                    public int getValue() {
                        ConfigurationSection groups = getGroups().getConfigurationSection("groups");
                        if (groups != null) {
                            return groups.getKeys(false).size();
                        }
                        return 0;
                    }
                });
                // track the number of users
                metrics.addCustomData(new Metrics.Plotter() {
                    @Override
                    public String getColumnName() {
                        return "Users";
                    }
                    @Override
                    public int getValue() {
                        ConfigurationSection users = getUsers().getConfigurationSection("users");
                        if (users != null) {
                            return getUsers().getConfigurationSection("users").getKeys(false).size();
                        }
                        return 0;
                    }
                });
                metrics.start();
            } catch (IOException e) {
                log("An error occurred while posting results to the Metrics.");
                warn(e.getLocalizedMessage());
            }
        }
        time = System.nanoTime() - time;
        profile(time, "plugin_enable");
    }

    @Override
    public void onDisable() {
        long time = System.nanoTime();
        playerManager.disable();
        //permissionManager.disable();
        time = System.nanoTime() - time;
        profile(time, "plugin_disable");
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
            debug("config.yml checksum: " + sha256(configFile));
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

    public void reload() {
        //permissionManager.clean();
        playerManager.disable();
        groupManager.clean();
        configuration = null;
        configFile = null;
        groups = null;
        groupFile = null;
        users = null;
        userFile = null;
        registerConfiguration();
        registerPermissions();
        updatePermissions();
    }

    private void registerPermissions() {
        playerManager = new PlayerManager(this);
        //permissionManager = new PermissionManager(this);
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

    private void updatePermissions() {
        groupManager.reload();
        playerManager.reload();
        //permissionManager.reload();
    }

    private void registerConfiguration() {
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
        on_start_clean = getConfig().getBoolean("users_cleanup", false);
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
        commandHandler.registerCommand(new ProfilingCommand(this));
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
            debug("users.yml checksum: " + sha256(userFile));
            getUsers().save(userFile);
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
            debug("groups.yml checksum: " + sha256(groupFile));
            getGroups().save(groupFile);
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

    /**
     * Writes a profiler message and then calculates the average for the specified event
     * @param time The amount of time the event which triggered the profiler took
     * @param event The event which triggered the profiler
     */
    public void profile(long time, String event) {
        if (profile) {
            String message = "[Profiler] [" + event + "] " + (time / 1000000L) + "ms (" + time + "ns)";
            getLogger().info(message);
            long average = getConfig().getLong("profiling." + event, 0);
            average = average + time;
            average = average / 2;
            getConfig().set("profiling." + event, average);
            saveConfig();
        }
    }

    /**
     * Writes a profiler log message to the server.log
     * @param message The message to write to the log
     * @see #profile(long, String)
     * @deprecated since 1.6
     */
    @Deprecated
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

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    @Deprecated
    public PermissionManager getPermissionManager() {
        return null;
    }

    public GroupManager getGroupManager() {
        return this.groupManager;
    }

    /**
     * Creates a sha-256 hash of the specified file
     * @param file The file we're creating a sha-256 hash for
     * @return The hash, as a string
     */
    private String sha256(File file) {
        try {
            byte[] bytes = Files.getDigest(file, MessageDigest.getInstance("SHA-256"));
            StringBuilder checksum = new StringBuilder();
            for (byte b : bytes) {
                checksum.append(b);
            }
            return checksum.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                return e.getLocalizedMessage();
            }
            e.printStackTrace();
        }
        return null;
    }

}
