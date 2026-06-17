package fr.csp.app.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.csp.app.resources.Res
import fr.csp.app.resources.casquette
import fr.csp.app.resources.maillot_blanc
import fr.csp.app.resources.maillot_noir
import fr.csp.app.ui.theme.CspColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// ⚠️ Mettre à jour avec l'URL de la boutique HelloAsso du club
private const val HELLOASSO_URL = "https://www.helloasso.com/associations/cyclo-sport-pantin/boutiques/equipement-csp"

private data class Product(val name: String, val image: DrawableResource)

private val PRODUCTS = listOf(
    Product("Maillot CSP Noir", Res.drawable.maillot_noir),
    Product("Maillot CSP Blanc", Res.drawable.maillot_blanc),
    Product("Casquette CSP", Res.drawable.casquette),
)

@Composable
fun ShopScreen() {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Boutique",
            style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
        )
        Text(
            "Équipement aux couleurs du club.",
            style = TextStyle(fontSize = 14.sp, color = CspColors.Muted2),
        )

        PRODUCTS.forEach { product ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CspColors.Surface)
                    .border(1.dp, CspColors.Line, RoundedCornerShape(16.dp)),
            ) {
                Image(
                    painter = painterResource(product.image),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    product.name,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CspColors.Red)
                .clickable { uriHandler.openUri(HELLOASSO_URL) }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Commander sur HelloAsso",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
            )
        }

        Text(
            "Vous serez redirigé vers notre boutique HelloAsso pour finaliser votre commande.",
            style = TextStyle(fontSize = 12.sp, color = CspColors.Muted2, lineHeight = (12 * 1.6).sp),
        )
    }
}
