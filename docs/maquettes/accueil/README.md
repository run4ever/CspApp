# Handoff — Écran d'accueil « Cyclo Sport Pantin »

> **Pour Claude Code.** Ce dossier décrit l'écran d'accueil d'une application mobile du club
> Cyclo Sport Pantin, à intégrer dans un projet **Kotlin Multiplatform / Compose Multiplatform**.
> Lis ce README en entier avant de coder : il contient tous les tokens, mesures et le mapping
> composant → Composable.

---

## ⚠️ À lire d'abord — nature des fichiers fournis

Les fichiers du dossier `reference_html/` sont une **maquette de référence réalisée en HTML/React**
(un prototype qui montre l'apparence et le comportement voulus). **Ce n'est PAS du code à copier
tel quel.** Ta mission est de **recréer fidèlement cette maquette en Compose Multiplatform**, en
suivant les patterns et la structure déjà en place dans le projet KMP de l'utilisateur (thème,
navigation, organisation des modules, design system éventuel).

- Si le projet a déjà un thème Compose (`MaterialTheme` custom, fichier de couleurs, typo) :
  **réutilise-le** et mappe les tokens ci-dessous dessus plutôt que de tout redéfinir.
- Si rien n'existe encore : crée un petit thème dédié à partir des tokens fournis (un starter
  `CspTheme.kt` est proposé plus bas, à adapter).

**Fidélité : haute (hi-fi).** Couleurs, typographie, espacements et rayons sont définitifs —
vise un rendu au pixel près. Le `commonMain` Compose est la cible recommandée (UI partagée
Android + iOS), mais le client est **majoritairement Android**.

---

## Vue d'ensemble

Écran d'accueil, **thème sombre**, identité du club (rouge `#F5333F` + cyan `#4CCBEE`, repris du logo).
De haut en bas :

1. **Salutation** dynamique (« Bonjour » avant 16 h, « Bonsoir » à partir de 16 h) + sous-titre.
2. **Carte « Prochaine sortie »** mise en avant (haut rouge, bas cyan, filigrane « CSP »).
3. **Liste « Sorties suivantes »** : une timeline verticale de 3 événements, chacun cliquable.
4. **Barre de navigation basse** : Accueil / Événements / Profil.

Un tap sur la carte mise en avant **ou** sur un item de la liste ouvre l'écran de détail de
l'événement (l'écran de détail est présent dans la maquette HTML mais **hors périmètre de ce
handoff** — on se concentre sur l'accueil ; la navigation doit juste être câblée).

---

## Design tokens

### Couleurs (à porter en `androidx.compose.ui.graphics.Color`)

| Token        | Hex / rgba                 | Usage |
|--------------|----------------------------|-------|
| `red`        | `#F5333F`                  | Accent principal, haut de la carte une, onglet actif, croix annulé |
| `redDeep`    | `#D21F2B`                  | Dégradé rouge (fin), texte sur bouton blanc |
| `redSoft`    | `rgba(245,51,63,0.14)`     | Fonds de pastilles rouges |
| `blue`       | `#4CCBEE`                  | Cyan du logo : bas de la carte une, point timeline, liens |
| `blueDeep`   | `#1FA9D2`                  | Cyan foncé (réserve) |
| `green`      | `#3DD68C`                  | Statut « Ouvert » |
| `bg`         | `#0D0F11`                  | Fond d'écran |
| `surface`    | `#15181B`                  | Cartes / items |
| `surface2`   | `#1C2024`                  | Surfaces secondaires |
| `surface3`   | `#262B31`                  | Avatars, +N |
| `ink`        | `#F4F6F7`                  | Texte principal |
| `ink2`       | `#C5CCD1`                  | Texte secondaire |
| `muted`      | `#8A929A`                  | Texte tertiaire / méta |
| `muted2`     | `#5E666D`                  | Chevrons, inactif |
| `line`       | `rgba(255,255,255,0.09)`   | Bordures / trait timeline |
| `line2`      | `rgba(255,255,255,0.055)`  | Bordures de cartes |
| `cyanInk`    | `#0A2C36`                  | Texte sombre posé SUR le cyan (bas de la carte une) |

Couleur de texte sur la carte une (haut rouge) : **blanc `#FFFFFF`**.

### Typographie

Police : **Archivo** (Google Fonts), une grotesque géométrique. Variable dans la maquette web ;
en Compose, embarque les `.ttf` (poids 500/700/800/900) dans `commonMain/composeResources/font/`.
À défaut, **Archivo Black** pour les titres + une grotesque proche pour le texte.

