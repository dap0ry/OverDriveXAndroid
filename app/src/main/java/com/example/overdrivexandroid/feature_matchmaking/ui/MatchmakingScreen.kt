package com.example.overdrivexandroid.feature_matchmaking.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.overdrivexandroid.core.session.SessionManager
import com.example.overdrivexandroid.feature_matchmaking.domain.AVAILABLE_GAMES

// Pantalla principal de Matchmaking que orquesta las 4 sub-pantallas
// dependiendo del State Enum: Selector -> Buscando -> VS -> ...
@Composable
fun MatchmakingScreen(
    onNavigateBack: () -> Unit,
    viewModel: MatchmakingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Este LaunchedEffect es un bloque que se ejecuta sin bloquear la UI cuando cambia el screenState
    LaunchedEffect(state.screenState) {
        if (state.screenState == MatchmakingScreenState.SELECTOR && state.matchData != null) {
            // Regresamos al estado inicial después de reportar un resultado exitosamente:
            // Por simplicidad en la lógica actual, asumimos que onNavigateBack() limpia la pila de vuelta.
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Renderizamos diferentes UI según el estado actual del Matchmaking
        when (state.screenState) {
            MatchmakingScreenState.SELECTOR -> GameSelectorScreen(
                selectedGames = state.selectedGameSlugs,
                isLoading = false,
                errorMessage = state.errorMessage,
                onToggleGame = { viewModel.toggleGameSelection(it) },
                onStartSearch = { viewModel.startSearch() },
                onBack = onNavigateBack
            )
            MatchmakingScreenState.SEARCHING -> SearchingScreen(
                onCancel = { viewModel.cancelSearch() }
            )
            MatchmakingScreenState.VERSUS -> VersusScreen(
                state = state,
                // Le pasamos null al username provisionalmente (lo extraeremos después con más calma)
                myUsername = SessionManager.currentToken?.let { null },
                myFullCharUrl = state.myFullCharUrl,
                onReportResult = { result -> viewModel.reportResult(result, onNavigateBack) },
                isLoading = false
            )
            MatchmakingScreenState.DISPUTE -> DisputeScreen(onGoBack = onNavigateBack)
        }

        // Si hay un error saltará un Snackbar superpuesto gracias al Box
        if (state.errorMessage != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK", color = Color.White)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(state.errorMessage!!, color = Color.White)
            }
        }
    }
}

