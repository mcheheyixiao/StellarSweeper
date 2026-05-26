package org.stellarvan.stellarsweeper.platform;

import dev.architectury.platform.Platform;
import java.nio.file.Path;

public final class Services {
    private static final PlatformBridge BRIDGE = new ArchitecturyPlatformBridge();

    private Services() {
    }

    public static PlatformBridge platform() {
        return BRIDGE;
    }

    private static final class ArchitecturyPlatformBridge implements PlatformBridge {
        @Override
        public Path getConfigDir() {
            return Platform.getConfigFolder();
        }

        @Override
        public boolean isModLoaded(String modId) {
            return Platform.isModLoaded(modId);
        }

        @Override
        public String getPlatformName() {
            if (Platform.isFabric()) {
                return "fabric";
            }
            if (Platform.isNeoForge()) {
                return "neoforge";
            }
            if (Platform.isForgeLike()) {
                return "forge-like";
            }
            return "unknown";
        }
    }
}
