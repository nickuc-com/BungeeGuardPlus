package me.lucko.bungeeguard.backend;

import java.util.List;

public interface BungeeGuardBackend {

    String getKickMessage(String key);

    List<String> getTokens();

    void reloadConfig();
}
