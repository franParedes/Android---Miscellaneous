import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import vehicleRoutes from '../src/routes/vehicles.js';
import driverRoutes from '../src/routes/drivers.js';
import maintenanceRoutes from '../src/routes/maintenance.js';
import fuelRoutes from '../src/routes/fuel.js';
import { auth } from '../src/routes/auth.js';
import { toNodeHandler } from 'better-auth/node';

dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

// =====================================================================
// PASO 1: Nuestras rutas custom de Google OAuth para Android
// DEBEN ir ANTES del handler de Better-Auth para evitar que el wildcard
// /api/auth/*path las intercepte y devuelva 404.
// Las movemos a /api/google/* para evitar el conflicto.
// =====================================================================

// Inicia el flujo OAuth de Google. Android abre esta URL en el browser.
// Usamos una página HTML con redirect en JS para saltarnos la advertencia de ngrok.
app.get("/api/google/start", (req, res) => {
    const callbackURL = `${process.env.BETTER_AUTH_URL}/api/google/callback`;
    
    res.setHeader("Content-Type", "text/html");
    res.send(`<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Redirigiendo a Google...</title>
</head>
<body>
  <p>Conectando con Google...</p>
  <script>
    fetch('${process.env.BETTER_AUTH_URL}/api/auth/sign-in/social', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        provider: 'google',
        callbackURL: '${callbackURL}'
      })
    })
    .then(res => res.json())
    .then(data => {
      if (data.url) {
        window.location.replace(data.url);
      } else if (data.error) {
        document.body.innerHTML = "Error: " + data.error.message;
      } else {
        document.body.innerHTML = "Error inesperado al iniciar OAuth.";
      }
    })
    .catch(err => {
      document.body.innerHTML = "Error de conexión: " + err.message;
    });
  </script>
</body>
</html>`);
});

// Recibe el control después de que Google y Better-Auth completan el OAuth.
// Better-Auth ya creó la sesión (cookie). Extraemos el token y redirigimos
// al deep link de Android: fleetlog://auth?token=...
app.get("/api/google/callback", async (req, res) => {
    try {
        const session = await auth.api.getSession({ headers: req.headers });

        if (session && session.session && session.session.token) {
            const token = session.session.token;
            const email = session.user?.email || "";
            // El OS de Android intercepta este deep link y lanza AuthCallbackActivity
            res.redirect(`fleetlog://auth?token=${token}&email=${encodeURIComponent(email)}`);
        } else {
            res.redirect("fleetlog://auth?error=no_session");
        }
    } catch (error) {
        console.error("Error en google/callback:", error);
        res.redirect("fleetlog://auth?error=server_error");
    }
});

// =====================================================================
// PASO 2: Better-Auth maneja TODO bajo /api/auth/* (sign-in, sign-up,
// callback de Google, etc.). Va DESPUÉS de nuestras rutas custom.
// =====================================================================
app.all("/api/auth/*path", toNodeHandler(auth));

// =====================================================================
// RUTAS DE LA APLICACIÓN
// =====================================================================
app.use('/api/vehicles', vehicleRoutes);
app.use('/api/drivers', driverRoutes);
app.use('/api/maintenance', maintenanceRoutes);
app.use('/api/fuel', fuelRoutes);

app.get('/', (req, res) => {
    res.json({ message: 'Bienvenido al FleetLog API Web Service' });
});

app.listen(port, () => {
    console.log(`Servidor FleetLog corriendo en http://localhost:${port}`);
    console.log(`Ngrok URL: ${process.env.BETTER_AUTH_URL}`);
    console.log(`Google OAuth Android → ${process.env.BETTER_AUTH_URL}/api/google/start`);
});