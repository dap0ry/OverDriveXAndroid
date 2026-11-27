# Tutorial: Creación del Login y Registro en Android (Migración OverDriveX)

Esta guía documenta los pasos exactos realizados para cumplir la **Sección 1 (MVVM)**, la **Sección 2 (Capa de Datos)** y la **Sección 3 (Jetpack Compose)** para la función de Autenticación del proyecto Andorid y su conexión con la REST API.

## Pasos Realizados

### 1. Preparación del Entorno (`build.gradle.kts` y `AndroidManifest.xml`)
Se añadieron las dependencias críticas para arquitectura moderna recomendada por Google:
- **Retrofit & GSON:** Para manejar las peticiones HTTP y leer el JSON. (Sección 2 - Capa de Datos).
- **Navigation Compose:** Para viajar de una ventana a otra.
- **Coroutines:** Para realizar la carga asíncrona de datos sin bloquear la UI principal.
- También se otorgó `<uses-permission android:name="android.permission.INTERNET" />` en el manifest para salir a la red.

### 2. Configuración de Red (Sección 2: Consumo de API)
Creamos la configuración de Retrofit apuntando a `https://overdrivexapi.onrender.com/` (nuestra API).
Creamos modelos abstractos (Data Classes Kotlin) llamadas `LoginRequest`, `RegisterRequest` y `AuthResponse` que sirven como contrato del JSON (`feature_auth/domain/Models.kt`).
Declaramos las interfaces con `@POST("login")` para usar métodos eficientes de red.

### 3. Repositorio Asíncrono (Sección 2: Uso de Corrutinas)
Creamos `AuthRepository.kt` cuya función es centralizar las llamadas de Retrofit. 
En él usamos `withContext(Dispatchers.IO) { ... }` para derivar la carga de la API a un hilo secundario y nunca colapsar el "Main Thread" usando funciones etiquetadas como **suspend**, tal cual indica la rúbrica.

### 4. ViewModel y State Hoisting (Sección 1: Arquitectura y Estado)
Hicimos un `AuthViewModel.kt`. Aquí es donde reside gran parte de la nota del proyecto.
El ViewModel crea su propio alcance asíncrono seguro (`viewModelScope.launch`).  Así nos evitamos Memory Leaks o crasheos. 
El estado se expone con un formato reactivo: `StateFlow<AuthState>`.  Este patrón (`State Hoisting`) impide que un botón escriba directamente sobre la lógica, consiguiendo interfaces predecibles.

### 5. Pantallas (Jetpack Compose UI)
Se crearon las vistas `LoginScreen.kt` y `RegisterScreen.kt` únicamente usando Jetpack Compose.
Aquí leemos el ViewModel mediante `val state by viewModel.state.collectAsState()`, reaccionando a variables como `isLoading` para enseñar aros de carga (`CircularProgressIndicator`) al mismo tiempo que evaluamos campos vacíos para deshabilitar botones.

### 6. Navegación 
Por último ajustamos `MainActivity.kt` definiendo una malla de navegación segura: `NavHost`. Esto permite alternar a nivel visual desde 'login' hasta 'register'.

## ¿Qué sigue para completar el proyecto?
El paso posterior sería guardar el token obtenido del Login en **DataStore** local, y crear el Bottom Menu que nos dirija al **Dashboard**.

---

## 2️⃣ Implementación del Dashboard y Temática (Jetpack Compose)
Para igualar el diseño visual oscuro con tonos rojos de la aplicación web y consumir `/dashboard/<user_id>`, se realizaron las siguientes configuraciones:

### 1. Variables de Sesión (DataStore)
Guardamos el JWT en `SessionManager.kt`. Dado que la API no requiere introducir tu ID y contraseña de nuevo si tienes token, decodificamos el JWT Base64 en Kotlin para extraer de forma segura el `user_id`, y en `MainActivity.kt` añadimos una validación (`LaunchedEffect`) para que **te auto-loguees si ya tienes un token válido en la memoria del dispositivo**.
- Además, creamos `AuthInterceptor.kt` para meter automáticamente el header `Authorization: Bearer <TOKEN>` en cada petición de Retrofit.

### 2. Theming (Colores Gamers)
En `ui/theme/Color.kt` y `Theme.kt`, se forzó el **Dark Mode** deshabilitando los colores dinámicos de Android. Se usó una paleta basada en `0xFF0F0F0F` (Fondo Negro Intenso) y `0xFFFF0000` (Rojo Primario OverDriveX), replicando la vista oscura web.

### 3. API y Modelos de Datos del Dashboard
Se crearon en `DashboardModels.kt` las clases exactas (`GlobalStats`, `MatchHistory`, `FriendStats`) para mapear el JSON que devuelve el backend en Python.
Luego, `DashboardRepository.kt` hace la llamada GET asíncrona mediante corrutinas.

### 4. Vistas Reactivas (Compose)
- **El Menú Inferior**: Se creó `MainAppScreen.kt` usando un componente base `Scaffold` con un `BottomNavigationBar` anclado abajo. Contiene cada uno de los iconos (`Inicio`, `Amigos`, `Juegos`...)
- **El Dashboard**: La vista `DashboardScreen.kt`, alimentada por un `DashboardViewModel`, contiene la estructura visual idéntica a "Fight Arena". 
  - Usamos `LazyColumn` (listas eficientes de Compose, rúbrica punto 3) para alojar todo.
  - Se crearon componentes modulares reutilizables como `StatCard()` para el Rango y Amigos Online, y `MatchRow()` para colorear cada partida web según si ganaste `"Victoria"` (Verde) o perdiste `"Derrota"` (Primario Web).
  - Incluye el Botón Grande Destacado para **Buscar Partida**.

---

## 3️⃣ Implementación de la Pestaña "Amigos"
Para seguir el diseño de la web y el prototipo móvil (fondo negro, recuadros destacados y botones de acción), se programó la vista de Amigos conectándola con la NavBar:

### 1. Conexión a la API (Amigos)
Se ampliaron los Modelos y Repositorios para soportar las siguientes rutas:
- `GET /friends/list`: Devuelve la lista de amigos con su estado online/offline.
- `GET /friends/pending`: Devuelve las solicitudes entrantes.
- `POST /friends/accept`, `POST /friends/reject`, `POST /friends/request`: Acciones para gestionar las invitaciones.
Todo está centralizado en `FriendsViewModel.kt` usando `ViewState` para actualizar la pantalla sin parpadeos.

### 3. Expiración de Sesión y Logout
Se añadió control de sesión estricto en el lado del cliente (Android):
- **Expiración de Token Automática**: `SessionManager.kt` decodifica la fecha de caducidad `exp` del JWT. Si detecta que tu token ha caducado, automáticamente borra la sesión antigua y te obliga a iniciar sesión (`MainActivity.kt` previene el acceso al Dashboard).
- **Cierre de Sesión Manual**: En la cabecera superior derecha del Dashboard (`DashboardScreen.kt`), añadimos un botón nativo de **Cerrar Sesión** (`Logout` 🚪). Al pulsarlo, el token se borra del dispositivo y te devuelve de golpe a la pantalla inicial de Login.

¡Si llegaste hasta aquí abriendo la App y Logueándote con una cuenta de Mongo válida, acabarás en la vista de Dashboard completa, y navegando a la pestaña "Amigos" verás tu listado en vivo!
