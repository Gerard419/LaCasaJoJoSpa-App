# La casa JoJo

App Android (Kotlin + Jetpack Compose) para armar el menú del día, hacer pedidos y obtener la boleta.
Arquitectura MVVM: ViewModel + StateFlow por pantalla, repositorio con DataStore (Gson) y Navigation Compose.

## Integrantes

- Matias Tiznado
- Juan Pablo Asenjo
- Gerard Foix

Asignatura: DSY1105 Desarrollo de Aplicaciones Móviles · Docente: Nancy Bernal

## Cuentas de prueba (se crean solas al primer inicio)
| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@lacasajojo.cl | Admin123 |
| Dueño | dueno@lacasajojo.cl | Dueno123 |
| Cliente | se registra desde la app ("Regístrate aquí") | mínimo 8 caracteres, letras y números |

## Qué hace cada tipo de usuario
- **Administrador** (`AdminScreen`): crea cuentas de dueño y bloquea o activa usuarios.
- **Dueño** (`DuenoScreen`, `PlatoFormScreen`): CRUD de platos con precio, agregados y ensaladas; marca lo que está disponible hoy (menú del día); ve los pedidos y cambia su estado.
- **Cliente** (`ClienteScreen`, `ArmarPlatoScreen`, `BoletaScreen`): ve el menú del día agrupado por precio, arma su plato (1 o 2 agregados + 1 ensalada), usa el carrito, confirma y obtiene la boleta; ve su historial.

## Recursos nativos
- **Cámara** (`PlatoFormScreen`): el dueño saca una foto del plato. El permiso `CAMERA` se declara en `AndroidManifest.xml` y se pide al tocar el botón; si se rechaza se muestra un aviso (con acceso a los ajustes) y el plato se puede guardar sin foto. La foto se guarda en el almacenamiento interno y se muestra en el menú del dueño, en el del cliente y al armar el plato.
- **Vibración** (`LoginScreen`, `ClienteScreen`): vibra al fallar el ingreso y al confirmar un pedido. El permiso `VIBRATE` es normal (se concede al instalar).

## Animaciones
- Cambio de color y tamaño de los botones al presionarlos (`BotonJoJo`) y del color de los íconos de los campos.
- Aparición suave de los mensajes de error con `AnimatedVisibility` (`CampoTexto`, `MensajeError`).
- Transiciones entre pantallas en el `NavHost` (`Navigation.kt`).
- Animación de elementos en las listas con `animateItem` (dueño, administrador y cliente).

## Notas
- La boleta es un comprobante interno; los precios incluyen IVA (neto = total / 1,19).
- Datos guardados localmente con DataStore detrás de un repositorio (sin servidor). Para una versión real habría que pasar a una base de datos en la nube.
