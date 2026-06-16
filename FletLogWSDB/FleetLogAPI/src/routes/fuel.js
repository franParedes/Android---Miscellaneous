import express from 'express';
import pool from '../config/db.js';

const router = express.Router();

// OBTENER los registros de combustible
router.get('/', async (req, res) => {
    try {
        const query = `
            SELECT f.*, v.plate, v.brand 
            FROM fuel_logs f 
            JOIN vehicles v ON f.vehicle_id = v.id
        `;
        const [rows] = await pool.query(query);
        res.json(rows);
    } catch (error) {
        res.status(500).json({ error: 'Error obteniendo registros de combustible' });
    }
});

// INSERTAR un registro de combustible
router.post('/', async (req, res) => {
    const { vehicle_id, gallons, total_cost, date_filled } = req.body;
    try {
        const [result] = await pool.query(
            'INSERT INTO fuel_logs (vehicle_id, gallons, total_cost, date_filled) VALUES (?, ?, ?, ?)',
            [vehicle_id, gallons, total_cost, date_filled]
        );
        res.status(201).json({ message: 'Combustible registrado', id: result.insertId });
    } catch (error) {
        res.status(500).json({ error: 'Error guardando el registro de combustible' });
    }
});

// ELIMINAR un registro
router.delete('/:id', async (req, res) => {
    try {
        await pool.query('DELETE FROM fuel_logs WHERE id=?', [req.params.id]);
        res.json({ message: 'Registro eliminado' });
    } catch (error) {
        res.status(500).json({ error: 'Error eliminando el registro' });
    }
});

export default router;