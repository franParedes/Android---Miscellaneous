import { betterAuth } from "better-auth";
import pool from "../config/db.js";

// Configuramos Better-Auth
export const auth = betterAuth({
    database: pool,

    emailAndPassword: {
        // Habilitamos el login tradicional (Correo/Contraseña)
        enabled: true,
        autoSignIn: true
    },
    socialProviders: {
        // Aquí habilitamos Facebook y Google
        facebook: {
            clientId: process.env.FACEBOOK_CLIENT_ID,
            clientSecret: process.env.FACEBOOK_CLIENT_SECRET
        },
        google: {
            clientId: process.env.GOOGLE_CLIENT_ID,
            clientSecret: process.env.GOOGLE_CLIENT_SECRET
        }
    }
});