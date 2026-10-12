package gg.lellek.hud.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;

/** Ein animiertes Cosmetic: baut seine Teile über ein MeshDefinition und rendert relativ zum Spielerkörper. */
public abstract class Cosmetic {
    public final String id, name, category;
    public final Identifier texture;
    protected final ModelPart root;

    protected Cosmetic(String id, String name, String category, String textureFile, int texW, int texH) {
        this.id = id; this.name = name; this.category = category;
        this.texture = Identifier.fromNamespaceAndPath("lellekhud", "textures/cosmetics/" + textureFile);
        MeshDefinition mesh = new MeshDefinition();
        build(mesh.getRoot());
        this.root = LayerDefinition.create(mesh, texW, texH).bakeRoot();
    }

    protected abstract void build(PartDefinition root);
    /** Textur für diesen Spieler – null = nicht zeichnen */
    public Identifier textureFor(java.util.UUID uuid) { return texture; }
    /** t = Sekunden; Pose steht am Körperursprung des Spielers. */
    public abstract void render(PoseStack pose, SubmitNodeCollector out, int light, AvatarRenderState state, float t, Identifier tex);
}
