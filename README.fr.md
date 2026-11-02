<p align="center">
  <a href="https://chatbit.zap-info.com/fr/"><img width="160" height="160" alt="chatbit" src="https://chatbit.zap-info.com/icon-192.png"/></a>
</p>

<h1 align="center">chatbit</h1>

<p align="center">
  <b>La messagerie bitchat pour Android, avec Tor activé d'office, l'anti-flood et les salons privés.</b><br/>
  Le mesh Bluetooth quand il n'y a pas de réseau, Nostr via Tor quand il y en a. Sans compte, sans numéro de téléphone.
</p>

<p align="center">
  <a href="https://chatbit.zap-info.com/fr/"><b>Site et téléchargement</b></a> ·
  <a href="https://www.youtube.com/@chatbit-messagerie">YouTube</a> ·
  <a href="README.md">README in English</a>
</p>

---

chatbit est un fork de [bitchat-android](https://github.com/permissionlesstech/bitchat-android), la
messagerie décentralisée lancée par Jack Dorsey. Il reste compatible avec bitchat sur iPhone et Android
(mesh Bluetooth et salons géohash) et ajoute une couche de sécurité active dès le premier lancement,
sans rien régler.

## Télécharger

L'APK signé est sur **[chatbit.zap-info.com](https://chatbit.zap-info.com/fr/)**. La page donne les
empreintes SHA-256 et le rapport VirusTotal de chaque version.

Android 8.0 ou plus récent. L'APK publié sur le site n'affiche aucune publicité.

## Captures

<table>
  <tr>
    <td><img src="https://chatbit.zap-info.com/img/filtres.webp" alt="Réglages anti-spam : anti-flood, anti-bot, anti-spam de liens" width="260"/></td>
    <td><img src="https://chatbit.zap-info.com/img/securite.webp" alt="Réglages de sécurité actifs par défaut" width="260"/><br/><br/>
        <img src="https://chatbit.zap-info.com/img/theme.webp" alt="Couleurs d'accent et thème" width="260"/></td>
  </tr>
</table>

## Ce que chatbit ajoute à bitchat

**Confidentialité par défaut**
- Tor (Arti) actif dès le premier lancement, rotation automatique de la route et bouton « nouvelle route »
- Coupure des messages si Tor tombe, plutôt qu'une fuite sur le réseau en clair
- Liens ouverts uniquement dans Tor Browser
- Captures d'écran bloquées, notifications masquées, presse-papiers vidé après 60 secondes, métadonnées des photos (EXIF) retirées
- Clavier incognito, messages éphémères (24 h), pas d'annonce de présence

**Protection du téléphone**
- Verrou par code PIN chatbit sur son propre pavé (jamais le clavier système)
- Code de contrainte : le saisir efface tout, en silence
- Préférences chiffrées et coffre de médias chiffré, dont la clé est détruite à l'effacement
- Effacement automatique après un nombre de jours d'inactivité choisi

**Des salons publics propres**
- Anti-flood de base toujours actif : texte répété, clés jetables, pseudos générés (bot_xxxx), spam de liens, texte Zalgo
- Mode anti-spam complet avec seuils et règles réglables, et liste des expéditeurs masqués
- Import et export des listes de blocage (txt / json)

**Plus de façons de discuter**
- Salons privés chiffrés de bout en bout, transportés par Nostr et Tor, rejoints par lien ou QR code d'invitation
- Globe interactif des salons géohash, avec un annuaire des zones hors ligne
- Partage hors ligne : un point d'accès Wi-Fi sert l'appli aux personnes autour, sans Internet
- 34 langues, 8 couleurs d'accent, thèmes clair et sombre, visite guidée avec la chatonne chatbit

## Hérité de bitchat

- **Mesh Bluetooth LE** : relais de téléphone en téléphone (jusqu'à 7 sauts), sans antenne ni serveur
- **Nostr** : salons géohash et messages privés via des relais publics quand Internet est disponible
- **Chiffrement de bout en bout** : [Noise Protocol](https://noiseprotocol.org) (XX, X25519 + ChaCha20-Poly1305) pour les messages privés
- **Preuve de travail** (NIP-13) sur les messages envoyés, pour rendre le spam coûteux
- **Effacement d'urgence** : triple appui pour tout effacer
- Commandes façon IRC (`/join`, `/msg`, `/who`…), plus les formes courtes de chatbit (`/j`, `/m`, `/w`…)

## Compiler depuis les sources

Il faut le SDK Android (API 26+) et le JDK 21.

```bash
git clone https://github.com/ledokter/chatbit.git
cd chatbit
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Une compilation faite à partir de ce seul dépôt est **sans marque** : elle s'appelle « Chat (unbranded) »
et utilise une simple icône de bulle. Le nom chatbit, la chatonne et les icônes chatbit ne font pas
partie de ce dépôt (voir [TRADEMARKS.md](TRADEMARKS.md)).

## Vie privée

chatbit n'a ni compte ni numéro de téléphone. Voir [docs/chatbit/PRIVACY.md](docs/chatbit/PRIVACY.md).

## Licence

chatbit est un logiciel libre sous **licence publique générale GNU v3**, comme le code dont il dérive.
Voir [LICENSE.md](LICENSE.md). Le nom et les visuels chatbit relèvent de [TRADEMARKS.md](TRADEMARKS.md).

Construit sur [bitchat](https://github.com/permissionlesstech/bitchat) et
[bitchat-android](https://github.com/permissionlesstech/bitchat-android). Merci à leurs auteurs.
