package com.example.overdrivexandroid.feature_characters.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.overdrivexandroid.core.network.NetworkMonitor
import com.example.overdrivexandroid.core.ui.OfflineScreen
import com.example.overdrivexandroid.feature_characters.domain.Character

@Composable
fun CharactersScreen(viewModel: CharactersViewModel = viewModel()) {
    val context = LocalContext.current
    
    // Necesitamos Internet indispensablemente para descargar tanto las fotos del Roster como para guardar en la API.
    val isOnline = remember { NetworkMonitor.isOnline(context) }

    if (!isOnline) {
        OfflineScreen()
        return
    }

    val state by viewModel.state.collectAsState()

    // Estado efímero puramente visual para mostrar una notificación (snackbar) unos segundos
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    // Observar los estados de Success ó Error para lanzar Snackbars temporales
    LaunchedEffect(state.saveSuccess, state.errorMessage) {
        if (state.saveSuccess) {
            showSuccessSnackbar = true
            kotlinx.coroutines.delay(2000)
            showSuccessSnackbar = false
            viewModel.clearMessages()
        }
        if (state.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    // Identificador del juego que actualmente estamos visualizando (Pestañas horizontales)
    val activeGame = state.rosters.getOrNull(state.selectedGameIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // == EFECTOS VISUALES: Fondo Dinámico ==
        // Dependiendo del juego (ej: Street Fighter), el fondo será un Wallpaper representativo del juego desaturado
        if (activeGame != null) {
            AsyncImage(
                model = activeGame.bgImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.2f // Transparencia muy baja para no interrumpir lectura
            )
            // Se sobrepone un gradiente oscuro estilo "fade-to-black" desde Bottom hasta Top para crear profundidad.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            ),
                            startY = 0f,
                            endY = 1000f
                        )
                    )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Cabecera Principal
            Text(
                text = "Personajes",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
            )

            // Selector horizontal estilo "Tabs" (Mortal Kombat, StreetFighter, Tekken, GG...)
            GameTabs(
                rosters = state.rosters,
                selectedIndex = state.selectedGameIndex,
                onGameSelected = { viewModel.selectGame(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // == CONTENIDO INFERIOR DIVIDIDO ==
            if (activeGame != null) {
                
                // Arriba: Vista Previa gigante del personaje seleccionado (Full art image)
                CharacterPreview(
                    character = state.selectedCharacter,
                    gameName = activeGame.gameName,
                    isSaving = state.isSaving,
                    onSave = { viewModel.saveSelectedCharacter() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Abajo: Matríz (Grid Vertical estilo Pinterest) de los iconos de todos los personajes del Roster Actual
                Text(
                    text = "Selecciona un luchador",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(80.dp), // Intentará colocar X columnas de iconos adaptables al layout de 80dp
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f) // Ocupa el restante
                ) {
                    items(activeGame.chars) { char ->
                        CharacterIcon(
                            character = char,
                            // Mantenemos marcado con check/borde la que hemos pulsado
                            isSelected = state.selectedCharacter?.id == char.id,
                            onClick = { viewModel.selectCharacter(char) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // ======== NOTIFICACIONES (SNACKBARS FLOTANTES) ======== 
        if (showSuccessSnackbar) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¡Avatar actualizado!", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (state.errorMessage != null) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Text(state.errorMessage!!, color = Color.White)
            }
        }
    }
}

// ─────────────────────────────────────────────
// COMPONENTES AUXILIARES EXTRAIDOS DE CHARACTERS_SCREEN
// ─────────────────────────────────────────────

// Barra horizontal "LazyRow" reslizable con chips por juego (Ej: "Mortal Kombat 1", "Tekken 8"...)
@Composable
fun GameTabs(
    rosters: List<com.example.overdrivexandroid.feature_characters.domain.GameRoster>,
    selectedIndex: Int,
    onGameSelected: (Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rosters.size) { index ->
            val game = rosters[index]
            val isSelected = index == selectedIndex
            
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onGameSelected(index) }
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = game.gameName,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Tarjeta grande rectangular. Subdividida en dos bloques, IZQUIERDA = Titulo del char + Boton Guardar | DERECHA = Imagen completa del char
@Composable
fun CharacterPreview(
    character: Character?,
    gameName: String,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        if (character == null) {
            // Estado vacío cuando el usuario solo ha entrado a la pestaña y aún no ha seleccionado avatar interno
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Elige un personaje de la lista", color = Color.Gray)
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Izquierda: Títulos y Botón Usar Avatar
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(gameName.uppercase(), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(character.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = onSave,
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("USAR AVATAR")
                        }
                    }
                }

                // Derecha: Imagen cuerpo entero
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    AsyncImage(
                        model = character.image,
                        contentDescription = character.name,
                        contentScale = ContentScale.Fit, // Se ajusta perfectamente al contenedor derecho, sin desbordarse ni comer pixeles
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp, bottom = 16.dp, end = 16.dp)
                    )
                }
            }
        }
    }
}

// Representa UNA SOLA CARILLA en la "matriz" LazyGrid del roster. Suele ser una cara cortada del personaje del juego estilo Mugshot
@Composable
fun CharacterIcon(
    character: Character,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Lo hacemos perfectamente cuadrado independientemente del tamaño dinámico que adopte
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = character.icon,
            contentDescription = character.name,
            contentScale = ContentScale.Crop, // Expande en caso de que sea de formato irregular para rellenar este cuadrito de 80dp
            modifier = Modifier.fillMaxSize()
        )
        
        // Renderizamos una miniatura del "check"/V en la pura esquina superior derecha
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}
