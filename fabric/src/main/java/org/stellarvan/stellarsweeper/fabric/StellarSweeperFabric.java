package org.stellarvan.stellarsweeper.fabric;

import net.fabricmc.api.ModInitializer;
import org.stellarvan.stellarsweeper.StellarSweeper;

public final class StellarSweeperFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        StellarSweeper.init();
    }
}
