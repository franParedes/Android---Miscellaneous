# Documentación del Proyecto: FleetLogDB

Este documento describe la estructura, arquitectura y funcionamiento del proyecto Android **FleetLogDB**. El proyecto sigue un estilo de desarrollo clásico (old-school) en Android, cumpliendo con los requisitos académicos establecidos (uso de `SQLiteOpenHelper`, `ListView`, adaptadores clásicos y ejecución síncrona).

## 1. Arquitectura General
El proyecto es una aplicación **CRUD** (Crear, Leer, Actualizar, Eliminar) para la gestión de un registro de vehículos. La arquitectura está estructurada en los siguientes paquetes bajo `com.example.fleetlogdb`:

*   **`model`**: Contiene la clase de datos que representa la entidad del dominio.
*   **`data`**: Encargado de la persistencia de datos local usando SQLite directamente.
*   **`ui.adapter`**: Contiene el adaptador personalizado para conectar la colección de datos con la interfaz de usuario.
*   **Raíz del paquete (`com.example.fleetlogdb`)**: Contiene la actividad principal (`MainActivity`) que orquesta la interfaz, los diálogos y las operaciones de base de datos.

## 2. Componentes Principales

### 2.1 Modelo de Datos (`Vehicle.kt`)
Clase de datos (`data class`) sencilla que mapea la tabla de la base de datos.
*   **Atributos**: `id` (Entero, autoincremental), `brand` (Marca), `model` (Modelo), `plate` (Placa), `year` (Año), `isPickup` (Entero usado como booleano: 1 = Sí, 0 = No).

### 2.2 Persistencia de Datos (`VehicleSQLHelper.kt`)
Clase que hereda de `SQLiteOpenHelper`.
*   **Base de datos**: Crea una base de datos local llamada `"FleetLogDB"`.
*   **Tablas**: Crea la tabla `VEHICLES` definiendo explícitamente sus columnas e indicando `_id` como clave primaria.
*   **Actualizaciones**: Si la versión cambia (`onUpgrade`), simplemente destruye la tabla y la vuelve a crear (estilo clásico para entornos de desarrollo).

### 2.3 Interfaz de Usuario y Lógica (`MainActivity.kt`)
La pantalla principal de la aplicación. Maneja prácticamente toda la lógica de presentación e interacción.
*   **Listado (ListView)**: Usa un componente clásico `ListView` en lugar del moderno `RecyclerView`.
*   **Flujo de lectura (`loadData`)**: 
    1. Obtiene una base de datos de lectura (`readableDatabase`).
    2. Ejecuta un `query` clásico obteniendo un `Cursor`.
    3. Itera sobre el cursor reconstruyendo objetos `Vehicle` y llenando una lista.
    4. Refresca el `VehicleAdapter` asignándolo de nuevo al `ListView`.
    > [!NOTE]
    > Fiel al estilo clásico, las consultas a la base de datos se hacen directamente en el **hilo principal** (UI Thread).
*   **Formulario de creación/edición (`showVehicleFormDialog`)**: 
    *   Infla una vista personalizada (`dialog_vehicle_form.xml`) dentro de un `AlertDialog`.
    *   Dependiendo de si recibe un objeto `Vehicle` o `null`, el diálogo funciona para actualizar o crear un nuevo registro.
    *   Guarda los datos mapeándolos con `ContentValues` y ejecutando `db.insert` o `db.update`.
*   **Menús Clásicos**:
    *   **Menú de Opciones**: En el `ActionBar` (esquina superior derecha) se infla un botón (+) para añadir un nuevo vehículo.
    *   **Menú Contextual**: Configurado para el `ListView`. Al hacer un "click largo" sobre un elemento de la lista, se abre un menú emergente con las opciones "Editar" y "Eliminar".

### 2.4 Adaptador Personalizado (`VehicleAdapter.kt`)
Clase que hereda de `ArrayAdapter<Vehicle>`.
*   Sobrescribe el método `getView` inflando un diseño personalizado (`item_vehicle.xml`) por cada fila.
*   Recupera las referencias a las vistas (`TextView`, `ImageView`) utilizando el clásico `findViewById`.
*   Usa lógica condicional para mostrar un icono de camioneta (`ic_pickup`) o un carro (`ic_car`) basándose en el valor numérico `isPickup`.

## 3. Interfaces Gráficas (Layouts)
Las vistas están definidas en XML usando `ConstraintLayout` y vistas clásicas:
*   `activity_main.xml`: Contiene el `ListView` para mostrar la lista de vehículos.
*   `item_vehicle.xml`: Define cómo se ve cada elemento individual de la lista (icono, textos para marca, modelo, año y placa).
*   `dialog_vehicle_form.xml`: Formulario con `EditText`s y un `CheckBox` para rellenar la información del vehículo dentro del `AlertDialog`.

## 4. Alineación con los Requisitos (Estilo "Old-School")
El proyecto cumple a cabalidad con el estilo de código solicitado por los requerimientos académicos:
1.  **Sin inyección de dependencias** (No Dagger/Hilt). Instanciación directa.
2.  **Uso de ListView y ArrayAdapter**, sin implementar los patrones modernos como `ViewHolder` estricto (aunque se recicla el `convertView` de forma muy básica).
3.  **Uso directo de `SQLiteOpenHelper` y `Cursor`**, sin ORM modernos como Room.
4.  **Flujos síncronos**: Las operaciones a la base de datos ocurren en el hilo principal sin usar Coroutines, Flow ni `AsyncTask` (esto último es aceptable si la lista es corta).
5.  **Eventos clásicos**: Uso de `AdapterView.OnItemClickListener` y `ContextMenu` tradicionales.

---

> [!TIP]
> **Próximos pasos (Consumo del Web Service)**
> Según se indica, el siguiente paso será migrar o añadir la lógica para consumir un Web Service (REST). Para mantener este mismo estilo exigido por el profesor, se recomendará utilizar clases clásicas como `HttpURLConnection` y `AsyncTask` (o un hilo/handler básico), o en su defecto, la librería **Volley**, la cual era el estándar en esa generación de desarrollo en Android, consumiendo la respuesta y procesándola manualmente mediante `JSONObject` y `JSONArray`.
