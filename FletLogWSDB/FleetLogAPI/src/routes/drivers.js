import express from 'express';
import pool from '../config/db.js';

const router = express.Router();

router.get('/', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM drivers');
        res.json(rows);
    } catch (error) {
        res.status(500).json({ error: 'Error obteniendo conductores' });
    }
});

router.post('/', async (req, res) => {
    const { name, license_number, phone } = req.body;
    try {
        const [result] = await pool.query('INSERT INTO drivers (name, license_number, phone) VALUES (?, ?, ?)', [name, license_number, phone]);
        res.status(201).json({ message: 'Conductor creado', id: result.insertId });
    } catch (error) {
        res.status(500).json({ error: 'Error guardando el conductor' });
    }
});

router.put('/:id', async (req, res) => {
    const { name, license_number, phone } = req.body;
    try {
        await pool.query(
            'UPDATE drivers SET name=?, license_number=?, phone=? WHERE id=?',
            [name, license_number, phone, req.params.id]
        );
        res.json({ message: 'Conductor actualizado' });
    } catch (error) {
        res.status(500).json({ error: 'Error actualizando el conductor' });
    }
});

router.delete('/:id', async (req, res) => {
    try {
        await pool.query('DELETE FROM drivers WHERE id=?', [req.params.id]);
        res.json({ message: 'Conductor eliminado' });
    } catch (error) {
        res.status(500).json({ error: 'Error eliminando el conductor' });
    }
});

export default router;