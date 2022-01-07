/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package me.lucko.bungeeguard.sponge;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;

/**
 * Some management plugins may have a function to unload the plugin at runtime or to control sensitive actions. E.g. PlugMan.
 * By disallowing some of these commands, you can decrease the chance that someone will mess up the storage of UUIDs by bypassing BungeeGuard.
 */
public class ExtraProtectionListener {

    private static final ImmutableSet<String> BLOCKED_COMMANDS = ImmutableSet.of("plugman", "system", "atlas");

    @Listener
    public void onSendCommand(SendCommandEvent event) {
        String commandToLower = event.getCommand().toLowerCase();
        if (commandToLower.contains("bungeeguard") && BLOCKED_COMMANDS.stream().anyMatch(commandToLower::contains)) {
            event.setCancelled(true);
            event.setResult(CommandResult.empty());
        }
    }

}
