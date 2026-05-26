package org.stellarvan.stellarsweeper.platform;

import java.nio.file.Path;

public interface PlatformBridge {
    Path getConfigDir();

    boolean isModLoaded(String modId);

    String getPlatformName();
}
