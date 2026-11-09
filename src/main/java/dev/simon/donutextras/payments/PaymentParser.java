package dev.simon.donutextras.payments;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects real incoming payment messages. If DonutSMP words the message differently,
 * change PAID_YOU below (group 1 = player name, group 2 = amount like 10M, 2.5m, 1,500).
 */
public final class PaymentParser {
	private static final Pattern PAID_YOU = Pattern.compile(
			"([A-Za-z0-9_.]{3,20})\\s+(?:has\\s+)?paid\\s+you\\s+\\$?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?\\s*[kKmMbBtT]?)");

	// The same payment can show up in chat and in the action bar; show it once.
	private static String lastKey = "";
	private static long lastTime = 0;

	private PaymentParser() {}

	public static void handle(String raw) {
		if (raw == null) return;
		Matcher m = PAID_YOU.matcher(raw);
		if (!m.find()) return;

		String name = m.group(1);
		String amount = m.group(2).replace(" ", "").replace(",", "").toUpperCase(Locale.ROOT);

		long now = System.currentTimeMillis();
		String key = name + "|" + amount;
		if (key.equals(lastKey) && now - lastTime < 1500) return;
		lastKey = key;
		lastTime = now;

		PaymentToasts.push(name, "+$" + amount);
	}
}
