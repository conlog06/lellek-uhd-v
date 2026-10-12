package gg.lellek.hud.feature;

/** Einstellung eines Features – Bool, Zahl, Auswahl oder Farbe. Wird als String gespeichert. */
public abstract class Setting {
    public final String id, name;
    protected Setting(String id, String name) { this.id = id; this.name = name; }
    public abstract String get();
    public abstract void set(String v);

    public static class Bool extends Setting {
        public boolean value;
        public Bool(String id, String name, boolean v) { super(id, name); value = v; }
        public String get() { return String.valueOf(value); }
        public void set(String v) { value = Boolean.parseBoolean(v); }
    }
    public static class Int extends Setting {
        public int value, min, max; public String suffix;
        public Int(String id, String name, int v, int min, int max, String suffix) { super(id, name); value = v; this.min = min; this.max = max; this.suffix = suffix; }
        public String get() { return String.valueOf(value); }
        public void set(String v) { try { value = Math.max(min, Math.min(max, Integer.parseInt(v))); } catch (NumberFormatException ignored) {} }
    }
    public static class Choice extends Setting {
        public int index; public final String[] options;
        public Choice(String id, String name, int idx, String... options) { super(id, name); index = idx; this.options = options; }
        public String get() { return String.valueOf(index); }
        public void set(String v) { try { index = Math.max(0, Math.min(options.length - 1, Integer.parseInt(v))); } catch (NumberFormatException ignored) {} }
        public String current() { return options[index]; }
        public void next() { index = (index + 1) % options.length; }
    }
    public static class Text extends Setting {
        public String value;
        public Text(String id, String name, String v) { super(id, name); value = v; }
        public String get() { return value; }
        public void set(String v) { value = v == null ? "" : v; }
    }
    public static class Color extends Setting {
        public int value;
        public Color(String id, String name, int argb) { super(id, name); value = argb; }
        public String get() { return String.format("%08X", value); }
        public void set(String v) { try { value = (int) Long.parseLong(v, 16); } catch (NumberFormatException ignored) {} }
    }
}
