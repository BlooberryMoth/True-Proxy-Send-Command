package com.thatgalblu.tpsc;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class Paper extends JavaPlugin {
    static final String channelID = "tpsc:comms";
    final Logger logger = this.getLogger();

    @Override
    public void onEnable() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, channelID);
        getServer().getMessenger().registerIncomingPluginChannel(this, channelID, (channel, player, data) -> {
            ByteArrayDataInput buffer = ByteStreams.newDataInput(data);
            List<String> servers = new ArrayList<>();
            while (true) {
                try { servers.add(buffer.readUTF()); }
                catch (Exception ignored) { break; }
            }
            ProxiedServerArgument.servers = servers;
        });

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(
                Commands.literal("send")
                            .then(Commands.argument("players", ArgumentTypes.players())
                            .then(Commands.argument("server", new ProxiedServerArgument())
                            .executes(context -> {
                                final List<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                final String server = context.getArgument("server", String.class);

                                players.forEach(player -> {
                                    ByteArrayDataOutput buffer = ByteStreams.newDataOutput();
                                    buffer.writeUTF(player.getUniqueId().toString());
                                    buffer.writeUTF(server);
                                    player.sendPluginMessage(this, channelID, buffer.toByteArray());
                                });

                                return Command.SINGLE_SUCCESS;
                            })
                    )
            ).build()));

        logger.info("TPSC-P locked and loaded.");
    }
}

@NullMarked
class ProxiedServerArgument implements CustomArgumentType.Converted<String, String> {
    public static List<String> servers = new ArrayList<>();

    @Override
    public String convert(String nativeType) { return nativeType; }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (String server : servers) builder.suggest(server);
        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() { return StringArgumentType.word(); }
}