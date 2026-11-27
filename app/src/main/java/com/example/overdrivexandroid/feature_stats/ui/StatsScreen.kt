package com.example.overdrivexandroid.feature_stats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.overdrivexandroid.core.network.NetworkMonitor
import com.example.overdrivexandroid.core.ui.OfflineScreen
import com.example.overdrivexandroid.feature_stats.domain.GameStatSummary
import com.example.overdrivexandroid.feature_stats.domain.GlobalStatsSummary

/*
 * Pantalla de Estadísticas ("StatsScreen")
 * Muestra las estadísticas resumidas globales del jugador (WinRate, Derrotas, Rango Base Máximo)
 * y una lista detallada con una tarjeta individual desglosando victorias y derrotas especificadas
 * por todos los juegos que el jugador posee.
 */
@Composable
fun StatsScreen(
    userId: String,
    viewModel: StatsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Verificación constante de red (se suspende la pantalla si hay caída súbita de online a offline)
    val isOnline = remember { NetworkMonitor.isOnline(context) }

    if (!isOnline) {
        OfflineScreen()
        return
    }

    val state by viewModel.state.collectAsState()

    // En cuanto se abre la pantalla, lanzamos la petición a la API
    LaunchedEffect(userId) {
        viewModel.loadStats(userId)
    }

    when {
        // Pantalla de Carga
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        // Estado Error
        state.errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
            }
        }
        // Éxito: Hay datos listos para pintar
        state.globalStats != null -> {
            StatsContent(
                globalStats = state.globalStats!!,
                gameStats = state.gameStats
            )
        }
    }
}

// ─────────────────────────────────────────────
// COMPONENTE PRINCIPAL (Scroll Verticál + Secciones divididas)
// ─────────────────────────────────────────────
@Composable
private fun StatsContent(
    globalStats: GlobalStatsSummary,
    gameStats: List<GameStatSummary>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ── Cabecera ──
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "ESTADÍSTICAS",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tu rendimiento global",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ── Grid Global Stats (Bloque 2x2 similar a Dashboard pero con data estadística) ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Rango Global",
                    value = globalStats.global_rank,
                    subtitle = "${globalStats.global_elo} ELO",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = MaterialTheme.colorScheme.primary
                )
                DashStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Victorias",
                    value = "${globalStats.total_wins}",
                    subtitle = "de ${globalStats.total_matches} partidas",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = MaterialTheme.colorScheme.tertiary // Verde para victorias
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Derrotas",
                    value = "${globalStats.total_losses}",
                    subtitle = "partidas perdidas",
                    icon = Icons.Default.Close,
                    accentColor = Color(0xFFE57373) // Rojo pálido para derrotas
                )
                // Calculamos win-rate % dinámicamente:
                val winRate = if (globalStats.total_matches > 0)
                    (globalStats.total_wins * 100 / globalStats.total_matches)
                else 0
                DashStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Win Rate",
                    value = "$winRate%",
                    subtitle = "${globalStats.total_matches} totales",
                    icon = Icons.Default.Timeline,
                    accentColor = Color(0xFF00BFFF)
                )
            }
        }

        // ── Listado inferior juego por juego ──
        if (gameStats.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Estadísticas por Juego",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(gameStats) { _, gameStat ->
                GameStatRow(gameStat)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Deja un margen al final para la Barra de Navegación del móvil inferior (BottomBar Navigation)
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// Tarjetita cuadrada reutilizable para rellenar los "huecos" de Grid 2x2.
@Composable
private fun DashStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Card(
        modifier = modifier.border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = value, color = accentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// Componente para representar cada línea de Desglose de "X juego - X estadisticas"
@Composable
private fun GameStatRow(gameStat: GameStatSummary) {
    val total = gameStat.wins + gameStat.losses
    
    // Mapeamos el color del icono según la categoría del rango que devuelve la DB (Master = Oro intenso...)
    val rankColor = when (gameStat.rank.lowercase()) {
        "master"   -> Color(0xFFFF8C00)
        "diamond"  -> Color(0xFF00BFFF)
        "platinum" -> Color(0xFF00E5CC)
        "gold"     -> Color(0xFFFFD700)
        "silver"   -> Color(0xFFB0BEC5)
        else       -> Color(0xFFCD7F32) // bronze por defecto / NoRanked
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icono
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, rankColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sports,
                        contentDescription = "Game",
                        tint = rankColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Info central
                Column {
                    Text(
                        text = gameStat.game_name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ELO ${gameStat.elo}  •  ${gameStat.wins}V / ${gameStat.losses}D",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Decoración derecha (Categoría y número total)
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(rankColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = gameStat.rank,
                        color = rankColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$total partidas",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
