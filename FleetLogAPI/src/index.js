import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import vehicleRoutes from '../src/routes/vehicles.js';
import driverRoutes from '../src/routes/drivers.js';
import maintenanceRoutes from '../src/routes/maintenance.js';
import fuelRoutes from '../src/routes/fuel.js';
import { auth } from '../src/routes/auth.js';
// IMPORTANTE: Agregamos el adaptador para Node.js
import { toNodeHandler } from 'better-auth/node';

dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

// Middleware para entender JSON y permitir peticiones de otras apps (CORS)
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

// --- RUTAS DE AUTENTICACIÓN (Better-Auth) ---
// Better-Auth manejará automáticamente todas las rutas que empiecen con /api/auth
// Ejemplo: /api/auth/sign-in, /api/auth/sign-up, /api/auth/sign-in/facebook
app.all("/api/auth/*path", toNodeHandler(auth));

// --- RUTAS DE NUESTRA APLICACIÓN (Vehículos) ---
app.use('/api/vehicles', vehicleRoutes);
app.use('/api/drivers', driverRoutes);
app.use('/api/maintenance', maintenanceRoutes);
app.use('/api/fuel', fuelRoutes);

// Ruta de prueba para saber si el servidor está vivo
app.get('/', (req, res) => {
    res.json({ message: 'Bienvenido al FleetLog API Web Service' });
});

// Arrancar el servidor
app.listen(port, () => {
    console.log(`Servidor FleetLog corriendo en http://localhost:${port}`);
    console.log(`Ruta de vehículos: http://localhost:${port}/api/vehicles`);
});