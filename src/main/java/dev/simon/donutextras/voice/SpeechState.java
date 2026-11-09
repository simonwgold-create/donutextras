package dev.simon.donutextras.voice;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe record of who spoke recently. Voice packets arrive on audio threads, rendering reads on the main thread. */
public final class SpeechState {
	/** Mouth stays open this long after the last loud packet (packets are about 20 ms apart). */
	private static final long HOLD_MS = 120;

	private static final Map<UUID, Long> LAST_LOUD = new ConcurrentHashMap<>();

	private SpeechState() {}

	public static void report(UUID id, double level, double threshold) {
		if (id != null && level >= threshold) {
			LAST_LOUD.put(id, System.currentTimeMillis());
		}
	}

	public static boolean isTalking(UUID id) {
		Long t = LAST_LOUD.get(id);
		return t != null && System.currentTimeMillis() - t < HOLD_MS;
	}

	public static void clear() {
		LAST_LOUD.clear();
	}
}
