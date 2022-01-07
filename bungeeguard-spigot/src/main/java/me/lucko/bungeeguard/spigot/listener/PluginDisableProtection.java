/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package me.lucko.bungeeguard.spigot.listener;

import com.google.common.collect.ImmutableSet;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.net.InetAddress;

/**
 * Some management plugins may have a function to unload the plugin at runtime. E.g. PlugMan.
 * By disallowing some of these commands, you can decrease the chance that someone will mess up the storage of UUIDs by bypassing BungeeGuard.
 */
public class PluginDisableProtection implements Listener {

    private static final ImmutableSet<String> BLOCKED_COMMANDS = ImmutableSet.of("plugman", "system", "atlas");

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        String messageToLower = e.getMessage().toLowerCase();
        if (messageToLower.contains("bungeeguard") && BLOCKED_COMMANDS.stream().anyMatch(messageToLower::contains)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "You cannot manipulate the BungeeGuard from here.");
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
