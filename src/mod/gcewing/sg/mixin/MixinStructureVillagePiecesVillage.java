package gcewing.sg.mixin;

import gcewing.sg.SGCraft;
import gcewing.sg.features.tokra.TokraVillagerWorldRegistry;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.Random;

@Mixin(StructureVillagePieces.Village.class)
public abstract class MixinStructureVillagePiecesVillage {

    @Redirect(method = "spawnVillagers", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/EntityVillager;setProfession(Lnet/minecraftforge/fml/common/registry/VillagerRegistry$VillagerProfession;)V"))
    private void onSetProfessionViaStructureSpawn(final EntityVillager villager, final VillagerRegistry.VillagerProfession structureProfession) {
        Random rand = new Random();
        Optional<Boolean> populateTokra = TokraVillagerWorldRegistry.populateTokraVillagers(villager.getWorld().getWorldInfo().getWorldName());
        if (populateTokra.isPresent() && populateTokra.get()) {
            int profID = rand.nextInt(8);
            if (profID > 6) {
                villager.setProfession(SGCraft.tokraProfession);
            } else {
                villager.setProfession(profID);
            }
        }
    }
}