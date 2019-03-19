//------------------------------------------------------------------------------------------------
//
//   SG Craft - IC2 Stargate Power Unit Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.features.ic2;

import gcewing.sg.SGCraft;
import gcewing.sg.block.PowerBlock;
import net.minecraft.world.Explosion;

public class IC2PowerBlock extends PowerBlock<IC2PowerTE> {

    public IC2PowerBlock() {
        super(IC2PowerTE.class);
        setModelAndTextures("block/power.smeg",
            "ic2PowerUnit-bottom", "ic2PowerUnit-top", "ic2PowerUnit-side");
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return SGCraft.canHarvestSGBaseBlock;
    }
}
