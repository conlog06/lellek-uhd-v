# LellekHUD für Minecraft 26.3 (Fabric)

[![Build](https://github.com/conlog06/lellek-uhd-v/actions/workflows/build.yml/badge.svg)](https://github.com/conlog06/lellek-uhd-v/actions/workflows/build.yml)

**Download:** neueste Version unter [Releases](https://github.com/conlog06/lellek-uhd-v/releases/latest) – `LellekHUD-26.3-x.y.z.jar` in den `mods`-Ordner legen (benötigt Fabric Loader ≥ 0.19.5, Fabric API und Java 25).

## Neu in 1.3.1

- **HUD im Mod-Menü verschieben**: Seitenleiste → „✥ HUD verschieben“ oder ✥ auf einer HUD-Element-Karte. Das Menü blendet sich aus, Elemente direkt ziehen (mit Einrasten), Mausrad = Größe, Rechtsklick = Einstellungen, „Fertig“/ESC = zurück ins Menü.
- **Cosmetics-Rad (X)** fixt: schließt nicht mehr sofort, kein Weichzeichner, flüssiger. Halten + loslassen oder kurz tippen und klicken. Rechtsklick = abbrechen.
- **Eigene Cosmetics & Emotes** werden wieder angezeigt (Spieler-Erkennung über die Entity-ID statt Namensschild). Beim Emote wechselt die Kamera automatisch in die 3rd-Person und danach zurück.
- **Kein Lag mehr im Hintergrund**: neues Feature „Hintergrund-FPS-Limit“ (Standard 10 FPS, wenn Minecraft nicht im Fokus ist); Minimap rechnet seltener neu.

## Neu in 1.3 (26.3)

- **Minecraft 26.3** (Fabric API 0.162.0+26.3, Loader 0.19.5).
- **Smooth Zoom**: C halten zoomt weich rein und beim Loslassen weich wieder raus. **Mausrad** beim Zoomen zoomt stufenlos weiter ran – bis 50x (einstellbar bis 100x). Die Maus wird automatisch langsamer, je näher du ranzoomst, damit Zielen präzise bleibt. Einstellungen: Start-Zoom, Tempo, Max-Zoom, Stufe merken, Kino-Kamera, Zoom-Anzeige.
- **HUD-Elemente bleiben an ihrem Platz**: Positionen werden an der nächsten Bildschirmkante/-mitte verankert. Fenster größer/kleiner, Vollbild, andere GUI-Skalierung oder Element größer/kleiner machen – rechts oben bleibt rechts oben, Mitte bleibt Mitte. Alte Configs werden automatisch umgerechnet.
- **HUD-Editor**: Elemente rasten an Bildschirmmitte, Rändern und anderen Elementen ein (Hilfslinien), **Mausrad über einem Element** ändert die Größe.

## Bauen

`./gradlew build` (JDK 25) – die JAR liegt danach in `build/libs/`. Jeder Push wird auf GitHub automatisch gebaut; ein Tag `v*` erstellt ein Release mit Download.

---


Das HUD des LellekClient (0.4): FPS, CPS, Koordinaten, Ping, Uhrzeit, Keystrokes, Spotify, Rüstung, Effekte · Zoom, ToggleSprint, Fullbright, Hitboxen · Cosmetics mit Animation (Flügel, Heiligenschein, Krone, Lellek-Cape, Glitzer-Spur) · Hauptmenü mit Logo.

- **Rechte Shift-Taste**: HUD-Editor (ziehen, Rechtsklick = Einstellungen mit Regler und Farbwähler)
- **X**: Cosmetics-Rad (halten + loslassen, oder tippen + klicken)
- **C**: Zoom halten (weich), Mausrad zoomt näher ran
- **Numpad 4/5/6**: Spotify zurück / Play-Pause / weiter
- Config: `config/lellekhud.json`

## Emotes & Shop (1.1)

- **Taste G** öffnet die Emote-Auswahl (30 Emotes: Sitzen, Winken, Dab, Klatschen, Tanzen, Floss, Meditieren, Schlafen, Liegestütze, Boxen, Roboter …). Klick spielt ab, Bewegung oder G beendet. Andere LellekClient-Spieler sehen dein Emote über den Cosmetics-Server (Abgleich alle 2 s).
- **Shop** (aus der Emote-Auswahl oder ESC-Menü): Emotes kosten Coins, **10 Coins pro gespielter Minute** kommen automatisch. „Sitzen“ ist gratis. Preise stehen im Server (`SHOP` in server.js) und in `emote/Emotes.java`.
- **Cosmetics** haben neue, detailliertere Texturen (gefiederte Flügel, ziselierte Krone mit Edelsteinen, leuchtender Heiligenschein, Cape mit Stoffstruktur und Saum).
- **Tageszeit**: keine schwarzen Streifen mehr – die Zeit wird jetzt vor jedem Frame gesetzt, nicht nur pro Tick.
- Eigene Emotes: eine Zeile in `Emotes.java` mit den Gelenkwinkeln als Funktion der Zeit.

Für 26.2 läuft die Pose über einen Mixin auf `PlayerModel.setupAnim` – greift er nicht, fehlt nur die Bewegung, Shop und Sync funktionieren trotzdem.

## Element-Einstellungen, Karte, Layout (1.0)

- **Jedes Element hat eigene Optionen** (Rechtsklick im Editor oder Zahnrad im Mod-Menü): Keystrokes mit **eigener Farbe pro Taste** (W, A, S, D, LMB, RMB, Leertaste), Farbe und Textfarbe beim Drücken, **CPS direkt in LMB/RMB**. Farben als Swatches unter dem Farbwähler – Swatch anklicken, dann färben.
- **Koordinaten**: XYZ zusammen (eine oder drei Zeilen, mit/ohne Blickrichtung) und zusätzlich X, Y, Z als einzelne Elemente.
- **Richtung**: Leiste oder kompakt, Grad an/aus, Breite.
- **Fadenkreuz**: färbt sich beim Zielen auf Gegner (Farbe wählbar), optional dynamisch beim Laufen.
- **Karte auf M**: Vollbild-Karte, Mausrad zoomt, Ziehen verschiebt, Klick setzt einen Wegpunkt, Rechtsklick auf einen Marker bearbeitet ihn, Wegpunkt-Liste als Button.
- **Standard-Layout**: Elemente sitzen jetzt an Ankern – FPS/CPS/Ping/XYZ oben links, Richtung oben mittig, Minimap oben rechts, Keystrokes unten links, Rüstung unten rechts, Spotify über der Hotbar. Selten genutzte Elemente sind standardmäßig aus. „Zurücksetzen“ im Editor stellt das wieder her.

## Voice-Chat (0.9)

Eigener **Proximity-Voice-Chat** über den LellekVoiceServer – in jeder Version, ohne Mod auf dem Spielserver. Mod-Menü → Social → **Voice-Chat** einschalten, **B halten** zum Sprechen. Wer bis 48 Blöcke entfernt steht, hört dich, leiser mit der Entfernung. Lautstärke und Mikrofon-Verstärkung in den Einstellungen; das HUD-Element „Voice-Chat“ zeigt, wer gerade spricht und wie viele in Hörweite sind. Server-Adresse aus dem Launcher (Optionen) oder direkt im Feature.

Technik: Java-Sound (Standardmikrofon), µ-law 16 kHz, UDP. Telefonqualität, dafür keine nativen Bibliotheken. Wenn nichts zu hören ist: in Windows das richtige Standard-Mikrofon setzen und den UDP-Port des Servers freigeben.

## Cosmetics-Server (0.8)

Damit **andere LellekClient-Spieler dein Cosmetic sehen** (und du ihres), spricht die Mod mit dem Cosmetics-Server (eigenes Projekt `LellekCosmeticsServer`, ein Node-Skript). Login läuft wie ein Serverbeitritt über Mojang, niemand kann sich als jemand anderes ausgeben.

- Mod-Menü → Social → **Cosmetics-Sync**: Server-URL. Solange sie auf dem Standard steht, nimmt die Mod die Adresse aus dem Launcher (Optionen → Cosmetics-Server).
- Alle 30 Sekunden werden die Cosmetics der Spieler in der Tab-Liste geholt; dein eigenes wird beim Wechsel sofort gemeldet.
- **Eigenes Cape**: im Launcher unter Skins hochladen (PNG 64x32), dann in der Garderobe „Eigenes Cape“ wählen. Andere sehen es mit deinem Design.

## Karte, Wegpunkte & mehr (0.7)

**Eigenes Wegpunkt-System**: Taste **N** setzt einen Wegpunkt an deiner Position (Name, Koordinaten, Farbe per Farbwähler), **M** öffnet die Liste (sichtbar/unsichtbar, bearbeiten, im Chat teilen, löschen). In der Welt: Schild mit Name und Entfernung, das nie zu klein wird, plus optionaler Lichtstrahl. Gespeichert pro Server bzw. Welt und Dimension in `config/lellek-waypoints.json`.

**Eigene Minimap** (HUD-Element): Oberflächenkarte um dich herum aus den echten Blockfarben mit Relief-Schattierung, Norden oben, Spielerpfeil, Wegpunkte als Punkte, Koordinaten darunter. Zoom und Wegpunkt-Anzeige unter Mods → Karte → Minimap.

Für 26.2 stehen Block-Umrandung und Screenshot-Kopie im Menü auf „folgt“ (Render-Umbau). Weitere neue Features: **Richtung** (Kompassleiste mit Grad), **Perspektive/Freelook** (F6 halten, 360° um dich herum), **Schadensanzeige** (Zahlen über getroffenen Entities), **Block-Umrandung** (Farbe, Dicke, Füllung), **Sprint/Sneak-Anzeige**, **Item-Zähler** (Pfeile beim Bogen), **Auto-GG** (Auslöser, Text, Verzögerung), **Screenshot in Zwischenablage**. Wegpunkte haben auch ein Icon im ESC-Menü.

## Cosmetics-Garderobe & ESC-Menü (0.6)

- **ESC-Menü** trägt jetzt den LellekClient-Kopf: Schriftzug, „MOD-MENÜ“-Button und eine Icon-Leiste (HUD-Editor, Cosmetics, Spotify Play/Pause, Mods). Die Vanilla-Buttons rutschen darunter.
- **Cosmetics** sind aus dem Mod-Menü raus und haben eine eigene Garderobe (Mod-Menü → „✦ Cosmetics“ oder ESC → ✦): Kategorien links (Kopf, Rücken, Effekte), Suche, Karten mit Vorschaubild und „Anziehen“, rechts dein Spieler live mit dem Cosmetic – inklusive Animation. Das Rad auf X bleibt als Schnellwahl.

## Mod-Menü (0.5)

**Rechte Strg-Taste** (oder „Mod-Menü“ im HUD-Editor) öffnet das Mod-Menü im LellekClient-Look: Seitenleiste mit Kategorien, Suche, Karten mit animierten Schaltern. Klick auf eine Karte = an/aus, Zahnrad = Einstellungen (Regler, Auswahl, Farbwähler). Kategorien: Spiel, Welt, HUD, Chat, HUD-Elemente, Cosmetics.

Neue Features:

| Feature | Was | Einstellungen |
|---|---|---|
| ToggleSneak | Taste V schaltet Schleichen dauerhaft | – |
| Kein Schadens-Wackeln | Kamera bleibt bei Schaden ruhig | – |
| Tageszeit | Feste Tageszeit, nur für dich | Tag / Sonnenuntergang / Nacht / Sonnenaufgang |
| Immer klares Wetter | Regen und Gewitter ausblenden | – |
| Eigenes Fadenkreuz | Ersetzt das Vanilla-Fadenkreuz | Form (Kreuz/Punkt/Kreis/Plus), Größe, Dicke, Farbe – mit Vorschau |
| Chat-Zeitstempel | Uhrzeit vor jeder Nachricht | mit Sekunden |
| Reichweite (Element) | Distanz des letzten Treffers | Farbe, Größe, Position |
| Combo (Element) | Treffer in Folge, bricht bei eigenem Schaden ab | Farbe, Größe, Position |
| Server (Element) | Adresse des aktuellen Servers | Farbe, Größe, Position |
| Speicher (Element) | RAM-Verbrauch | Farbe, Größe, Position |

Zoom hat jetzt zusätzlich „Weiche Kamera“ als Option. Alle Features werden pro Profil gespeichert.

**Eigenes Feature hinzufügen:** in `feature/Mods.java` eine Zeile nach dem Muster der vorhandenen (Name, Beschreibung, Kategorie, Einstellungen), die Logik in `feature/Features.java`. Menü, Einstellungsscreen und Speicherung kommen automatisch.

## Bauen

Braucht **JDK 25** (Temurin 25 von adoptium.net) – nicht JDK 8 wie bei 1.8.9/1.12.2. Gradle 9.5.1 kommt über den Wrapper.

```
gradlew build          → build/libs/LellekHUD-26.2-1.1.0.jar
gradlew runClient      → Test-Minecraft mit der Mod
```

In VS Code: JDK-25-Pfad in `.vscode/tasks.json` und `settings.json` anpassen, dann Strg+Shift+B.

## Stand 26.2 (1.2)

Alles, was vorher auf „folgt“ stand, ist drin – ohne unsichere Mixins: Hitboxen und feste Tageszeit suchen sich ihre Methode per Reflection, die Block-Umrandung läuft über Fabric-Events, die Screenshot-Kopie über den Screenshot-Ordner, der Wegpunkt-Lichtstrahl und die Spielervorschau in der Garderobe sind zurück. Die Karte skaliert jetzt korrekt (vorher gekachelt).

**Ohne Server**: Cosmetics, Emotes (lokal), HUD und alle Mods funktionieren ohne Cosmetics- und Voice-Server. Ist der Server nicht erreichbar, wird das einmal geloggt und alle 5 Minuten neu probiert – keine Fehlerflut.

**Neu in 26.2**: Maustasten (LMB/RMB) als eigenes Element, im Keystrokes-Element abschaltbar. Zehn weitere Elemente/Features: Leben & Hunger, Rüstungswert, Biom, Lichtlevel, Item in Hand, Entfernung zum Ziel, Geschwindigkeit, Server-TPS, Stoppuhr (Taste Ende), Sitzung; dazu Chat-Erwähnung (Ton + Markierung), Auto-Respawn, Haltbarkeits-Warnung.

Die Zeile unten im Mod-Menü („Aktiv: … | Nicht gegriffen: …“) zeigt, welche Mixins in dieser 26.2-Version tatsächlich laufen.

## Was fehlt noch

- Cosmetics anderer Spieler: braucht den Cosmetics-Server (Spieler-UUID → Cosmetic-ID), `CosmeticLayer.others` ist dafür der Einstiegspunkt.
- Taskleisten-Icon: wird in 26.x über den Launcher gesetzt, nicht über die Mod.
