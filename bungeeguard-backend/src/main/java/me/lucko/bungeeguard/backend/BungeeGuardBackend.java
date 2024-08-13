package me.lucko.bungeeguard.backend;

import java.nio.file.Path;
import java.util.List;

public interface BungeeGuardBackend {

    String getKickMessage(String key);

    List<String> getTokens();

    Path getConfigPath();

    void reloadConfig();

    boolean isVerbose();
}
