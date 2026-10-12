package gg.lellek.hud.emote;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gg.lellek.hud.LellekHud;
import gg.lellek.hud.cosmetic.CosmeticSync;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Coins und gekaufte Emotes – Zustand vom Cosmetics-Server. */
public class Shop {
    public static volatile int coins = -1; // -1 = noch nicht geladen
    public static final Set<String> owned = new HashSet<String>();
    public static final Map<String, Integer> prices = new HashMap<String, Integer>();
    public static volatile String lastError;

    /** Offline (kein Cosmetics-Server erreichbar oder noch nie geladen): alle Emotes frei – nur lokal sichtbar */
    public static boolean offline() { return CosmeticSync.serverDown() || prices.isEmpty(); }
    /** Freie Emotes (Preis 0 lokal oder laut Server) gehören jedem; Shop-Emotes nach Kauf */
    public static boolean owns(String id) {
        if (offline()) return true;
        Integer p = prices.get(id); Emote e = Emotes.byId(id);
        int price = p != null ? p : e != null ? e.price : 0;
        return owned.contains(id) || price == 0;
    }
    public static int price(Emote e) { Integer p = prices.get(e.id); return p != null ? p : e.price; }

    public static void refresh() {
        CosmeticSync.request("GET", "/v1/shop/me", null, true, new CosmeticSync.Callback() { public void done(JsonObject j, String err) {
            if (err != null) { lastError = err; if (coins < 0) coins = 0; return; }
            coins = j.get("coins").getAsInt(); owned.clear(); for (JsonElement e : j.getAsJsonArray("owned")) owned.add(e.getAsString());
            prices.clear(); for (Map.Entry<String, JsonElement> en : j.getAsJsonObject("prices").entrySet()) prices.put(en.getKey(), en.getValue().getAsInt());
            lastError = null;
        } });
    }
    public static void buy(final String id) {
        CosmeticSync.request("POST", "/v1/shop/buy", "{\"item\":\"" + id + "\"}", true, new CosmeticSync.Callback() { public void done(JsonObject j, String err) {
            if (err != null) { lastError = err; LellekHud.log("Shop: " + err); return; }
            coins = j.get("coins").getAsInt(); owned.clear(); for (JsonElement e : j.getAsJsonArray("owned")) owned.add(e.getAsString()); lastError = null;
        } });
    }
    public static void heartbeat() { CosmeticSync.request("POST", "/v1/shop/heartbeat", "{}", true, new CosmeticSync.Callback() { public void done(JsonObject j, String err) { if (err == null && j.has("coins")) coins = j.get("coins").getAsInt(); } }); }
}
