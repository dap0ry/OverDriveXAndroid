package com.example.overdrivexandroid.feature_home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.overdrivexandroid.core.ui.OfflineDashboardBanner
import com.example.overdrivexandroid.core.ui.OfflineScreen
import com.example.overdrivexandroid.feature_home.domain.DashboardResponse
import com.example.overdrivexandroid.feature_home.domain.MatchHistory

// Pantalla principal del Dashboard
@Composable
fun DashboardScreen(
    userId: String,
    onLogout: () -> Unit,
    onNavigateToMatchmaking: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    // Observamos el estado emitido por el ViewModel
    val state by viewModel.state.collectAsState()

    // Cada vez que cambie el userId (normalmente solo 1 vez al entrar), cargamos los datos
    LaunchedEffect(userId) {
        viewModel.loadDashboard(userId)
    }

    // Lógica condicional para renderizar: spinner de carga, contenido, o pantalla de error 100% offline
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        state.data != null -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Si la red falla pero tenemos caché, mostramos un aviso amarillo arriba del todo
                if (state.isOffline) {
                    OfflineDashboardBanner()
                }
                DashboardContent(
                    data = state.data!!,
                    isOffline = state.isOffline,
                    onLogout = onLogout,
                    onNavigateToMatchmaking = onNavigateToMatchmaking
                )
            }
        }
        state.errorMessage != null -> {
            // Sin conexión a internet y sin datos cacheados
            OfflineScreen()
        }
    }
}

// Estructura interna del Dashboard usando LazyColumn (como un RecyclerView en Android antiguo)
// para permitir scrolling infinito y cargar solo lo que se ve en pantalla.
@Composable
fun DashboardContent(
    data: DashboardResponse,
    isOffline: Boolean = false,
    onLogout: () -> Unit,
    onNavigateToMatchmaking: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // == CABECERA (Foto de perfil, Nombre y Botón de salir) ==
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Contenedor de la foto de perfil con Coil para cargar imágenes desde URL
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!data.avatar_url.isNullOrEmpty()) {
                            AsyncImage(
                                model = data.avatar_url,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop, // Recorta para llenar el cuadrado
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            // Placeholder si el usuario no tiene avatar
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Sin foto",
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "OVERDRIVEX",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hola, ${data.username}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = Color.Gray
                    )
                }
            }
        }

        // == CUADRICULAS DE ESTADÍSTICAS (4 tarjetas) ==
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Rango General",
                    value = data.stats.global_rank,
                    subtitle = "Nivel ${data.level} • ${data.stats.global_elo} pts",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Victorias",
                    value = "${data.stats.total_wins} victorias",
                    subtitle = "Racha",
                    icon = Icons.Default.Timeline,
                    accentColor = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Amigos Online",
                    value = "${data.friends.online} / ${data.friends.total}",
                    subtitle = "${data.friends.pending} solicitudes",
                    icon = Icons.Default.Group,
                    accentColor = Color(0xFF00BFFF)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Torneos",
                    value = "${data.total_tournaments} activos",
                    subtitle = "Participando",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // == BOTÓN GIGANTE DE BUSCAR PARTIDA == 
        // Solo lo mostramos si hay internet (en modo offline está deshabilitado por lógica).
        if (!isOffline) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToMatchmaking,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Gamepad, contentDescription = "Gamepad", modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Buscar Partida", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Comienza a luchar ahora", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                        // Botón auxiliar puramente visual
                        Button(
                            onClick = onNavigateToMatchmaking,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Jugar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // == HISTORIAL DE PARTIDAS RECIENTES ==
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Actividad Reciente",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Usamos `items()` que itera dinámicamente sobre la lista de partidas
        items(data.last_matches) { match ->
            MatchRow(match)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Espacio final para que la barra de navegación no tape el contenido
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// Componente reutilizable: Tarjeta cuadrada de estadísticas
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        // Borde tenue usando el color de acento del parámetro
        modifier = modifier
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(16.dp))
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

// Componente reutilizable: Fila horizontal de histórico de una partida
@Composable
fun MatchRow(match: MatchHistory) {
    val isWin = match.result == "WIN"
    // Verde (tertiary) para victoria, Purpura/Rosa (primary) para derrota
    val resultColor = if (isWin) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, resultColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Game",
                        tint = resultColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = match.game_name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = "ELO ${match.elo_before} -> ${match.elo_after}", color = Color.Gray, fontSize = 12.sp)
                }
            }

            // Etiqueta flotante de WIN/LOSS
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(resultColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isWin) "Victoria" else "Derrota",
                        color = resultColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(text = "Reciente", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
