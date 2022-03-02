package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyPlatform;

import java.util.Locale;
import java.util.Optional;

/**
 * Velocity implementation of Platform
 */
public class VelocityPlatform extends ProxyPlatform {

    //instance of ProxyServer
    private final ProxyServer server;

    /**
     * Constructs new instance with given parameter
     * @param server - instance of ProxyServer
     */
    public VelocityPlatform(ProxyServer server) {
        super(new PacketBuilder());
        this.server = server;
    }

    @Override
    public void loadFeatures() {
        super.loadFeatures();
        for (Player p : server.getAllPlayers()) {
            TAB.getInstance().addPlayer(new VelocityTabPlayer(p));
        }
    }

    @Override
    public String getPluginVersion(String plugin) {
        Preconditions.checkNotNull(plugin, "plugin");
        Optional<PluginContainer> pl = server.getPluginManager().getPlugin(plugin.toLowerCase(Locale.US));
        return pl.flatMap(pluginContainer -> pluginContainer.getDescription().getVersion()).orElse(null);
    }
}