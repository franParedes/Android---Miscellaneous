import { betterAuth } from "better-auth";
import pool from "../config/db.js";

// Configuramos Better-Auth
export const auth = betterAuth({
    database: pool,
    // MEJOR_AUTH_URL debe apuntar al servidor real (no al emulador)
    baseURL: process.env.BETTER_AUTH_URL,
    // Orígenes de confianza: incluimos el deep link de Android
    trustedOrigins: [
        "http://localhost:3000",
        "http://10.0.2.2:3000",
        "fleetlog://auth"
    ],
    emailAndPassword: {
        // Habilitamos el login tradicional (Correo/Contraseña)
        enabled: true,
        autoSignIn: true
    },
    socialProviders: {
        google: {
            clientId: process.env.GOOGLE_CLIENT_ID,
            clientSecret: process.env.GOOGLE_CLIENT_SECRET
        }
    }
});