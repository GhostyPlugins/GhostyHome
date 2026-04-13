package de.ghosty.ghostyhome.manager;

import de.ghosty.ghostyhome.GhostyHome;
import de.ghosty.ghostyhome.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class LangManager {

    private final GhostyHome  plugin;
    private FileConfiguration langConfig;
    private String            language;

    public LangManager(GhostyHome plugin) { this.plugin = plugin; reload(); }

    public void reload() {
        language = plugin.getConfig().getString("language", "de_DE");
        loadLanguage(language);
    }

    private void loadLanguage(String lang) {
        saveDefaultLang("de_DE");
        saveDefaultLang("en_EN");

        File langFolder = new File(plugin.getDataFolder(), "lang");
        File langFile   = new File(langFolder, lang + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + lang + ".yml' not found! Falling back to de_DE.");
            langFile = new File(langFolder, "de_DE.yml");
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);

        InputStream defStream = getDefaultStream(lang);
        if (defStream == null) defStream = getDefaultStream("de_DE");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(defConfig);
        }
    }

    private void saveDefaultLang(String lang) {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) langFolder.mkdirs();
        File langFile = new File(langFolder, lang + ".yml");
        if (!langFile.exists()) {
            InputStream stream = getDefaultStream(lang);
            if (stream != null) {
                try (OutputStream out = new FileOutputStream(langFile)) {
                    byte[] buf = new byte[1024]; int len;
                    while ((len = stream.read(buf)) > 0) out.write(buf, 0, len);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error saving language file: " + lang, e);
                }
            }
        }
    }

    private InputStream getDefaultStream(String lang) {
        return plugin.getResource("lang/" + lang + ".yml");
    }

    public String get(String key, Object... placeholders) {
        String msg = langConfig.getString(key, "&cMissing lang key: &f" + key);
        for (int i = 0; i < placeholders.length; i++)
            msg = msg.replace("{" + i + "}", String.valueOf(placeholders[i]));
        return ColorUtil.colorize(msg);
    }
    public String getPrefix() { return get("prefix"); }
    public String getPrefixed(String key, Object... placeholders) {
        return getPrefix() + get(key, placeholders);
    }
    public String getLanguage() { return language; }
}
