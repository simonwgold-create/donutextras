package dev.simon.donutextras.voice;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Swaps a player's skin for a copy with an open mouth while they talk.
 * Purely visual and local: only your own game renders the modified skin.
 */
public final class MouthSkins {
	// Mouth pixels on the 64x64 skin: the head front face is x 8..15, y 8..15.
	private static final int MOUTH_X1 = 10, MOUTH_X2 = 13, MOUTH_Y = 13;
	private static final int OPEN_COLOR = 0xFF3B0D0D;
	// How different the mouth pixels must look from the cheeks to count as "has a mouth".
	private static final int MIN_CONTRAST = 18;

	private static final class Entry {
		volatile boolean started;
		volatile boolean hasMouth;
		volatile Identifier openTexture;
		PlayerSkin lastOriginal;
		PlayerSkin lastOpen;
	}

	private static final Map<UUID, Entry> ENTRIES = new ConcurrentHashMap<>();

	private MouthSkins() {}

	/** Called from the getSkin mixin, so it runs every frame for every visible player. Keep it cheap. */
	public static PlayerSkin apply(UUID id, PlayerSkin original) {
		if (id == null || original == null) return original;

		Entry e = ENTRIES.computeIfAbsent(id, k -> new Entry());
		if (!e.started) {
			e.started = true;
			String url = SkinAccess.skinUrl(id);
			if (url != null) prepare(id, e, url);
		}

		Identifier open = e.openTexture;
		if (open == null || !e.hasMouth || !SpeechState.isTalking(id)) return original;

		if (e.lastOriginal != original || e.lastOpen == null) {
			e.lastOriginal = original;
			e.lastOpen = new PlayerSkin(
					new ClientAsset.ResourceTexture(open, open),
					original.cape(),
					original.elytra(),
					original.model(),
					original.secure());
		}
		return e.lastOpen;
	}

	public static void clear() {
		Minecraft mc = Minecraft.getInstance();
		for (Entry e : ENTRIES.values()) {
			Identifier t = e.openTexture;
			if (t != null) mc.execute(() -> mc.getTextureManager().release(t));
		}
		ENTRIES.clear();
		SpeechState.clear();
	}

	private static void prepare(UUID id, Entry e, String url) {
		CompletableFuture.supplyAsync(() -> {
			try {
				return SkinAccess.download(url);
			} catch (Exception ex) {
				return null;
			}
		}).thenAccept(base -> {
			if (base == null) return;
			// Textures must be created on the render thread.
			Minecraft.getInstance().execute(() -> build(id, e, base));
		});
	}

	private static void build(UUID id, Entry e, NativeImage base) {
		try {
			// Old 64x32 skins get converted by the game; a raw copy would render wrong, so skip them.
			if (base.getWidth() != 64 || base.getHeight() != 64 || !skinHasMouth(base)) {
				base.close();
				return;
			}
			NativeImage open = new NativeImage(64, 64, true);
			open.copyFrom(base);
			base.close();

			for (int x = MOUTH_X1; x <= MOUTH_X2; x++) {
				paint(open, x, MOUTH_Y);
				if (x > MOUTH_X1 && x < MOUTH_X2) paint(open, x, MOUTH_Y + 1);
			}

			Identifier tex = Identifier.fromNamespaceAndPath("donutextras", "mouth/" + id);
			Minecraft.getInstance().getTextureManager()
					.register(tex, new DynamicTexture(() -> "donutextras mouth " + id, open));
			e.hasMouth = true;
			e.openTexture = tex;
		} catch (Exception ex) {
			base.close();
		}
	}

	/** Paints one mouth pixel on the face layer, and on the hat layer too if the skin has a hat there. */
	private static void paint(NativeImage img, int x, int y) {
		img.setPixel(x, y, OPEN_COLOR);
		int hatX = x + 32; // hat layer sits 32 px to the right of the face layer
		if (((img.getPixel(hatX, y) >>> 24) & 0xFF) > 0) {
			img.setPixel(hatX, y, OPEN_COLOR);
		}
	}

	/**
	 * Heuristic: the skin "has a mouth" when the mouth row is clearly darker or lighter than the
	 * cheek pixels beside it. Plain faces with no mouth drawn are left alone.
	 */
	private static boolean skinHasMouth(NativeImage img) {
		int mouth = 0;
		int n = 0;
		for (int x = MOUTH_X1; x <= MOUTH_X2; x++) {
			mouth += luminance(img.getPixel(x, MOUTH_Y));
			n++;
		}
		mouth /= n;
		int cheeks = (luminance(img.getPixel(MOUTH_X1 - 1, MOUTH_Y))
				+ luminance(img.getPixel(MOUTH_X2 + 1, MOUTH_Y))
				+ luminance(img.getPixel(MOUTH_X1, MOUTH_Y - 1))
				+ luminance(img.getPixel(MOUTH_X2, MOUTH_Y - 1))) / 4;
		return Math.abs(mouth - cheeks) >= MIN_CONTRAST;
	}

	private static int luminance(int argb) {
		int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
		return (r * 299 + g * 587 + b * 114) / 1000;
	}
}
