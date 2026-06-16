import express from 'express';
import pool from '../config/db.js';

const router = express.Router();

router.get('/', async (req, res) => {
    try {
        // Unimos el mantenimiento con la tabla de vehículos para traer la placa
        const query = `
            SELECT m.*, v.plate, v.brand 
            FROM maintenance_logs m 
            JOIN vehicles v ON m.vehicle_id = v.id
        `;
        const [rows] = await pool.query(query);
        res.json(rows);
    } catch (error) {
        res.status(500).json({ error: 'Error obteniendo mantenimientos' });
    }
});

router.post('/', async (req, res) => {
    const { vehicle_id, description, cost, service_date } = req.body;
    try {
        const [result] = await pool.query(
            'INSERT INTO maintenance_logs (vehicle_id, description, cost, service_date) VALUES (?, ?, ?, ?)',
            [vehicle_id, description, cost, service_date]
        );
        res.status(201).json({ message: 'Mantenimiento registrado', id: result.insertId });
    } catch (error) {
        res.status(500).json({ error: 'Error guardando el mantenimiento' });
    }
});

router.put('/:id', async (req, res) => {
    const { vehicle_id, description, cost, service_date } = req.body;
    try {
        await pool.query(
            'UPDATE maintenance_logs SET vehicle_id=?, description=?, cost=?, service_date=? WHERE id=?',
            [vehicle_id, description, cost, service_date, req.params.id]
        );
        res.json({ message: 'Mantenimiento actualizado' });
    } catch (error) {
        res.status(500).json({ error: 'Error actualizando el mantenimiento' });
    }
});

router.delete('/:id', async (req, res) => {
    try {
        await pool.query('DELETE FROM maintenance_logs WHERE id=?', [req.params.id]);
        res.json({ message: 'Mantenimiento eliminado' });
    } catch (error) {
        res.status(500).json({ error: 'Error eliminando el mantenimiento' });
    }
});

export default router;