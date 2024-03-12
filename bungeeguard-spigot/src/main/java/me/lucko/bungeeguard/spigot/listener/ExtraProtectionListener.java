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
import org.bukkit.event.player.PlayerLoginEvent;

import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Some management plugins may have a function to unload the plugin at runtime or to control sensitive actions. E.g. PlugMan.
 * By disallowing some of these commands, you can decrease the chance that someone will mess up the storage of UUIDs by bypassing BungeeGuard.
 */
public class ExtraProtectionListener implements Listener {

    private static final ImmutableSet<String> BLOCKED_COMMANDS = ImmutableSet.of("plugman", "system", "atlas");
    private static final ImmutableSet<String> SYSTEM_TERMINAL = ImmutableSet.of("terminal", "cmd", "prompt");
    public static boolean SUCCESSFULLY_DECODED;

    private final Logger logger;

    public ExtraProtectionListener(Logger logger) {
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();
        String messageToLower = message.toLowerCase();

        // Disable BungeeGuard in-game manipulation
        if (messageToLower.contains("bungeeguard") && BLOCKED_COMMANDS.stream().anyMatch(messageToLower::contains)) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot manipulate the BungeeGuard from here.");
            logger.warning(String.format("The player %s tried to disable the BungeeGuard in-game. (\"%s\")", player.getName(), message));
            return;
        }

        // Disable the "/system terminal" command in-game, since it opens security breaches.
        // https://github.com/eduardo-mior/System/pull/8
        if (messageToLower.contains("system") && SYSTEM_TERMINAL.stream().anyMatch(messageToLower::contains)) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot manipulate the System terminal from here.");
            logger.warning(String.format("The player %s tried to access the System terminal command in-game. (\"%s\")", player.getName(), message));
        }

        // Disable the "/plugman download" command in-game, since it opens RCE security breaches.
        // https://github.com/TheBlackEntity/PlugManX/blob/master/src/main/java/com/rylinaux/plugman/PlugManCommandHandler.java#L83
        if (messageToLower.contains("plugman") && messageToLower.contains("download")) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot manipulate the PlugMan download from here.");
            logger.warning(String.format("The player %s tried to access the PlugMan download command in-game. (\"%s\")", player.getName(), message));
        }
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        InetAddress ip = e.getAddress();

        // If the IP address is null, this can cause an exception and bypass BungeeGuard.
        if (ip == null || ip.getHostAddress() == null) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Unable to authenticate, because an invalid IP address was provided.");
        }

        // ProtocolLib has failed to process handshake packets in the past.
        // Therefore, we double-check that the handshake has been validated previously.
        // https://github.com/dmulloy2/ProtocolLib/issues/2601
        if (!SUCCESSFULLY_DECODED) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Unable to authenticate, because the handshake validation could not be confirmed.");
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        // ProtocolLib has failed to process handshake packets in the past.
        // Therefore, we double-check that the handshake has been validated previously.
        // https://github.com/dmulloy2/ProtocolLib/issues/2601
        if (!SUCCESSFULLY_DECODED) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Unable to authenticate, because the handshake validation could not be confirmed.");
        }
    }
}
