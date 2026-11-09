package dev.simon.donutextras.voice;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import net.minecraft.client.Minecraft;

/**
 * Loaded by Simple Voice Chat itself (the "voicechat" entrypoint in fabric.mod.json).
 * Everything here runs on your client only. No data is sent anywhere.
 */
public class VoiceMouthPlugin implements VoicechatPlugin {
	// Audio level (0..1) that counts as "talking". Raise it if background noise opens your mouth.
	private static final double THRESHOLD = 0.02;

	@Override
	public String getPluginId() {
		return "donutextras";
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		// Your own microphone, fired before the audio is encoded and sent.
		registration.registerEvent(ClientSoundEvent.class, event -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;
			SpeechState.report(mc.player.getUUID(), level(event.getRawAudio()), THRESHOLD);
		});

		// Other players you hear, so you also see their mouths move. Only entity (proximity) audio has an owner.
		registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, event ->
				SpeechState.report(event.getId(), level(event.getRawAudio()), THRESHOLD));
	}

	/** RMS loudness of a 16-bit PCM buffer, 0..1. */
	private static double level(short[] pcm) {
		if (pcm == null || pcm.length == 0) return 0;
		double sum = 0;
		for (short s : pcm) {
			double v = s / 32768.0;
			sum += v * v;
		}
		return Math.sqrt(sum / pcm.length);
	}
}
