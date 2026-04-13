package de.ghosty.ghostyhome.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_AMP   = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_BRACE = Pattern.compile("\\{#([A-Fa-f0-9]{6})}");
    private static final Pattern HEX_ANGLE = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public static String colorize(String text) {
        if (text == null) return "";
        text = replaceHex(HEX_AMP,   text);
        text = replaceHex(HEX_BRACE, text);
        text = replaceHex(HEX_ANGLE, text);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String replaceHex(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) m.appendReplacement(sb, ChatColor.of("#" + m.group(1)).toString());
        m.appendTail(sb);
        return sb.toString();
    }

    public static String strip(String text) {
        return ChatColor.stripColor(colorize(text));
    }
}
