package gg.lellek.hud.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;

import java.util.ArrayList;
import java.util.List;

/** Alle eingebauten Cosmetics. */
public class Cosmetics {
    public static final List<Cosmetic> ALL = new ArrayList<>();
    public static final String[] CATEGORIES = { "Alle", "Kopf", "Rücken", "Effekte" };
    static { ALL.add(new Wings()); ALL.add(new Halo()); ALL.add(new Crown()); ALL.add(new LellekCape()); ALL.add(new CustomCape()); ALL.add(new Sparkles());
        ALL.add(new LellekCape("cape_ocean", "Ozean-Cape", "cape_ocean.png")); ALL.add(new LellekCape("cape_emerald", "Smaragd-Cape", "cape_emerald.png")); ALL.add(new LellekCape("cape_fire", "Feuer-Cape", "cape_fire.png")); ALL.add(new LellekCape("cape_galaxy", "Galaxie-Cape", "cape_galaxy.png")); }
    public static Cosmetic byId(String id) { for (Cosmetic c : ALL) if (c.id.equals(id)) return c; return null; }

    /** Modellteil abschicken – Signatur von SubmitNodeCollector kann sich in 26.x ändern */
    static void part(ModelPart p, PoseStack pose, SubmitNodeCollector out, net.minecraft.resources.Identifier tex, int light) {
        net.minecraft.client.renderer.rendertype.RenderType rt = RenderTypeResolver.entityCutoutNoCull(tex);
        if (rt == null) return;
        out.submitModelPart(p, pose, rt, light, OverlayTexture.NO_OVERLAY, null);
    }

    static class Wings extends Cosmetic {
        ModelPart left, right;
        Wings() { super("wings", "Flügel", "Rücken", "wings.png", 64, 32); }
        protected void build(PartDefinition r) {
            r.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 0).addBox(0, -8, 0, 20, 20, 0, new net.minecraft.client.model.geom.builders.CubeDeformation(0.1f)), PartPose.offset(0, 2, 2.2f));
            r.addOrReplaceChild("left", CubeListBuilder.create().texOffs(32, 0).addBox(-20, -8, 0, 20, 20, 0, new net.minecraft.client.model.geom.builders.CubeDeformation(0.1f)), PartPose.offset(0, 2, 2.2f));
        }
        public void render(PoseStack pose, SubmitNodeCollector out, int light, AvatarRenderState s, float t, net.minecraft.resources.Identifier tex) {
            if (left == null) { left = root.getChild("left"); right = root.getChild("right"); }
            float flap = (float) Math.sin(t * 4f) * 0.45f + 0.55f;
            right.yRot = -0.35f - flap; right.zRot = 0.25f;
            left.yRot = 0.35f + flap;   left.zRot = -0.25f;
            part(root, pose, out, tex, light);
        }
    }

    static class Halo extends Cosmetic {
        Halo() { super("halo", "Heiligenschein", "Kopf", "halo.png", 16, 16); }
        protected void build(PartDefinition r) { r.addOrReplaceChild("ring", CubeListBuilder.create().texOffs(0, 0).addBox(-5, 0, -5, 10, 0, 10), PartPose.ZERO); }
        public void render(PoseStack pose, SubmitNodeCollector out, int light, AvatarRenderState s, float t, net.minecraft.resources.Identifier tex) {
            pose.pushPose();
            pose.translate(0, -0.72f + Math.sin(t * 2) * 0.03f, 0);
            pose.mulPose(new org.joml.Matrix4f().rotation(Axis.YP.rotationDegrees(t * 40 % 360)));
            part(root, pose, out, tex, light);
            pose.popPose();
        }
    }

    static class Crown extends Cosmetic {
        Crown() { super("crown", "Krone", "Kopf", "crown.png", 32, 32); }
        protected void build(PartDefinition r) { r.addOrReplaceChild("crown", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5f, -3, -4.5f, 9, 3, 9, new net.minecraft.client.model.geom.builders.CubeDeformation(0.2f)), PartPose.ZERO); }
        public void render(PoseStack pose, SubmitNodeCollector out, int light, AvatarRenderState s, float t, net.minecraft.resources.Identifier tex) {
            pose.pushPose();
            pose.mulPose(new org.joml.Matrix4f().rotation(Axis.YP.rotationDegrees(s.yRot)));
            pose.mulPose(new org.joml.Matrix4f().rotation(Axis.XP.rotationDegrees(s.xRot)));
            pose.translate(0, -0.5f, 0);
            part(root, pose, out, tex, light);
            pose.popPose();
        }
    }

    static class LellekCape extends Cosmetic {
        ModelPart cape;
        LellekCape() { this("cape", "Lellek-Cape"); }
        LellekCape(String id, String name) { this(id, name, "cape.png"); }
        LellekCape(String id, String name, String tex) { super(id, name, "Rücken", tex, 64, 32); }
        protected void build(PartDefinition r) { r.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 0).addBox(-5, 0, -1, 10, 16, 1), PartPose.offset(0, 0, 2)); }
        public void render(PoseStack pose, SubmitNodeCollector out, int light, AvatarRenderState s, float t, net.minecraft.resources.Identifier tex) {
            if (cape == null) cape = root.getChild("cape");
            // Vanilla-Cape-Physik aus dem Render-State
            float swing = Math.max(0, Math.min(150, s.capeFlap)) + (float) Math.sin(t * 6) * 3f;
            cape.xRot = (float) Math.toRadians(6 + swing / 2 + s.capeLean * 10);
            cape.zRot = (float) Math.toRadians(s.capeLean2 / 2);
            pose.pushPose();
            pose.translate(0, 0, 0.02f);
            part(root, pose, out, tex, light);
            pose.popPose();
        }
    }

    static class CustomCape extends LellekCape {
        CustomCape() { super("customcape", "Eigenes Cape"); }
        @Override public net.minecraft.resources.Identifier textureFor(java.util.UUID uuid) { return uuid == null ? null : CosmeticSync.capeTexture(uuid); }
    }

    static class Sparkles extends Cosmetic {
        int tick;
        Sparkles() { super("sparkles", "Glitzer-Spur", "Effekte", "sparkle.png", 8, 8); }
        protected void build(PartDefinition r) {}
        public void render(PoseStack pose, SubmitNodeCollector out, int light, AvatarRenderState s, float t, net.minecraft.resources.Identifier tex) {
            Minecraft mc = Minecraft.getInstance();
            if (++tick % 2 != 0 || mc.level == null) return;
            double x = s.x + (Math.random() - 0.5) * 0.6, y = s.y + Math.random() * 1.8, z = s.z + (Math.random() - 0.5) * 0.6;
            mc.level.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0.02, 0);
            if (tick % 6 == 0) mc.level.addParticle(ParticleTypes.FIREWORK, x, y, z, 0, 0.01, 0);
        }
    }
}
