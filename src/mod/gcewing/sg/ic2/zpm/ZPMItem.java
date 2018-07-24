package gcewing.sg.ic2.zpm;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;

public class ZPMItem extends Item {
  static final String ENERGY = "AvailableEnergy";

  public ZPMItem(ResourceLocation registryName, String unlocalizedName) {
    this.setRegistryName(registryName);
    this.setUnlocalizedName(unlocalizedName);


    this.registerInventoryModel();

  }

  @SideOnly(Side.CLIENT)
  private void registerInventoryModel() {
    ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
  }
}
