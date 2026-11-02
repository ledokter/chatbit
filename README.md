<p align="center">
  <a href="https://chatbit.zap-info.com/en/"><img width="160" height="160" alt="chatbit" src="https://chatbit.zap-info.com/icon-192.png"/></a>
</p>

<h1 align="center">chatbit</h1>

<p align="center">
  <b>The bitchat messenger for Android, with Tor on by default, anti-flood and private rooms.</b><br/>
  Bluetooth mesh when there is no network, Nostr over Tor when there is. No account, no phone number.
</p>

<p align="center">
  <a href="https://chatbit.zap-info.com/en/"><b>Website and download</b></a> ·
  <a href="https://chatbit.zap-info.com/fr/">Site en français</a> ·
  <a href="https://www.youtube.com/@chatbit-messagerie">YouTube</a> ·
  <a href="README.fr.md">README en français</a>
</p>

---

chatbit is a fork of [bitchat-android](https://github.com/permissionlesstech/bitchat-android), the
decentralized messenger started by Jack Dorsey. It stays compatible with bitchat on iOS and Android
(Bluetooth mesh and geohash channels), and adds a security layer that is switched on from the first
launch, with nothing to configure.

## Download

Get the signed APK from **[chatbit.zap-info.com](https://chatbit.zap-info.com/en/)**. The page lists the
SHA-256 checksums and the VirusTotal report of each release.

Requires Android 8.0 or newer. The APK published on the website shows no ads.

## Screenshots

<table>
  <tr>
    <td><img src="https://chatbit.zap-info.com/img/filtres.webp" alt="Anti-spam settings: anti-flood, anti-bot and link spam filters" width="260"/></td>
    <td><img src="https://chatbit.zap-info.com/img/securite.webp" alt="Security settings enabled by default" width="260"/><br/><br/>
        <img src="https://chatbit.zap-info.com/img/theme.webp" alt="Accent colours and theme" width="260"/></td>
  </tr>
</table>

## What chatbit adds to bitchat

**Privacy by default**
- Tor (Arti) on from the first launch, with automatic route rotation and a one-tap "new route" button
- Messages are cut off if Tor drops, instead of leaking over the clear network
- Links open only in Tor Browser
- Screenshots blocked, notifications hidden, clipboard cleared after 60 seconds, photo metadata (EXIF) removed
- Incognito keyboard, ephemeral messages (24 h), no presence broadcast

**Protection of your phone**
- chatbit PIN lock on its own keypad (never the system keyboard)
- Duress PIN: entering it silently wipes everything
- Encrypted preferences and encrypted media vault, whose key is destroyed on wipe
- Automatic wipe after a chosen number of days without use

**Clean public channels**
- Base anti-flood always on: same text repeated, throwaway keys, generated nicknames (bot_xxxx), link spam, Zalgo text
- Full anti-spam mode with adjustable thresholds and rules, and a list of hidden senders
- Import and export of block lists (txt / json)

**More ways to talk**
- Private rooms encrypted end to end, carried over Nostr and Tor, joined with an invitation link or QR code
- Interactive globe of geohash channels, with an offline zone directory
- Offline sharing: a Wi-Fi hotspot serves the app to people nearby, with no Internet needed
- 34 languages, 8 accent colours, light and dark themes, a guided tour with the chatbit kitten

## Inherited from bitchat

- **Bluetooth LE mesh**: multi-hop relay between nearby phones (up to 7 hops), works with no antenna and no server
- **Nostr**: geohash location channels and private messages over public relays when the Internet is available
- **End-to-end encryption**: [Noise Protocol](https://noiseprotocol.org) (XX, X25519 + ChaCha20-Poly1305) for private messages
- **Proof of work** (NIP-13) on outgoing messages to make spam costly
- **Emergency wipe**: triple-tap to clear all data
- IRC-style commands (`/join`, `/msg`, `/who`…), plus chatbit short forms (`/j`, `/m`, `/w`…)

## Building from source

Requires the Android SDK (API 26+) and JDK 21.

```bash
git clone https://github.com/ledokter/chatbit.git
cd chatbit
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

A build made from this repository alone is **unbranded**: it is called "Chat (unbranded)" and uses a
plain chat bubble icon. The chatbit name, the kitten mascot and the chatbit icons are not part of this
repository (see [TRADEMARKS.md](TRADEMARKS.md)).

Tests:

```bash
./gradlew test                  # unit tests
./gradlew lint
./gradlew connectedAndroidTest  # needs a device or emulator
```

Bluetooth mesh behaviour is hard to emulate: protocol and session logic are covered by unit tests,
radio behaviour needs real phones.

## Privacy

chatbit has no account and no phone number. See [docs/chatbit/PRIVACY.md](docs/chatbit/PRIVACY.md).

## License

chatbit is free software under the **GNU General Public License v3**, like the code it derives from.
See [LICENSE.md](LICENSE.md). The chatbit name and visuals are covered by [TRADEMARKS.md](TRADEMARKS.md).

Built on [bitchat](https://github.com/permissionlesstech/bitchat) and
[bitchat-android](https://github.com/permissionlesstech/bitchat-android). Thanks to their authors.
