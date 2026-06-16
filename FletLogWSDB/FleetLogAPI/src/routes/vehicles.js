import express from 'express';
import pool from '../config/db.js';

const router = express.Router();

// OBTENER TODOS LOS VEHÍCULOS
router.get('/', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM vehicles');
        const formattedRows = rows.map(row => ({
            id: row.id,
            brand: row.brand,
            model: row.model,
            plate: row.plate,
            year: row.year,
            color: row.color,
            mileage: row.mileage,
            status: row.status,
            imageBase64: row.image_base64,
            isPickup: row.is_pickup
        }));
        res.json(formattedRows);
    } catch (error) {
        res.status(500).json({ error: 'Error obteniendo los vehículos' });
    }
});

// INSERTAR
router.post('/', async (req, res) => {
    const { brand, model, plate, year, color, mileage, status, imageBase64, isPickup } = req.body;
    try {
        const query = `
            INSERT INTO vehicles 
            (brand, model, plate, year, color, mileage, status, image_base64, is_pickup) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        `;
        const [result] = await pool.query(query, [brand, model, plate, year, color, mileage, status, imageBase64, isPickup]);
        res.status(201).json({ message: 'Vehículo creado', id: result.insertId });
    } catch (error) {
        res.status(500).json({ error: 'Error guardando el vehículo' });
    }
});

// EDITAR
router.put('/:id', async (req, res) => {
    const { id } = req.params;
    const { brand, model, plate, year, color, mileage, status, imageBase64, isPickup } = req.body;
    try {
        const query = `
            UPDATE vehicles 
            SET brand=?, model=?, plate=?, year=?, color=?, mileage=?, status=?, image_base64=?, is_pickup=? 
            WHERE id=?
        `;
        await pool.query(query, [brand, model, plate, year, color, mileage, status, imageBase64, isPickup, id]);
        res.json({ message: 'Vehículo actualizado' });
    } catch (error) {
        res.status(500).json({ error: 'Error actualizando el vehículo' });
    }
});

// ELIMINAR
router.delete('/:id', async (req, res) => {
    const { id } = req.params;
    try {
        await pool.query('DELETE FROM vehicles WHERE id=?', [id]);
        res.json({ message: 'Vehículo eliminado' });
    } catch (error) {
        res.status(500).json({ error: 'Error eliminando el vehículo' });
    }
});

export default router;