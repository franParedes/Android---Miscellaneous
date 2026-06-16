# Guía de Estudio Técnico: Sistema FleetLog

Esta guía detalla el funcionamiento interno de la aplicación FleetLog. Está diseñada para ayudarte a defender el proyecto, entender cómo interactúan los componentes y explicar el flujo de datos desde la interfaz de usuario hasta la base de datos.

> [!TIP]
> **Consejo para la defensa:** Si te preguntan por qué usaste ciertas tecnologías (como `HttpURLConnection` en lugar de Retrofit, o `JSONObject` en lugar de Gson), responde que el objetivo del proyecto era **demostrar el dominio de las herramientas nativas de Android** para el consumo de APIs, tal como lo exige la rúbrica, evitando depender de librerías de terceros que "hacen la magia por debajo".

---

## 1. Arquitectura General (Cliente - Servidor)

El sistema sigue una arquitectura de dos capas principales (Cliente-Servidor) comunicadas a través de una API RESTful usando el formato JSON.

*   **Backend (Servidor):** `FleetLogAPI`. Construido con **Node.js** y **Express**. Actúa como intermediario seguro entre la aplicación móvil y la base de datos (MySQL/MariaDB). Expone "Endpoints" (URLs) para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar).
*   **Frontend (Cliente):** `FleetLogDB`. Una aplicación nativa de **Android** escrita en **Kotlin**. Consume la API del backend para mostrar los datos al usuario y capturar sus entradas.

---

## 2. El Backend: FleetLogAPI (Node.js)

El backend es el encargado de ejecutar las sentencias SQL reales. Nunca conectamos Android directamente a la base de datos por razones de seguridad.

### Archivos Principales:
*   `src/index.js`: Es el punto de entrada de la aplicación. Configura Express, habilita CORS (para permitir peticiones de otros orígenes), procesa los JSON entrantes y monta las rutas.
*   `src/config/db.js`: Contiene la configuración del **Connection Pool** hacia la base de datos. Usar un "pool" es vital porque mantiene múltiples conexiones abiertas y listas para usarse, mejorando drásticamente el rendimiento en lugar de abrir y cerrar una conexión por cada petición.
*   `src/routes/*.js` (`vehicles.js`, `drivers.js`, etc.): Cada archivo maneja las rutas de una entidad específica.

### ¿Cómo funciona una ruta? (Ejemplo: `vehicles.js`)
El backend utiliza los verbos HTTP estándar para definir qué acción SQL ejecutar:
1.  **GET `/`**: Ejecuta un `SELECT *`. Devuelve un JSON (un array de objetos) con todos los registros.
2.  **POST `/`**: Ejecuta un `INSERT`. Extrae los datos del `req.body` (el JSON que manda Android) y los guarda en la BD.
3.  **PUT `/:id`**: Ejecuta un `UPDATE`. Usa el ID que viene en la URL (`req.params.id`) para saber qué fila actualizar con los datos del `req.body`.
4.  **DELETE `/:id`**: Ejecuta un `DELETE` usando el ID de la URL.

> [!NOTE]
> Las consultas SQL en Node.js se hacen usando `?` (ej: `INSERT INTO tabla VALUES (?, ?)`). Esto se llama **Consultas Parametrizadas** y es la defensa principal contra ataques de Inyección SQL.

---

## 3. El Frontend: FleetLogDB (Android / Kotlin)

La aplicación móvil está estructurada separando las responsabilidades: Interfaz (Activities), Lógica de red (HttpTask) y Modelos de datos.

### 3.1. Capa de Red (Comunicación con la API)
*   **`network/ApiConstants.kt`**: Un archivo centralizado que guarda la IP del servidor y las URLs de los endpoints. Si el servidor cambia de IP, solo modificas este archivo. *(Nota: `10.0.2.2` es un alias especial en Android Studio que apunta al `localhost` de tu computadora).*
*   **`network/HttpTask.kt`**: **EL CORAZÓN DE LA RED**. Es una clase que hereda de `AsyncTask`. 
    *   *¿Por qué AsyncTask?* Android prohíbe hacer peticiones de red en el Hilo Principal (UI Thread) porque congelaría la pantalla. `AsyncTask` ejecuta la petición en un hilo secundario (`doInBackground`) y luego entrega el resultado en el hilo principal (`onPostExecute`).
    *   *¿Cómo funciona por dentro?* Usa `HttpURLConnection` de Java puro. Abre la conexión, configura el método (GET, POST...), inyecta el token de seguridad (Authorization: Bearer...), lee la respuesta del servidor con un `BufferedReader` y la devuelve como un String (que es un JSON).

### 3.2. Modelos de Datos (Entities)
*   **`model/*.kt`** (ej. `Vehicle.kt`): Son clases sencillas (Data Classes en Kotlin) que representan un registro de la base de datos en memoria. Tienen los mismos campos que las tablas SQL.

