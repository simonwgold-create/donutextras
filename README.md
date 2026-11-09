# Donut Extras (client-side Fabric mod, Minecraft 26.2)

Everything runs on your own client. Nothing is installed on or sent to the server.

## Features
- **Payment popups**: when the server tells you "<name> paid you $<amount>", a popup slides in at the top right
  with the player's head, their whole name in green and the amount in green. It is bigger than the original
  (change `SCALE` in `PaymentToasts.java`). It only reacts to real messages, it never creates payments.
- **Voice mouth**: with Simple Voice Chat installed, your skin (and other players you hear) gets an open mouth
  while talking. Only you see it. Skins with no mouth drawn on the face are left alone.

## Build (needs JDK 25)
1. Copy `gradlew`, `gradlew.bat` and the `gradle/` folder from the official Fabric example mod into this folder
   (or run `gradle wrapper` if you have Gradle 9 installed).
2. `./gradlew build` (Windows: `gradlew.bat build`)
3. Put `build/libs/donutextras-1.0.0.jar` in your mods folder together with Fabric API and Simple Voice Chat.

## If the build fails
- Check `gradle.properties` against https://fabricmc.net/develop (loader, Loom, Fabric API versions).
- Drawing code is isolated in `render/Draw.java`. Mouth skin code is in `voice/MouthSkins.java` and
  `mixin/AbstractClientPlayerMixin.java`. Those are the files most likely to need small renames on 26.2.
- Payment message not detected? Edit the `PAID_YOU` regex in `payments/PaymentParser.java`.
- Mouth opens from background noise? Raise `THRESHOLD` in `voice/VoiceMouthPlugin.java`.

Check DonutSMP's rules on client mods before using it on the server.