// ─────────────────────────────────────────────
// PANTALLA 1: Selector de juegos (lista clickeable)
// ─────────────────────────────────────────────
@Composable
fun GameSelectorScreen(
    selectedGames: Set<String>,
    isLoading: Boolean,
    errorMessage: String?,
    onToggleGame: (String) -> Unit,
    onStartSearch: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "ELIGE TUS JUEGOS",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Pulsa para activar / desactivar",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Cancel, contentDescription = "Volver", tint = Color.Gray)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f) // Ocupa todo el espacio de arriba hasta empujar el botón al fondo
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(AVAILABLE_GAMES) { game ->
                val isSelected = selectedGames.contains(game.slug)
                // Usamos colores dinámicos dependiendo de si está o no seleccionado el juego
                val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                val bgColor = if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.surface

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { onToggleGame(game.slug) },
                    colors = CardDefaults.cardColors(containerColor = bgColor),
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
                            Icon(
                                imageVector = Icons.Default.Sports,
                                contentDescription = game.name,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                game.name,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartSearch,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    "BUSCAR PARTIDA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ─────────────────────────────────────────────
// PANTALLA 2: Buscando partida 
// Usa animateFloat y rememberInfiniteTransition para animaciones fluidas!
// ─────────────────────────────────────────────
@Composable
fun SearchingScreen(onCancel: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // Animación de latido (crecer/encoger el círculo central)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // Animación de los puntos secuenciales (... al estilo "Buscando.")
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 200), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 400), RepeatMode.Reverse),
        label = "dot3"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .scale(scale) // Aplica la animación dinámica
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sports,
                contentDescription = "Buscando",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "BUSCANDO",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("•", color = MaterialTheme.colorScheme.primary, fontSize = 32.sp, modifier = Modifier.alpha(dot1Alpha))
            Text("•", color = MaterialTheme.colorScheme.primary, fontSize = 32.sp, modifier = Modifier.alpha(dot2Alpha))
            Text("•", color = MaterialTheme.colorScheme.primary, fontSize = 32.sp, modifier = Modifier.alpha(dot3Alpha))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Esperando oponente...", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedButton(
            onClick = onCancel,
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("CANCELAR", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────
// PANTALLA 3: VERSUS (Pantalla dividida dinámica Vertical para Android)
// ─────────────────────────────────────────────
@Composable
fun VersusScreen(
    state: MatchmakingState,
    myUsername: String?,
    myFullCharUrl: String?,
    onReportResult: (String) -> Unit,
    isLoading: Boolean
) {
    val matchData = state.matchData ?: return
    val timerMinutes = state.activeTimerSeconds / 60
    val timerSecs = state.activeTimerSeconds % 60
    val myName = (myUsername ?: "TÚ").uppercase()
    val opponentName = (matchData.opponent_name ?: "OPONENTE").uppercase()
    val gameName = (matchData.game_name ?: "").uppercase()

    // Controla si mostramos el modal (diálogo de confirmación de victoria/derrota)
    var showConfirmDialog by remember { mutableStateOf<String?>(null) }

    if (showConfirmDialog != null) {
        val result = showConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null }, // Al pulsar fuera se cierra
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    if (result == "win") "¿Confirmar VICTORIA?" else "¿Confirmar DERROTA?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Esta acción no se puede deshacer.", color = Color.Gray)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = null
                        onReportResult(result) // Confirmamos disparando hacia el ViewModel
                    },
                    colors = ButtonDefaults.buttonColors(
                        // Usar verde si ganamos, morado/rojo si perdimos
                        containerColor = if (result == "win") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Confirmar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra del juego y tiempo restante arriba del todo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(gameName, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%02d:%02d".format(timerMinutes, timerSecs),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── TOP PLAYER (El jugador actual) ──
        PlayerPanel(
            name = myName,
            label = "PLAYER 1",
            avatarUrl = matchData.player_full ?: myFullCharUrl ?: matchData.player_avatar,
            labelColor = MaterialTheme.colorScheme.primary
        )

        // ── Cartel central de VS ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "VS",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontStyle = FontStyle.Italic
                )
            )
        }

        // ── BOTTOM PLAYER (Oponente) ──
        PlayerPanel(
            name = opponentName,
            label = "PLAYER 2",
            avatarUrl = matchData.opponent_full ?: matchData.opponent_avatar,
            labelColor = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.weight(1f))

        // ── Botonera para reportar Win/Loss (Baja abajo de forma flexible usando el weight) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "¿RESULTADO?",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Button(
                    onClick = { showConfirmDialog = "win" },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VICTORIA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { showConfirmDialog = "loss" },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DERROTA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// Sub-componente extraído de la pantalla Versus para renderizar cada carta de jugador
@Composable
fun PlayerPanel(
    name: String,
    label: String,
    avatarUrl: String?,
    labelColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(2.dp, labelColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = labelColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Contenedor principal de la imagen de cuerpo entero (ContentScale.Fit mantiene proporciones)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Personaje de $name",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sports,
                        contentDescription = name,
                        tint = labelColor,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────
// PANTALLA 4: Disputa (Confrontación de resultados del Matchmaking)
// ─────────────────────────────────────────────
@Composable
fun DisputeScreen(onGoBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = "Disputa",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("DISPUTA", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Los reportes de ambos jugadores no coinciden. Un administrador revisará el resultado.",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onGoBack,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("VOLVER AL INICIO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
