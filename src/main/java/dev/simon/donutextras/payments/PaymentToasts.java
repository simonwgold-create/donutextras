package dev.simon.donutextras.payments;

import dev.simon.donutextras.render.Draw;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/** The top-right payment popups: player head, green name, green +$amount. */
public final class PaymentToasts {
	// ---- Tweak these ----
	private static final float SCALE = 1.7f;          // original DonutSMP size is about 1.0, this is "bigger"
	private static final long LIFETIME_MS = 5000;
	private static final long SLIDE_MS = 250;
	private static final int MAX_VISIBLE = 5;
	private static final int NAME_COLOR = 0xFF55FF55;   // whole name green
	private static final int AMOUNT_COLOR = 0xFF55FF55;
	private static final int ACCENT_COLOR = 0xFF55FF55; // left bar
	private static final int BACKGROUND = 0xCC101010;

	// ---- Layout in unscaled units ----
	private static final int HEAD = 24;
	private static final int PAD = 5;
	private static final int HEIGHT = HEAD + PAD * 2;
	private static final int GAP = 4;
	private static final int BAR = 2;

	private record Toast(String name, String amount, long created) {}

	private static final CopyOnWriteArrayList<Toast> TOASTS = new CopyOnWriteArrayList<>();

	private PaymentToasts() {}

	public static void push(String name, String amount) {
		TOASTS.add(0, new Toast(name, amount, System.currentTimeMillis()));
		while (TOASTS.size() > MAX_VISIBLE) TOASTS.remove(TOASTS.size() - 1);
	}

	/** Matches HudElement#extractRenderState(GuiGraphicsExtractor, DeltaTracker). */
	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		if (TOASTS.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		long now = System.currentTimeMillis();

		int screenW = Draw.scaledWidth(mc);
		float y = 6; // in scaled units

		for (Iterator<Toast> it = TOASTS.iterator(); it.hasNext(); ) {
			Toast t = it.next();
			long age = now - t.created();
			if (age > LIFETIME_MS) {
				TOASTS.remove(t);
				continue;
			}

			int textW = Math.max(Draw.textWidth(font, t.name()), Draw.textWidth(font, t.amount()));
			int width = BAR + PAD + HEAD + PAD + textW + PAD * 2;

			// Slide in from the right, slide out at the end.
			float slide = 1f;
			if (age < SLIDE_MS) slide = age / (float) SLIDE_MS;
			else if (age > LIFETIME_MS - SLIDE_MS) slide = (LIFETIME_MS - age) / (float) SLIDE_MS;
			slide = 1f - (1f - slide) * (1f - slide); // ease-out

			float x = screenW / SCALE - width * slide - 4;

			Draw.pushScale(g, SCALE);
			Draw.translate(g, x, y);

			Draw.fill(g, 0, 0, width, HEIGHT, BACKGROUND);
			Draw.fill(g, 0, 0, BAR, HEIGHT, ACCENT_COLOR);

			Draw.face(g, mc, t.name(), BAR + PAD, PAD, HEAD);
			int textX = BAR + PAD + HEAD + PAD;
			Draw.text(g, font, t.name(), textX, PAD + 3, NAME_COLOR);
			Draw.text(g, font, t.amount(), textX, PAD + 3 + 11, AMOUNT_COLOR);

			Draw.popScale(g);

			// x/y are in scaled units (after Draw.pushScale), so stacking is a plain add.
			y += HEIGHT + GAP;
		}
	}
}
