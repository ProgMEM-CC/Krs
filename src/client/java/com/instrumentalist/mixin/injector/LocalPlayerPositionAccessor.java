package com.instrumentalist.mixin.injector;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface LocalPlayerPositionAccessor {
    @Accessor("xLast")
    double krs$getLastReportedX();

    @Accessor("yLast")
    double krs$getLastReportedY();

    @Accessor("zLast")
    double krs$getLastReportedZ();
}
