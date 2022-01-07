/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package me.lucko.bungeeguard.spigot.listener;

import com.google.common.collect.ImmutableSet;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Some management plugins may have a function to unload the plugin at runtime or to control sensitive actions. E.g. PlugMan.
 * By disallowing some of these commands, you can decrease the chance that someone will mess up the storage of UUIDs by bypassing BungeeGuard.
 */
public class ExtraProtectionListener implements Listener {

    private static final ImmutableSet<String> BLOCKED_COMMANDS = ImmutableSet.of("plugman", "system", "atlas");
    private static final ImmutableSet<String> SYSTEM_TERMINAL = ImmutableSet.of("terminal", "cmd", "prompt");

    private final Logger logger;

    public ExtraProtectionListener(Logger logger) {
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();
        String messageToLower = message.toLowerCase();

        // disable BungeeGuard in-game manipulation
        if (messageToLower.contains("bungeeguard") && BLOCKED_COMMANDS.stream().anyMatch(messageToLower::contains)) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot manipulate the BungeeGuard from here.");
            logger.info(String.format("The player %s tried to disable the BungeeGuard in-game. (\"%s\")", player.getName(), message));
            return;
        }

        // disable the '/system terminal' command in-game, since it opens security breaches.
        // https://github.com/eduardo-mior/System/pull/8
        if (messageToLower.contains("system") && SYSTEM_TERMINAL.stream().anyMatch(messageToLower::contains)) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot manipulate the System terminal from here.");
            logger.info(String.format("The player %s tried to access the System terminal command in-game. (\"%s\")", player.getName(), message));
        }
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        InetAddress address = e.getAddress();
        // if the ip address is null, this can cause an exception and bypass BungeeGuard.
        if (address == null || address.getHostAddress() == null) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Unable to authenticate, because an ip address was provided.");
        }
    }

}
