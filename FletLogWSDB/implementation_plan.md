# Plan de Implementación: FleetLog Android (Migración a Web Service)

## Contexto

La app **FleetLogDB** ya existe con CRUD local via SQLite. Esta migración reemplaza la capa de datos para consumir la **REST API Node.js** (`FleetLogAPI`) en lugar de la base de datos local.

La API usa **Better-Auth** en `POST /api/auth/sign-in/email` y `POST /api/auth/sign-up/email`. El token de sesión se extrae de la cookie `better-auth.session_token` de la respuesta.

## Arquitectura del Backend (resumen)

| Endpoint | Método | Body / Params |
|---|---|---|
| `/api/auth/sign-up/email` | POST | `{ email, password, name }` |
| `/api/auth/sign-in/email` | POST | `{ email, password }` → devuelve cookie con token |
| `/api/vehicles` | GET/POST | `{ brand, model, plate, year, color, mileage, status, imageBase64, isPickup }` |
| `/api/vehicles/:id` | PUT/DELETE | mismo body para PUT |
| `/api/drivers` | GET/POST/DELETE | `{ name, license_number, phone }` |
| `/api/maintenance` | GET/POST | `{ vehicle_id, description, cost, service_date }` |
| `/api/fuel` | GET/POST/DELETE | `{ vehicle_id, gallons, total_cost, date_filled }` |

## Estructura de Archivos a Crear/Modificar

### Archivos Nuevos

#### model/
- `Driver.kt` — POJO Conductor
- `MaintenanceLog.kt` — POJO Mantenimiento  
- `FuelLog.kt` — POJO Combustible
- **MODIFICAR** `Vehicle.kt` — Agregar campos: `color`, `mileage`, `status`, `imageBase64`

#### network/
- `ApiConstants.kt` — URL base y endpoints
- `HttpTask.kt` — AsyncTask genérico para HTTP (GET/POST/PUT/DELETE con HttpURLConnection)

#### utils/
- `SessionManager.kt` — SharedPreferences para token y email del usuario
- `ImageUtils.kt` — Bitmap ↔ Base64

#### ui/adapter/
- **MODIFICAR** `VehicleAdapter.kt` — Decodificar Base64 para ImageView
- `DriverAdapter.kt` — Adaptador para conductores
- `MaintenanceAdapter.kt` — Adaptador para mantenimientos
- `FuelAdapter.kt` — Adaptador para combustible

#### Activities
- `LoginActivity.kt` — Login + Registro con Better-Auth
- **CONVERTIR** `MainActivity.kt` — Consume `/api/vehicles` via AsyncTask
- `DriversActivity.kt` — CRUD conductores
- `MaintenanceActivity.kt` — CRUD mantenimientos
- `FuelActivity.kt` — CRUD combustible

### Layouts XML Nuevos/Modificados
- `activity_login.xml`
- `activity_drivers.xml`
- `activity_maintenance.xml`
- `activity_fuel.xml`
- `item_driver.xml`
- `item_maintenance.xml`
- `item_fuel.xml`
- `dialog_driver_form.xml`
- `dialog_maintenance_form.xml`
- `dialog_fuel_form.xml`
- **MODIFICAR** `dialog_vehicle_form.xml` — Agregar campos color, km, status, imagen
- **MODIFICAR** `item_vehicle.xml` — Mostrar imagen Base64 en ImageView
- **MODIFICAR** `menu_main.xml` — Agregar ítems para Conductores/Mant./Combustible

### AndroidManifest.xml
- Agregar `INTERNET` permission
- Agregar `LoginActivity` como launcher
- Registrar las 3 nuevas Activities

## Restricciones de la Rúbrica Cumplidas

- ✅ `HttpURLConnection` dentro de clases `AsyncTask`
- ✅ Parseo con `JSONObject` / `JSONArray` nativos
- ✅ `ListView` (prohibido RecyclerView)
- ✅ `ArrayAdapter` con `getView()` + `LayoutInflater`
- ✅ `onCreateOptionsMenu` + `registerForContextMenu`
- ✅ `AlertDialog.Builder` para formularios y confirmaciones
- ✅ `SharedPreferences` para token de sesión
- ✅ 4 data classes POJO
- ✅ Gestión de imágenes Base64 con galería

## Orden de Generación

1. POJOs (4 data classes)
2. `SessionManager.kt`
3. `ApiConstants.kt`
4. `HttpTask.kt` (AsyncTask genérico)
5. `ImageUtils.kt`
6. `AndroidManifest.xml` actualizado
7. Menús XML
8. `LoginActivity.kt` + layout
9. `VehicleAdapter.kt` actualizado + layouts de vehículos
10. `MainActivity.kt` actualizado
11. Drivers (adapter + activity + layouts)
12. Maintenance (adapter + activity + layouts)
13. Fuel (adapter + activity + layouts)
