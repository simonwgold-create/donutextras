package dev.simon.donutextras.voice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

/**
 * Gets the raw skin PNG for a player so a copy with an open mouth can be made.
 * Minecraft does not keep the original pixels in memory after uploading a skin to the GPU,
 * so the PNG is downloaded again from Mojang's skin server (read-only, once per player).
 */
final class SkinAccess {
	private static final HttpClient HTTP = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	private SkinAccess() {}

	/** Call on the main thread. Returns null if the player or skin URL is unknown. */
	static String skinUrl(UUID id) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.getConnection() == null) return null;
		PlayerInfo info = mc.getConnection().getPlayerInfo(id);
		if (info == null) return null;

		GameProfile profile = info.getProfile();
		for (Property prop : profile.properties().get("textures")) {
			try {
				String json = new String(Base64.getDecoder().decode(prop.value()), StandardCharsets.UTF_8);
				JsonObject skin = JsonParser.parseString(json).getAsJsonObject()
						.getAsJsonObject("textures").getAsJsonObject("SKIN");
				String url = skin.get("url").getAsString();
				URI uri = URI.create(url);
				// Only ever fetch from Mojang's texture server.
				if ("https".equals(uri.getScheme()) && uri.getHost() != null
						&& uri.getHost().endsWith("textures.minecraft.net")) {
					return url;
				}
			} catch (Exception ignored) {
				// malformed property, try the next one
			}
		}
		return null;
	}

	/** Blocking download, call from a worker thread. */
	static NativeImage download(String url) throws Exception {
		HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8)).GET().build();
		HttpResponse<InputStream> res = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());
		if (res.statusCode() != 200) throw new IllegalStateException("HTTP " + res.statusCode());
		try (InputStream in = res.body()) {
			return NativeImage.read(in);
		}
	}
}