| Rôle (classe web)        | Taille | Graisse | Interligne | Notes |
|--------------------------|--------|---------|-----------|-------|
| Salutation (`.display`)  | 34 sp  | ~820    | 1.02      | « Bonjour/Bonsoir » |
| Titre carte une (`.display`) | 22 sp | ~820 | 1.1      | sur fond rouge, blanc |
| Filigrane « CSP » (`.display-wide`) | 96 sp | ~880 | 1 | blanc 13 % d'opacité, rogné dans la carte |
| Titre de section         | 17 sp  | ~820    | —         | « Sorties suivantes » |
| Numéro du jour (timeline)| 21 sp  | ~820    | 1.1       | ex. « 21 » |
| Titre d'événement        | 15 sp  | 700     | 1.25      | |
| Méta / date / heure      | 13–14 sp | 600–700 | —       | |
| Jour & mois (timeline)   | 10–11 sp | 700    | —         | « Dim. » / « juin », +letterSpacing 0.04em |
| Badge « PROCHAINE SORTIE » | 11.5 sp | 800   | —         | majuscules, letterSpacing 0.06em |
| Label onglet nav         | 10.5 sp | 600/700 | —        | |

> `.display` ≈ FontWeight 800 + très légère condensation. Sans la police variable, FontWeight.Black
> (900) sur Archivo Black donne un rendu fidèle pour les gros titres.

### Espacements, rayons, ombres

- **Padding écran** : 16 dp en haut, **18 dp** gauche/droite, 22 dp en bas (zone scrollable).
- **Rayons** : carte une `22 dp` · items timeline `14 dp` · pastilles/pills `999 dp` (full) ·
  cartes variées `16 dp`.
- **Gaps** : salutation↔carte 24 dp · titre section (marge bas) 16 dp · item timeline → contenu 14 dp ·
  espacement vertical entre items 16 dp (0 sur le dernier).
- **Ombre carte une** : `0 16 32 -12 rgba(0,0,0,0.55)` → en Compose, `Modifier.shadow(16.dp, RoundedCornerShape(22.dp))` ou élévation ~12–16 dp.
- **Dégradé haut de carte** : linéaire 135°, `#F5333F` → `#D21F2B` (stop à 82 %).
- **Bas de carte** : aplat `#4CCBEE`.

---

## Composants — mapping vers Compose

Recrée chaque bloc comme un `@Composable`. Suggestions de noms entre parenthèses.

### 1. `Greeting()`
- Colonne. Texte 1 : salutation (34 sp, black, `ink`). Texte 2 : « Prêt·e à rouler ? » (15 sp, 500, `muted`), marge top 6 dp.
- Logique : `val hour = ...; val salut = if (hour >= 16) "Bonsoir" else "Bonjour"`.
  Utilise `kotlinx-datetime` (`Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour`).
- ⚠️ L'en-tête historique (logo + cloche) a été **retiré** : ne PAS l'ajouter.

### 2. `NextRideHero(event, onClick)` — carte « Prochaine sortie »
`Column` dans une `Card`/`Box` (rayon 22 dp, `clip`, ombre, `clickable`). Deux zones :
- **Haut (rouge, dégradé)** : `Box` avec `Brush.linearGradient`. Padding 18/18/15.
  - Filigrane « CSP » : `Text` 96 sp en haut-droite, blanc 13 %, `clipToBounds`, non cliquable.
  - Pastille « PROCHAINE SORTIE » : `Row` pill, fond blanc 20 %, icône *route* + label 11.5 sp/800 blanc.
  - Titre de l'événement : 22 sp black, blanc, marge top 13 dp, maxWidth ~94 %.
- **Bas (cyan `#4CCBEE`)** : padding 14/18/16, texte en `cyanInk` (`#0A2C36`).
  - `Row` (gap 16) : icône *calendar* + « {jour} {n°} {mois} » ; icône *clock* + heure. 14 sp/700.
  - `Row` espacé : à gauche, pile d'avatars superposés (3, chevauchement −10 dp) + « {n} inscrits » 13 sp/700 ;
    à droite, pill blanche « Voir › » (texte `redDeep`, 14 sp/800).

### 3. `AgendaItem(event, last, onClick)` — ligne de timeline
`Row` (gap 14, `clickable`) en 3 colonnes :
- **Date** (largeur 44 dp, centrée) : jour 3 lettres (11 sp/700 `muted`) / numéro (21 sp black `ink`) / mois (10 sp/700 `muted`).
- **Rail timeline** (largeur 14 dp) : trait vertical 2 dp `line` sur toute la hauteur ; au centre un marqueur :
  - événement normal → **point plein** 12 dp couleur `blue`, halo 4 dp couleur `bg`.
  - événement **annulé** → **croix rouge** (deux traits `#F5333F`, ~2.4 dp, linecap rond) dans un disque `bg` de 20 dp.
- **Carte** (`weight 1`) : `surface`, bordure `line2`, rayon 14 dp, padding 12/13. `Row` :
  - Colonne : titre (15 sp/700 `ink`) ; ligne méta = icône *clock* + heure.
  - Chevron `›` à droite (17 dp, `muted2`) pour indiquer la cliquabilité.
