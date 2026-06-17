# Guía: Autenticación de Google en Android con Better-Auth

Esta guía explica a detalle la arquitectura del login con Google, por qué se toman ciertas decisiones técnicas y qué revisar si el sistema falla.

## 1. ¿Cómo funciona el flujo de Login?

1. **La App Android** lanza un _Intent_ abriendo la URL `SIGN_IN_GOOGLE` (`/api/google/start`) en el navegador web del teléfono.
2. **El Backend (`index.js`)** recibe la petición. En lugar de redirigir directamente, devuelve un HTML oculto que hace una petición **POST** a la ruta interna de Better-Auth (`/api/auth/sign-in/social`).
3. **Better-Auth** responde con la URL real de Google (que incluye el estado y los parámetros de OAuth). El HTML detecta esto y cambia la página hacia Google.
4. **Google** autentica al usuario y lo devuelve al callback del backend (`/api/google/callback`).
5. **El Backend** procesa la respuesta, le dice a Better-Auth que genere la cookie de sesión y finalmente lanza el _Deep Link_ (`fleetlog://auth?token=...`).
6. **Android (`AuthCallbackActivity.kt`)** intercepta ese enlace, guarda el token en sesión y permite al usuario usar la app.

## 2. ¿Por qué usamos un Túnel (localtunnel)?

El navegador de Android y el entorno de desarrollo web son estrictos por seguridad. Cuando se intentaba hacer esto sin HTTPS (es decir, en `http://localhost` o `http://10.0.2.2`), ocurría el famoso error **`state_not_found`**. 

Esto sucede por la **política de cookies SameSite**. Better-Auth genera una "cookie de estado" temporal para prevenir ataques (CSRF) antes de enviarte a Google. Al regresar de Google, si la cookie original no viene firmada como `Secure` (lo cual requiere **HTTPS**) o si los dominios no coinciden, Chrome elimina la cookie silenciosamente y Better-Auth rechaza el inicio de sesión por prevención de ataques. 

Al usar `localtunnel`, le damos a nuestro servidor local en Node.js una conexión cifrada y pública (`https://fleetlog-dev-fp.loca.lt`), solucionando el bloqueo de las cookies instantáneamente.

---

## 3. Guía de Solución de Problemas (Troubleshooting)

Si el flujo de Google OAuth (o el login general) deja de funcionar en el futuro, revisa paso a paso los siguientes puntos:

### A. El comando de Localtunnel
Debes asegurarte de ejecutar siempre el túnel con el mismo subdominio para que las URLs coincidan:
```powershell
npx localtunnel --port 3000 --subdomain fleetlog-dev-fp
```
> [!WARNING]
> Si Localtunnel se cae o cierras esa pestaña de PowerShell, tu app no podrá comunicarse con el backend a la hora de hacer login con Google. Debes reiniciarlo.

### B. El archivo `.env` del Backend
Abre el archivo [.env](file:///d:/Android---Miscellaneous/FletLogWSDB/FleetLogAPI/.env) y asegúrate de que la variable `BETTER_AUTH_URL` coincida exactamente con lo que te devolvió el comando localtunnel (sin una barra `/` al final).
```env
BETTER_AUTH_URL=https://fleetlog-dev-fp.loca.lt
```

### C. Configuración en la Nube (Google Cloud Console)
Si al abrir el navegador te sale el error de Google **"Access blocked: redirect_uri_mismatch"**, significa que Google no reconoce tu URL.
* Solución: Ve a tu Consola de Google Cloud, entra a tus credenciales OAuth, y asegúrate de que la "URI de redireccionamiento autorizada" sea exactamente tu URL de localtunnel más `/api/auth/callback/google`.
* Ejemplo: `https://fleetlog-dev-fp.loca.lt/api/auth/callback/google`

### D. La App de Android (`ApiConstants.kt`)
En [ApiConstants.kt](file:///d:/Android---Miscellaneous/FletLogWSDB/FleetLogDB/app/src/main/java/com/example/fleetlogdb/network/ApiConstants.kt), mantenemos separadas las URLs a propósito.
* Si **solo falla Google**, verifica que `BASE_URL_AUTH_GOOGLE` tenga tu URL de localtunnel correcta (`https://fleetlog-dev-fp.loca.lt`).
* Si **falla el Login General** (correo/contraseña), asegúrate de que `BASE_URL` esté apuntando a la IP especial del emulador: `http://10.0.2.2:3000`.

### E. Orígenes Confiables del Backend (`routes/auth.js`)
Si ves en la terminal de tu servidor errores de CORS o Better-Auth rechaza el login general devolviendo un `403 Forbidden` o `401 Unauthorized`, revisa el archivo [auth.js](file:///d:/Android---Miscellaneous/FletLogWSDB/FleetLogAPI/src/routes/auth.js). 
El bloque `trustedOrigins` debe incluir obligatoriamente tanto el túnel (que Better-Auth detecta mágicamente por el `.env`) como la IP del emulador para que el login tradicional local sea aceptado en el entorno de desarrollo:
```javascript
trustedOrigins: [
    "http://localhost:3000",
    "http://10.0.2.2:3000",
    "fleetlog://auth"
]
```

### F. Enrutamiento del Callback (`index.js`)
Si la autenticación en Google tiene éxito pero la pantalla del navegador se queda en blanco, o un endpoint da error 404, asegúrate de que en [index.js](file:///d:/Android---Miscellaneous/FletLogWSDB/FleetLogAPI/src/index.js) las rutas `/api/google/start` y `/api/google/callback` se declaren **antes** de la línea que monta el wildcard de Better-Auth (`app.all("/api/auth/*path", ...)`). Si se declaran después, Better-Auth interceptará esas rutas personalizadas y no sabrá qué hacer con ellas.