### 3.3. Adaptadores (Adapters)
*   **`ui/adapter/*.kt`** (ej. `VehicleAdapter.kt`): Dado que por rúbrica no se usa `RecyclerView`, usamos `ListView`. El `ListView` es ciego, no sabe cómo pintar datos. El **Adapter** (que hereda de `ArrayAdapter`) es el traductor: toma la lista de objetos `Vehicle`, infla un layout XML (`item_vehicle.xml`) por cada uno, y pone los textos (marca, placa) en los `TextViews` correspondientes dentro del método `getView()`.

### 3.4. Vistas (Activities)
*   **`MainActivity.kt`** (y las demás Activities): Son los controladores de pantalla.
    1.  **Petición inicial:** En el `onCreate`, llaman a `loadVehicles()` que ejecuta un `HttpTask` (GET).
    2.  **Parseo Nativo:** Cuando el backend responde con un String JSON, la Activity usa `JSONArray` y `JSONObject` nativos de Android para extraer los campos uno por uno (ej. `obj.getString("brand")`) y llenar una lista de modelos. Luego le avisa al adapter que refresque la vista (`adapter.notifyDataSetChanged()`).
    3.  **CRUD UI:** Implementan diálogos flotantes (`AlertDialog.Builder`) para los formularios de Crear/Editar, validando que los campos no estén vacíos antes de enviarlos por `HttpTask` (POST o PUT).

---

## 4. Flujos Clave Explicados Paso a Paso

### Flujo A: Editar un Vehículo (El flujo completo)
1.  **Interacción UI:** El usuario hace un "click largo" en la lista. Se abre el Menú Contextual (`registerForContextMenu`).
2.  **Selección:** Elige "Editar". El sistema obtiene el objeto `Vehicle` de esa posición.
3.  **Diálogo:** Se abre el `AlertDialog` y los `EditText` se **precargan** con los datos del objeto (ej. `etBrand.setText(vehicle.brand)`).
4.  **Validación y JSON:** Al dar "Guardar", se verifica que los campos obligatorios no estén vacíos. Se crea un objeto `JSONObject` y se rellena con `put("brand", valor)`. Luego se convierte a String (`.toString()`).
5.  **Envío a la API:** Se instancia un `HttpTask` con el método `"PUT"`, la URL del vehículo (`/api/vehicles/5`) y el String JSON en el cuerpo.
6.  **Backend:** Node.js recibe la petición PUT en `vehicles.js`, extrae los datos, y ejecuta el `UPDATE` en MySQL. Retorna un mensaje de éxito.
7.  **Actualización UI:** Android recibe el mensaje de éxito, muestra un `Toast` y vuelve a llamar a `loadVehicles()` para refrescar la lista con el dato modificado.

### Flujo B: Selección de Imágenes (La Galería)
> [!IMPORTANT]
> Este es un punto técnico fuerte. El manejo moderno de imágenes sin pedir permisos invasivos.

1.  **Lanzador Moderno:** Al tocar "Seleccionar Imagen", se llama a `imagePickerLauncher.launch("image/*")`. Esto usa `ActivityResultContracts.GetContent()`, una API moderna de Android.
2.  **Permisos:** **No requiere permisos en el Manifest** (`READ_EXTERNAL_STORAGE`). ¿Por qué? Porque el sistema operativo abre su propio selector seguro. Cuando el usuario elige una foto, el OS le da a la app un acceso temporal (vía una `Uri`) solo a esa foto específica.
3.  **Procesamiento:** La clase utilitaria `ImageUtils.kt` toma esa `Uri`, la comprime (para no saturar la red ni la BD) y la convierte a un **String en formato Base64**.
4.  **Base64:** El Base64 es la imagen convertida en texto. Este texto largo se guarda en el JSON (`put("imageBase64", texto)`), viaja al backend y se guarda en un campo de texto largo (LONGTEXT) en la base de datos.
5.  **Renderizado:** Al leer de la API, Android hace lo inverso: toma el String Base64, lo decodifica a un arreglo de bytes (`ByteArray`) y lo convierte en un `Bitmap` para ponerlo en el `ImageView`.

---

## 5. Preguntas Comunes en la Defensa

*   **P: ¿Por qué usaste `HttpTask` (AsyncTask) si está deprecado?**
    *   *R:* Por los requerimientos de la rúbrica. Se pedía usar `HttpURLConnection` y parsing nativo de JSON. `AsyncTask` es la forma clásica de hacer que `HttpURLConnection` no bloquee el hilo principal en Android tradicional sin introducir librerías externas modernas como Coroutines + Retrofit.
*   **P: ¿Cómo validas que el usuario no envíe campos vacíos?**
    *   *R:* Usando el evento `setOnShowListener` del `AlertDialog`. En lugar de dejar que el diálogo cierre automáticamente al presionar el botón positivo, interceptamos el click. Validamos los `EditText` uno por uno usando `.trim().isEmpty()`. Si hay un error, mostramos el `.error` en el campo y usamos `return` para abortar el envío sin cerrar el diálogo.
*   **P: Si cambio la BD, ¿qué debo modificar en Android?**
    *   *R:* Absolutamente nada en Android (siempre que la API mantenga las mismas respuestas JSON). Android solo se comunica con la API. Esa es la ventaja de la arquitectura Cliente-Servidor: la aplicación móvil está abstraída e ignorante del motor de base de datos específico que usa el backend.
