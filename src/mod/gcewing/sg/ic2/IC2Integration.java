//------------------------------------------------------------------------------------------------
//
//   SG Craft - IC2 Integration Module
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.ic2;

import gcewing.sg.ic2.zpm.ZPMItem;
import gcewing.sg.ic2.zpm.ZpmInterfaceCart;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.*;
import gcewing.sg.*;

import ic2.api.item.*; //[IC2]
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class IC2Integration extends BaseSubsystem<SGCraft, SGCraftClient> {

    public static ItemStack getIC2Item(String name) {
        return getIC2Item(name, null);
    }

    public static ItemStack getIC2Item(String name, String variant) {
        ItemStack stack = IC2Items.getItem(name, variant);
        if (stack == null)
            throw new RuntimeException(String.format("IC2 item %s.%s not found", name, variant));
        return stack;
    }
    
    @Override
    public void registerBlocks() {
        mod.ic2PowerUnit = mod.newBlock("ic2PowerUnit", IC2PowerBlock.class, IC2PowerItem.class);

        SGCraft.zpm_interface_cart = new ZpmInterfaceCart(Material.ROCK).setHardness(1.5f);
        SGCraft.zpm_interface_cart.setRegistryName(new ResourceLocation(Info.modID, "block/zpm_interface_cart"));
        SGCraft.zpm_interface_cart.setUnlocalizedName(SGCraft.zpm_interface_cart.getRegistryName().getResourcePath().replace('/', '.'));
        SGCraft.zpm_interface_cart.setCreativeTab(CreativeTabs.MISC);
        ForgeRegistries.BLOCKS.register(SGCraft.zpm_interface_cart);
    }

    @Override
    protected void registerItems() {
        SGCraft.zpm_interface_cart_item = new ItemBlock(SGCraft.zpm_interface_cart);
        SGCraft.zpm_interface_cart_item.setRegistryName(SGCraft.zpm_interface_cart.getRegistryName());
        SGCraft.zpm_interface_cart_item.setUnlocalizedName(Info.modID + ".zpm_interface_cart");
        SGCraft.zpm_interface_cart_item.setCreativeTab(CreativeTabs.MISC);
        ForgeRegistries.ITEMS.register(SGCraft.zpm_interface_cart_item);
        SGCraft.zpm = new ZPMItem(new ResourceLocation(Info.modID, "zpm"), "zpm_item");

        ForgeRegistries.ITEMS.register(SGCraft.zpm);
    }

    @Override
    public void registerRecipes() {
        ItemStack rubber = getIC2Item("crafting", "rubber");
        ItemStack copperPlate = getIC2Item("plate", "copper");
        ItemStack machine = getIC2Item("resource", "machine");
        ItemStack wire = getIC2Item("cable", "type:copper,insulation:0");
        ItemStack circuit = getIC2Item("crafting", "circuit");
        mod.newRecipe("ic2Capacitor",mod.ic2Capacitor, 1, "ppp", "rrr", "ppp", 'p', copperPlate, 'r', rubber);
        mod.newRecipe("ic2Powerunit", mod.ic2PowerUnit,  1, "cwc", "wMw", "cec", 'c', mod.ic2Capacitor, 'w', wire, 'M', machine, 'e', circuit);
    }
    
}
