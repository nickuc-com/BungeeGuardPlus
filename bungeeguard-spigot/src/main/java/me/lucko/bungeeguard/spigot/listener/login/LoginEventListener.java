/*
 * This file is part of BungeeGuard, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.bungeeguard.spigot.listener.login;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.lucko.bungeeguard.spigot.TokenStore;
import me.lucko.bungeeguard.spigot.listener.AbstractTokenListener;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Collection;
import java.util.logging.Logger;

public class LoginEventListener extends AbstractTokenListener implements Listener {

    public LoginEventListener(TokenStore tokenStore, Logger logger, ConfigurationSection config) {
        super(tokenStore, logger, config);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLogin(PlayerLoginEvent e) {
        if (e.getAddress() == null || e.getAddress().getHostAddress() == null) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§c[nLogin]§r\n§r\n§r§cImpossível se autenticar, pois um endereço de ip foi fornecido.§r\n§r§cEm caso de problemas, tente reiniciar seu cliente.");
            return;
        }

        Player player = e.getPlayer();
        GameProfile gameProfile;
        try {
            gameProfile = (GameProfile) player.getClass().getDeclaredMethod("getProfile").invoke(player);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, noDataKickMessage);
            return;
        }

        Collection<Property> tokens = gameProfile.getProperties().get("bungeeguard-token");
        if (tokens.isEmpty()) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, noDataKickMessage);
            return;
        }

        Property tokenProperty = tokens.iterator().next();
        String token = tokenProperty.getValue();

        if (!tokenStore.isAllowed(token)) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, invalidTokenKickMessage);
            return;
        }

        gameProfile.getProperties().removeAll("bungeeguard-token");
    }

}
