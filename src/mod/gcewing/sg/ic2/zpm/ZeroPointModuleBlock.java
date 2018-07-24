package gcewing.sg.ic2.zpm;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ZeroPointModuleBlock extends BlockContainer {
  public ZeroPointModuleBlock(final Material material) {
    super(material);
  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(final World world, final int meta) {
    return new ZeroPointModuleBlockEntity();
  }
}
