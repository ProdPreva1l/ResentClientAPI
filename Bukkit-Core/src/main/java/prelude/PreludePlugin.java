package prelude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.TestOnly;
import prelude.adapter.VersionAdapter;
import prelude.adapter.impl.Adapter_1_11;
import prelude.adapter.impl.Adapter_1_16_5;
import prelude.adapter.impl.Adapter_1_9;
import prelude.api.Prelude;
import prelude.mods.*;

import java.io.File;
import java.util.Optional;

@NoArgsConstructor
public final class PreludePlugin extends JavaPlugin {
    @Getter private static PreludePlugin instance;
    private VersionAdapter adapter = null;

    @TestOnly
    @SuppressWarnings("unused")
    private PreludePlugin(JavaPluginLoader loader, PluginDescriptionFile description,
                          File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onLoad() {
        VersionUtil.BukkitVersion version = VersionUtil.getServerBukkitVersion();
        if (version.isUnknown()) {
            getLogger().warning("Server is running an outdated version ({}) and does not fully support all features."
                    .replace("{}", version.toString()));
        }

        else if (version.equals(VersionUtil.v1_8_8_R01)) {
            getLogger().warning("Server is running an outdated version ({}) and does not fully support all features."
                    .replace("{}", version.toString()));
        }

        else if (version.isHigherThanOrEqualTo(VersionUtil.v1_9_R01) && version.isLowerThan(VersionUtil.v1_11_R01)) {
            adapter = new Adapter_1_9(this);
            getLogger().warning("Server is running an outdated version ({}) and does not fully support all features."
                    .replace("{}", version.toString()));
        }

        else if (version.isHigherThanOrEqualTo(VersionUtil.v1_11_R01) && version.isLowerThan(VersionUtil.v1_16_5_R01)) {
            adapter = new Adapter_1_11(this);
            getLogger().warning("Server is running an outdated version ({}) and does not fully support all features."
                    .replace("{}", version.toString()));
        }

        else if (version.isHigherThanOrEqualTo(VersionUtil.v1_16_5_R01)) {
            adapter = new Adapter_1_16_5(this);
        }

        else {
            getLogger().warning("Server is running an outdated version ({}) and does not fully support all features."
                    .replace("{}", version.toString()));
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();

        new BukkitPrelude();

        if (getConfig().getBoolean("purely-api")) {
            getLogger().info("Partially Started Resent Client's Prelude API");
            return;
        }

        new BukkitOffHand();
        new BukkitTotemTweaks();
        new BukkitFreeLook();
        new BukkitServerTps();
        new BukkitAnchorRenderer();

        getServer().getPluginManager().registerEvents(new BaseImplementation(this), this);
      
        getServer().getMessenger().registerOutgoingPluginChannel(this, Prelude.CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(
                this,
                Prelude.CHANNEL,
                new BaseImplementation.ResentClientMessageListener());

        getLogger().info("Fully Started Resent Client's Prelude API");
    }

    @Override
    public void onDisable() {
        getLogger().info("Stopped Prelude API");
    }

    public void debug(String message) {
        if (getConfig().getBoolean("debug")) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public ConfigurationSection getModConfig() {
        return getConfig().getConfigurationSection("mods");
    }

    public Optional<VersionAdapter> getAdapter() {
        return Optional.ofNullable(adapter);
    }
}