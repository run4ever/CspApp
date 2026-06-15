package fr.cyclosportpantin.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens de couleur de l'écran d'accueil « Cyclo Sport Pantin » (thème sombre).
 * Portés depuis la maquette HTML (objet `C` dans reference_html/bundle.jsx).
 *
 * NOTE pour l'intégration : si le projet possède déjà un thème Compose, mappe ces
 * valeurs sur tes tokens existants plutôt que d'introduire un thème concurrent.
 */
object CspColors {
    val Red       = Color(0xFFF5333F) // accent principal
    val RedDeep   = Color(0xFFD21F2B) // fin de dégradé / texte sur bouton blanc
    val RedSoft   = Color(0x24F5333F) // ~14% — fonds de pastilles rouges
    val Blue      = Color(0xFF4CCBEE) // cyan du logo
    val BlueDeep  = Color(0xFF1FA9D2)
    val BlueSoft  = Color(0x264CCBEE) // ~15%
    val Green     = Color(0xFF3DD68C) // statut "Ouvert"

    val Bg        = Color(0xFF0D0F11) // fond d'écran
    val Surface   = Color(0xFF15181B) // cartes / items
    val Surface2  = Color(0xFF1C2024)
    val Surface3  = Color(0xFF262B31)

    val Ink       = Color(0xFFF4F6F7) // texte principal
    val Ink2      = Color(0xFFC5CCD1) // texte secondaire
    val Muted     = Color(0xFF8A929A) // texte tertiaire / méta
    val Muted2    = Color(0xFF5E666D) // chevrons / inactif

    val Line      = Color(0x17FFFFFF) // ~9% — bordures / trait timeline
    val Line2      = Color(0x0EFFFFFF) // ~5.5% — bordures de cartes

    val CyanInk   = Color(0xFF0A2C36) // texte sombre posé SUR le cyan
    val OnRed     = Color(0xFFFFFFFF) // texte blanc sur le haut rouge de la carte une
}

/*
 * Espacements de référence (dp) :
 *   screenPadding   = top 16 / horizontal 18 / bottom 22
 *   cardRadius      = 22   (carte une)
 *   itemRadius      = 14   (items timeline)
 *   pillRadius      = 50%  (RoundedCornerShape(percent = 50))
 *   heroShadow      = elevation ~16, RoundedCornerShape(22.dp)
 *
 * Dégradé du haut de la carte une :
 *   Brush.linearGradient(listOf(Red, RedDeep))  // ~135°, stop foncé vers 82%
 * Bas de la carte une : aplat CspColors.Blue
 *
 * Typo : police "Archivo" (poids 500/700/800/900). Titres = Black (900).
 */
