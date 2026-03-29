const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const app = express();
const PORT = 3000;

app.use(cors());
app.use(bodyParser.json());
app.use(express.static('public'));

let gear = [
    { id: 1, name: "Hiking Tent (2 Person)", price: 1500.0, image: "tent.jpg" },
    { id: 2, name: "Hiking Backpack (60L)", price: 800.0, image: "backpack.jpg" },
    { id: 3, name: "Portable Camping Stove", price: 1200.0, image: "stove.jpg" },
    { id: 4, name: "Sleeping Bag", price: 500.0, image: "bag.jpg" },
    { id: 5, name: "Hiking Boots (Pair)", price: 2500.0, image: "boots.jpg" }
];

let orders = [];

// API Endpoints
app.get('/api/gear', (req, res) => {
    res.json(gear);
});

app.post('/api/checkout', (req, res) => {
    const order = req.body;
    order.id = orders.length + 1;
    order.date = new Date().toLocaleString();
    orders.push(order);
    console.log("New Order Received:", order);
    res.status(201).send({ message: "Order processed successfully" });
});

app.get('/api/admin/orders', (req, res) => {
    res.json(orders);
});

app.listen(PORT, () => {
    console.log(`TripBadu Server running on http://localhost:${PORT}`);
});
