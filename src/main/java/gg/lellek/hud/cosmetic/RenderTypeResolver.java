package gg.lellek.hud.cosmetic;

import gg.lellek.hud.LellekHud;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Findet zur Laufzeit die Fabrikmethode für einen Entity-RenderType mit Textur.
 * Die Signatur von RenderTypes.entityCutoutNoCull(...) ändert sich zwischen 26.x-Versionen –
 * hier wird die erste statische Methode mit passendem Namen genommen, deren erster Parameter eine Identifier ist.
 */
public final class RenderTypeResolver {
    private static Method factory; private static boolean searched;
    private static final Map<Identifier, RenderType> cache = new HashMap<>();

    public static RenderType entityCutoutNoCull(Identifier tex) {
        RenderType cached = cache.get(tex); if (cached != null) return cached;
        if (!searched) { searched = true; factory = find(); if (factory == null) LellekHud.log("Cosmetics: keine RenderTypes-Fabrik gefunden – Cosmetics werden nicht gezeichnet"); }
        if (factory == null) return null;
        try {
            Class<?>[] p = factory.getParameterTypes(); Object[] args = new Object[p.length]; args[0] = tex;
            for (int i = 1; i < p.length; i++) args[i] = p[i] == boolean.class ? Boolean.FALSE : p[i] == int.class ? 0 : p[i] == float.class ? 0f : null;
            RenderType rt = (RenderType) factory.invoke(null, args);
            cache.put(tex, rt); return rt;
        } catch (Exception e) { LellekHud.log("Cosmetics: RenderType-Aufruf fehlgeschlagen: " + e); factory = null; return null; }
    }
    private static Method find() {
        for (String name : new String[] { "entityCutoutNoCull", "entityCutout", "entityTranslucent", "entitySolid" })
            for (Method m : RenderTypes.class.getMethods())
                if (m.getName().equals(name) && Modifier.isStatic(m.getModifiers()) && m.getParameterCount() >= 1 && m.getParameterTypes()[0] == Identifier.class && RenderType.class.isAssignableFrom(m.getReturnType())) { LellekHud.log("Cosmetics: nutze RenderTypes." + name + " mit " + m.getParameterCount() + " Parametern"); return m; }
        return null;
    }
}
