package org.stellarvan.stellarsweeper.neoforge;

import net.neoforged.fml.common.Mod;
import org.stellarvan.stellarsweeper.Constants;
import org.stellarvan.stellarsweeper.StellarSweeper;

@Mod(Constants.MOD_ID)
public final class StellarSweeperNeoForge {
    public StellarSweeperNeoForge() {
        StellarSweeper.init();
    }
}
