package dev.simon.donutextras;

import dev.simon.donutextras.payments.PaymentParser;
import dev.simon.donutextras.payments.PaymentToasts;
import dev.simon.donutextras.voice.MouthSkins;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public class DonutExtrasClient implements ClientModInitializer {
	public static final String MOD_ID = "donutextras";

	@Override
	public void onInitializeClient() {
		// Read-only listeners: they never cancel or edit chat, they only watch for real
		// "<name> paid you $<amount>" messages the server already sent to you.
		ClientReceiveMessageEvents.GAME.register((message, overlay) ->
				PaymentParser.handle(message.getString()));
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
				PaymentParser.handle(message.getString()));

		// Drop cached mouth textures when leaving a server.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MouthSkins.clear());

		// Draw the popups on top of everything else on the HUD.
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(MOD_ID, "payment_toasts"),
				PaymentToasts::render);
	}
}
