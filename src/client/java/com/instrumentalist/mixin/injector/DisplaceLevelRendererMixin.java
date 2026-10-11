package com.instrumentalist.mixin.injector;

import com.instrumentalist.krs.hacks.features.combat.Displace;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class DisplaceLevelRendererMixin {
    @Unique
    private float krs$displacePartialTicks;

    @Inject(method = "render", at = @At("HEAD"))
    private void krs$displaceCapturePartialTicks(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker,
                                               boolean renderBlockOutline, CameraRenderState camera,
                                               Matrix4fc projection, GpuBufferSlice fog, Vector4f fogColor,
                                               boolean renderSky, CallbackInfo ci) {
        krs$displacePartialTicks = Minecraft.getInstance().gameRenderer.mainCamera().getCameraEntityPartialTicks(deltaTracker);
    }

    @Inject(method = "submitFeatures", at = @At("TAIL"))
    private void krs$displaceSubmitArrow(LevelRenderState levelState, SubmitNodeCollector submitter,
                                       boolean renderBlockOutline, CallbackInfo ci) {
        Displace.submitDirectionArrow(submitter, levelState.cameraRenderState, krs$displacePartialTicks);
    }
}
