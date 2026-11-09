package dev.simon.donutextras.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * Every call into Minecraft's GUI drawing code lives in this one file.
 * Mojang renamed a lot of GUI classes around 26.1, so if the build complains about
 * a drawing method, this is the only file you need to fix.
 */
public final class Draw {
	private Draw() {}

	public static int scaledWidth(Minecraft mc) {
		return mc.getWindow().getGuiScaledWidth();
	}

	public static int textWidth(Font font, String s) {
		return font.width(s);
	}

	public static void pushScale(GuiGraphicsExtractor g, float s) {
		g.pose().pushMatrix();
		g.pose().scale(s, s);
	}

	public static void translate(GuiGraphicsExtractor g, float x, float y) {
		g.pose().translate(x, y);
	}

	public static void popScale(GuiGraphicsExtractor g) {
		g.pose().popMatrix();
	}

	public static void fill(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int argb) {
		g.fill(x1, y1, x2, y2, argb);
	}

	public static void text(GuiGraphicsExtractor g, Font font, String s, int x, int y, int argb) {
		g.text(font, s, x, y, argb, true); // true = drop shadow, like vanilla text
	}

	/** Draws the player's head (face + hat layer) for the given player name. */
	public static void face(GuiGraphicsExtractor g, Minecraft mc, String playerName, int x, int y, int size) {
		PlayerSkin skin = null;
		if (mc.getConnection() != null) {
			PlayerInfo info = mc.getConnection().getPlayerInfo(playerName);
			if (info != null) skin = info.getSkin();
		}
		if (skin == null) skin = DefaultPlayerSkin.getDefaultSkin();
		PlayerFaceExtractor.extractRenderState(g, skin, x, y, size);
	}
}
