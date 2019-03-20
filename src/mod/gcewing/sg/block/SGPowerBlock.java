//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Stargate Power Unit Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.block;

import gcewing.sg.SGCraft;
import gcewing.sg.tileentity.SGPowerTE;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;

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

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
