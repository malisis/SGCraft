package gcewing.sg.teleporter;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class FakeTeleporter implements ITeleporter {

    // This is a FakeTeleporter class used to override the default entity.changeDimension method. [public Entity changeDimension(int dimensionIn, net.minecraftforge.common.util.ITeleporter teleporter)]
    @Override
    public void placeEntity(World world, Entity entity, float yaw) {}
}
