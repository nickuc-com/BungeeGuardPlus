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

public class PluginDisableProtection {

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
