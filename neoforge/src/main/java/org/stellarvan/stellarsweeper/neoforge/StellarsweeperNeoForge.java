package org.stellarvan.stellarsweeper.neoforge;

import org.stellarvan.stellarsweeper.Stellarsweeper;
import net.neoforged.fml.common.Mod;

@Mod(Stellarsweeper.MOD_ID)
public final class StellarsweeperNeoForge {
    public StellarsweeperNeoForge() {
        // Run our common setup.
        Stellarsweeper.init();
    }
}
