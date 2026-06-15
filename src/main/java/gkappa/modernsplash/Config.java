package gkappa.modernsplash;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import cpw.mods.fml.client.FMLClientHandler;

public class Config {

    private static Configuration config;

    public static boolean enabled;
    public static boolean forgeLogo;
    public static boolean rotate;
    public static int logoOffset;
    public static int backgroundColor;
    public static int fontColor;
    public static int logoColor;
    public static int barBorderColor;
    public static int barColor;
    public static int barBackgroundColor;
    public static boolean showMemory;
    public static boolean showTotalMemoryLine;
    public static boolean displayStartupTimeOnMainMenu = true;
    public static boolean enableTimer = true;
    public static int memoryGoodColor;
    public static int memoryWarnColor;
    public static int memoryLowColor;

    public static String fontTexture;
    public static String forgeTexture;
    public static String resourcePackPath;

    public static ResourceLocation fontLoc;
    public static ResourceLocation forgeLoc;
    public static File miscPackFile;

    public static void load() {
        File configFile = new File(Minecraft.getMinecraft().mcDataDir, "config/modernsplash.cfg");
        config = new Configuration(configFile);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
        int now = Integer.parseInt(formatter.format(LocalDateTime.now()));

        enabled = config.getBoolean("enabled", "splash", true, "Enable the custom splash screen")
            && ((!FMLClientHandler.instance()
                .hasOptifine()) || Launch.blackboard.containsKey("optifine.ForgeSplashCompatible"));
        rotate = config.getBoolean("rotate", "splash", false, "Rotate the Forge logo");
        forgeLogo = config.getBoolean("forgeLogo", "splash", false, "Show the Forge logo");
        showMemory = config.getBoolean("showMemory", "splash", true, "Show the memory usage bar");
        showTotalMemoryLine = config
            .getBoolean("showTotalMemoryLine", "splash", false, "Show the total memory line on the memory bar");
        enableTimer = config.getBoolean("enableTimer", "splash", true, "Show the startup timer");
        displayStartupTimeOnMainMenu = config
            .getBoolean("timeOnMainMenu", "splash", true, "Show startup time on the main menu");

        logoOffset = config.getInt("logoOffset", "splash", 0, -999, 999, "Logo offset in pixels");

        backgroundColor = getConfigColor("background", 0xEF323D, "Background color (hex RRGGBB)");
        fontColor = getConfigColor("font", 0xFFFFFF, "Font color (hex RRGGBB)");
        logoColor = getConfigColor("logo", 0xFFFFFF, "Logo color (hex RRGGBB)");
        barBorderColor = getConfigColor("barBorder", 0xFFFFFF, "Progress bar border color (hex RRGGBB)");
        barColor = getConfigColor("bar", 0xFFFFFF, "Progress bar fill color (hex RRGGBB)");
        barBackgroundColor = getConfigColor("barBackground", 0xEF323D, "Progress bar background color (hex RRGGBB)");
        memoryGoodColor = getConfigColor("memoryGood", 0xFFFFFF, "Memory bar color (good, hex RRGGBB)");
        memoryWarnColor = getConfigColor("memoryWarn", 0xFFFFFF, "Memory bar color (warn, hex RRGGBB)");
        memoryLowColor = getConfigColor("memoryLow", 0xFFFFFF, "Memory bar color (low, hex RRGGBB)");

        boolean darkModeOnly = config
            .getBoolean("darkModeOnly", "splash", false, "Force dark mode always (ignores time range)");

        int darkStartTime = config.getInt("darkStartTime", "splash", 2300, 0, 2359, "Dark mode start time (HHmm)");
        int darkEndTime = config.getInt("darkEndTime", "splash", 600, 0, 2359, "Dark mode end time (HHmm)");

        int backgroundColorNight = getConfigColor(
            "backgroundDark",
            0x202020,
            "Background color night mode (hex RRGGBB)");
        int fontColorNight = getConfigColor("fontDark", 0x606060, "Font color night mode (hex RRGGBB)");
        int logoColorNight = getConfigColor("logoDark", 0x999999, "Logo color night mode (hex RRGGBB)");
        int barBorderColorNight = getConfigColor(
            "barBorderDark",
            0x4E4E4E,
            "Progress bar border color night mode (hex RRGGBB)");
        int barColorNight = getConfigColor("barDark", 0x4E4E4E, "Progress bar fill color night mode (hex RRGGBB)");
        int barBackgroundColorNight = getConfigColor(
            "barBackgroundDark",
            0x202020,
            "Progress bar background color night mode (hex RRGGBB)");
        int memoryGoodColorNight = getConfigColor(
            "memoryGoodDark",
            0x4E4E4E,
            "Memory bar color night mode (good, hex RRGGBB)");
        int memoryWarnColorNight = getConfigColor(
            "memoryWarnDark",
            0x4E4E4E,
            "Memory bar color night mode (warn, hex RRGGBB)");
        int memoryLowColorNight = getConfigColor(
            "memoryLowDark",
            0x4E4E4E,
            "Memory bar color night mode (low, hex RRGGBB)");

        if (darkModeOnly || (darkEndTime >= darkStartTime ? (now >= darkStartTime && now < darkEndTime)
            : (now >= darkStartTime || now <= darkEndTime))) {
            backgroundColor = backgroundColorNight;
            fontColor = fontColorNight;
            logoColor = logoColorNight;
            barBorderColor = barBorderColorNight;
            barColor = barColorNight;
            barBackgroundColor = barBackgroundColorNight;
            memoryGoodColor = memoryGoodColorNight;
            memoryWarnColor = memoryWarnColorNight;
            memoryLowColor = memoryLowColorNight;
        }

        fontTexture = config
            .getString("fontTexture", "splash", "textures/font/ascii.png", "Font texture resource location");
        forgeTexture = config
            .getString("forgeTexture", "splash", "fml:textures/gui/forge.gif", "Forge logo texture resource location");
        resourcePackPath = config.getString("resourcePackPath", "splash", "resources", "Resource pack directory path");

        fontLoc = new ResourceLocation(fontTexture);
        forgeLoc = new ResourceLocation(forgeTexture);
        miscPackFile = new File(Minecraft.getMinecraft().mcDataDir, resourcePackPath);

        config.save();
    }

    public static void disableSplash() {
        enabled = false;
        config.get("splash", "enabled", true, "Enable the custom splash screen")
            .setValue(false);
        config.save();
    }

    private static int getConfigColor(String name, int def, String comment) {
        Property prop = config.get(
            "splash",
            name,
            "0x" + Integer.toString(def, 16)
                .toUpperCase(),
            comment);
        return Integer.decode(prop.getString());
    }
}
