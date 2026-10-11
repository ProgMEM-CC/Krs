package com.instrumentalist.krs.hacks.features.combat;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** 26.2 feature submission: untextured, two-sided triangles and two-pixel outline on top. */
public final class DisplaceArrowRenderer {
    private DisplaceArrowRenderer() {
    }

    public static void submit(SubmitNodeCollector submitter, CameraRenderState camera,
                              DisplaceMath.ArrowGeometry geometry, float alpha) {
        if (submitter == null || camera == null || !camera.initialized)
            return;

        DrawableGizmoPrimitives primitives = new DrawableGizmoPrimitives();
        int fill = color(0xFFFFFF, 0.82F * alpha);
        int outline = color(0x000000, 0.95F * alpha);
        List<DisplaceMath.Point> triangles = geometry.triangles();
        for (int i = 0; i < triangles.size(); i += 3) {
            primitives.addTriangleFan(new Vec3[]{
                    vertex(triangles.get(i)), vertex(triangles.get(i + 1)), vertex(triangles.get(i + 2))
            }, fill);
        }
        List<DisplaceMath.Point> boundary = geometry.outline();
        for (int i = 0; i < boundary.size(); i++) {
            primitives.addLine(vertex(boundary.get(i)), vertex(boundary.get((i + 1) % boundary.size())),
                    outline, 2.0F);
        }
        // Vanilla handles camera-relative transforms, blending, clipping, shader resources,
        // frame buffers and the separate always-on-top pass. No global GL state is mutated.
        primitives.submit(submitter, camera, true);
    }

    private static Vec3 vertex(DisplaceMath.Point point) {
        return new Vec3(point.x(), point.y(), point.z());
    }

    private static int color(int rgb, float alpha) {
        int opacity = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
        return opacity << 24 | rgb;
    }
}
