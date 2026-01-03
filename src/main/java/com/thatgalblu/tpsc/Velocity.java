package com.thatgalblu.tpsc;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import static com.thatgalblu.tpsc.Paper.channelID;

@Plugin(id="tpsc", name="TPSC", version="1.0")
public class Velocity {
    @Inject
    private final ProxyServer proxy;
    @Inject
    final Logger logger;
    @Inject
    public Velocity(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxy.getChannelRegistrar().register(MinecraftChannelIdentifier.from(channelID));

        logger.info("TPSC-V locked and loaded.");
    }

    @Subscribe
    public void onCommand(PluginMessageEvent event) {
        if (event.getSource() instanceof ServerConnection && event.getIdentifier().getId().equals(channelID)) {
            ByteArrayDataInput buffer = ByteStreams.newDataInput(event.getData());

            UUID playerUUID = UUID.fromString(buffer.readUTF());
            String serverName = buffer.readUTF();

            proxy.getServer(serverName).ifPresent(server -> {
                proxy.getPlayer(playerUUID).ifPresent(player -> {
                    player.createConnectionRequest(server).fireAndForget();
                    event.setResult(PluginMessageEvent.ForwardResult.handled());
                });
            });
        }
    }

    @Subscribe
    public void onPlayerJoin(ServerPostConnectEvent event) {
        ByteArrayDataOutput buffer = ByteStreams.newDataOutput();
        Collection<RegisteredServer> servers = proxy.getAllServers();
        for (RegisteredServer server : servers) buffer.writeUTF(server.getServerInfo().getName());

        event.getPlayer().getCurrentServer().ifPresent(server -> server.sendPluginMessage(MinecraftChannelIdentifier.from(channelID), buffer.toByteArray()));
    }
}