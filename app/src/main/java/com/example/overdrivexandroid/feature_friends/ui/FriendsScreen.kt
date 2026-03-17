package com.example.overdrivexandroid.feature_friends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.overdrivexandroid.feature_friends.domain.Friend
import com.example.overdrivexandroid.feature_friends.domain.PendingRequest

// Pantalla Social: Lista de amigos, solicitudes enviadas/recibidas y buscador
@Composable
fun FriendsScreen(viewModel: FriendsViewModel = viewModel()) {
    val context = LocalContext.current
    // Comprobamos si hay internet. Si no lo hay, abortamos la carga de la pantalla entera.
    val isOnline = remember { NetworkMonitor.isOnline(context) }

    if (!isOnline) {
        OfflineScreen()
        return
    }

    // Observamos el StateFlow del ViewModel
    val state by viewModel.state.collectAsState()
    
    // Controlamos de forma local (UI state) si mostramos el modal para añadir amigod
    var showAddDialog by remember { mutableStateOf(false) }

    // Limpiamos mensajes de éxito/error después de 3 segundos automáticamente
    LaunchedEffect(state.successMessage, state.errorMessage) {
        if (state.successMessage != null || state.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    // Si el usuario da a "Añadir amigo", mostramos la ventana emergente flotante (AlertDialog)
    if (showAddDialog) {
        AddFriendDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { username ->
                viewModel.sendRequest(username)
                showAddDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // == CABECERA (Título y botón añadir) ==
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Amigos", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                // Calculamos y mostramos cuántos están "Online" dinámicamente
                Text("${state.friendsList.count { it.online }} conectados", color = Color.Gray, fontSize = 14.sp)
            }

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = "Añadir amigo", tint = Color.White)
            }
        }

        // == MENSAJES DE RESPUESTA DE LA API ==
        if (state.errorMessage != null) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        }
        if (state.successMessage != null) {
            Text(state.successMessage!!, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Si estamos descargando datos del servidor mostramos spinner
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            // == CONTENIDO PRINCIPAL: Lista con scroll Infinito de Solicitudes y Amigos ==
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                
                // Si hay gente que nos ha agregado, dibujamos la cajita superior de Solicitudes Pendientes
                if (state.pendingRequests.isNotEmpty()) {
                    item {
                        PendingRequestsSection(
                            requests = state.pendingRequests,
                            onAccept = { viewModel.acceptRequest(it) },
                            onReject = { viewModel.rejectRequest(it) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Generamos filas de amigos uno por uno de la lista mapeada.
                items(state.friendsList) { friend ->
                    FriendRow(friend)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────
// COMPONENTES REUTILIZABLES AUXILIARES
// ─────────────────────────────────────────────

// Componente helper para pintar fotos de perfil redondas o cuadradas, o un placeholder gris
@Composable
fun AvatarImage(
    avatarUrl: String?,
    username: String,
    size: Int = 40,
    isCircle: Boolean = true
) {
    val shape = if (isCircle) CircleShape else RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar de $username",
                contentScale = ContentScale.Crop, // Evitamos estirar o achatar la imagen
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Sin foto",
                tint = Color.White,
                modifier = Modifier.size((size * 0.6f).dp)
            )
        }
    }
}

// Sub-sección en la lista que muestra la tarjeta contenedora de invitaciones de amistad pendientes
@Composable
fun PendingRequestsSection(
    requests: List<PendingRequest>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = "Solicitudes", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Solicitudes (${requests.size})", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Iteramos las invitaciones una a una con sus botones Aceptar/Rechazar 
            requests.forEach { request ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarImage(
                            avatarUrl = request.avatar_url,
                            username = request.username,
                            size = 40
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(request.username, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Quiere ser tu amigo", color = Color.Gray, fontSize = 12.sp)
                        }
                    }

                    // Botonera Accept/Decline
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onAccept(request.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Aceptar", tint = Color.White)
                        }
                        IconButton(
                            onClick = { onReject(request.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Rechazar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Fila unitaria estándar de un amigo conectad/o desconectado en la lista 
@Composable
fun FriendRow(friend: Friend) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AvatarImage(
                    avatarUrl = friend.avatar_url,
                    username = friend.username,
                    size = 48
                )
                
                // Online Indicator (Un circulito pequeñito en verde en la esquina inferior izquierda de la foto del amigo)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (friend.online) MaterialTheme.colorScheme.tertiary else Color.Gray)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(friend.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${friend.rank} • ${friend.elo} pts", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

// Diálogo nativo modal centrado para pedirle username de amigo a agregar
@Composable
fun AddFriendDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var username by remember { mutableStateOf("") } // Estado local efímero para tipear nombre de usuario

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Añadir Amigo", color = Color.White) },
        text = {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (username.isNotBlank()) onAdd(username) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Enviar Solicitud", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}
