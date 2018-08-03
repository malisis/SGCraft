package gcewing.sg.ic2.zpm;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ZPMItem extends Item {
  static final String ENERGY = "AvailableEnergy";

  /*public ZPMItem(ResourceLocation registryName, String unlocalizedName) {
    this.setRegistryName(registryName);
    this.setUnlocalizedName(unlocalizedName);
    if (SGCraft.creativeTabs == null) {
      System.out.println("Why is this null");
    } else {
      System.out.println("Its Not Null");
    }

    this.setCreativeTab(SGCraft.creativeTabs);
    this.registerInventoryModel();

  } */

  @SideOnly(Side.CLIENT)
  private void registerInventoryModel() {
    ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
  }
}
