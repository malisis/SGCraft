package gcewing.sg.util;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.entity.living.player.Player;

public class PermissionsUtil {

    public PermissionsUtil() {
        System.out.println("SGCraft permissions system initialized!");
    }

    public static boolean spongeHasPermission(EntityPlayer player, String permission) {
        Player sPlayer = (Player) player;
        return sPlayer.hasPermission(permission);
    }
}