- **État annulé** (`status == "cancelled"`) :
  - **titre barré** (`TextDecoration.LineThrough`) mais **en blanc** (`ink`).
  - **heure barrée en rouge** (`red` + LineThrough).
  - mention **« · Annulé » en rouge, NON barrée**, FontWeight 800.
  - marqueur = croix rouge (cf. ci-dessus).
- `last == true` → pas de padding bas / le trait s'arrête.

### 4. `BottomNav(activeTab)` — barre de navigation
`Row` collée en bas, bordure haute `line`, fond `#0D0F11` ~92 % (effet verre/blur facultatif sur Android).
3 onglets équivalents (`weight 1`), chacun : icône (23 dp) + label (10.5 sp). Actif = `red` (+ trait plus épais),
inactif = `muted2`. Onglet actif par défaut : **Accueil**. Respecte les insets système (navigation bar).

### 5. `HomeScreen(...)` — assemblage
`Column` plein écran fond `bg` :
- Zone scrollable (`LazyColumn` ou `Column` + `verticalScroll`), padding 16/18/18/22 :
  `Greeting` → `NextRideHero` → titre « Sorties suivantes » (17 sp black) → liste des `AgendaItem`.
- `BottomNav` épinglée en bas (hors scroll).
- La carte une vient de l'événement marqué `featured = true` ; la liste = tous les autres (`!featured`).

---

## Icônes

Toutes les icônes de la maquette sont des **SVG au trait** (stroke, linecap/linejoin ronds), viewBox 24×24.
Icônes utilisées sur l'accueil : `calendar`, `clock`, `route`, `chevR` (chevron droit), + nav `home` / `calendar` / `user`.
Tu peux :
- les recréer avec `ImageVector` / `materialIcons` proches (Material Symbols **Outlined**, trait ~2 dp), ou
- récupérer les `path` exacts dans `reference_html/bundle.jsx` (fonction `Icon`, objet `paths`) et les porter en `ImageVector.Builder`.
Garde le style **outline fin**, pas de versions pleines (sauf cœur plein / étoile, non utilisés sur l'accueil).

## Animations (optionnel, fidélité)
- Entrée carte une & liste : léger *fade + translate Y* (~9 dp, 500 ms, easing standard). Respecte « réduire les animations ».
- Tap : `scale` à ~0.975 (feedback pressé). En Compose : `Modifier.clickable` + `animateFloatAsState` ou `InteractionSource`.

---

## Données (modèle)

Modèle d'événement utilisé par la maquette (à adapter à tes data classes / API) :

```kotlin
data class ClubEvent(
    val id: String,
    val title: String,
    val wd3: String,        // jour 3 lettres, ex. "Dim."
    val day: Int,           // n° du jour, ex. 21
    val monthShort: String, // ex. "juin"
    val time: String,       // ex. "8h00"
    val status: Status,     // OPEN, CANCELLED, FULL
    val featured: Boolean = false,
    val participants: Int = 0,
)
enum class Status { OPEN, CANCELLED, FULL }
```

Contenu affiché dans la maquette (à titre indicatif — remplace par tes vraies données) :

- **Featured / carte une** — « Rallye US Bois Saint-Denis », mer. 17 juin, 8h00, 36 inscrits.
- Liste « Sorties suivantes » :
  1. « Présentation des équipements 2026 » — **Dim.** 21 juin, 19h00 — ouvert.
  2. « Sortie CSP » — **Sam.** 27 juin, 8h00 — **annulé** (titre barré, croix rouge).
  3. « Rallye CSP » — **Dim.** 28 juin, 7h30 — ouvert.

> Note de cohérence laissée à ta main : dans la maquette, la carte « une » (17 juin) est antérieure
> au 1er item de liste (21 juin). En production, tri/featured devrait pointer la sortie la plus proche.

---

## Assets

- `assets/logo-csp.png` — logo officiel du club (rond rouge/cyan, « CSP »). Fourni pour le branding,
  l'écran de détail et l'icône d'app. **Non affiché sur l'accueil** (en-tête retiré).
- Le petit badge « CSP » rond rouge/cyan vu ailleurs dans la maquette est dessiné en CSS
  (`CspBadge`) — reproductible avec deux demi-disques (`Box` + `Brush`/`drawBehind`) si besoin,
  sinon utilise directement `logo-csp.png`.

## Fichiers de référence

- `reference_html/index.html` — ouvre dans un navigateur pour voir le rendu exact (plan de travail).
- `reference_html/bundle.jsx` — tout le code React de la maquette. **Source des valeurs exactes** :
  - objet `C` (tout en haut) = tokens couleur ;
  - `Greeting`, `NextRideHero`, `AgendaItem`, `BottomNav`, `HomeScreen` = les composants de l'accueil ;
  - fonction `Icon` (objet `paths`) = chemins SVG des icônes.
- `reference_html/android-frame.jsx` — cadre de téléphone Android utilisé pour la présentation
  (purement cosmétique, **à ignorer** pour l'intégration).
- `CspTheme.kt` — point de départ Compose (couleurs portées en Kotlin) à adapter à ton thème.
