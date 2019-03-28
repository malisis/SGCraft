//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.block;

import gcewing.sg.BaseBlock;
import gcewing.sg.tileentity.SGBaseTE;
import gcewing.sg.interfaces.ISGBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public abstract class SGBlock<TE extends TileEntity> extends BaseBlock<TE> implements ISGBlock {

    public static final AxisAlignedBB FULL_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    public static final AxisAlignedBB HALF_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);

    public SGBlock(Material material, Class<TE> teClass) {
        super(material, teClass);
    }

    @Override    
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (player.capabilities.isCreativeMode && isConnected(world, pos)) {
            if (world.isRemote)
                SGBaseTE.sendErrorMsg(player, "disconnectFirst");
            return false;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    
    boolean isConnected(World world, BlockPos pos) {
        SGBaseTE bte = getBaseTE(world, pos);
        return bte != null && bte.isConnected();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (source != null) {
            SGBaseTE te = getBaseTE(source, pos);
            if (te != null) {
                if (te.gateOrientation == 2 || te.gateOrientation == 3) {
                    return HALF_BLOCK_AABB;
                }
            }
        }
        return FULL_BLOCK_AABB;
    }
}
