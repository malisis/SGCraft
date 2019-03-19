//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Stargate Power Unit Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.block;

import gcewing.sg.SGCraft;
import gcewing.sg.tileentity.SGPowerTE;
import net.minecraft.world.Explosion;

public class SGPowerBlock extends PowerBlock<SGPowerTE> {

    public SGPowerBlock() {
        super(SGPowerTE.class);
        setModelAndTextures("block/power.smeg",
            "sgPowerUnit-bottom", "sgPowerUnit-top", "sgPowerUnit-side");
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return SGCraft.canHarvestSGBaseBlock;
    }
}
