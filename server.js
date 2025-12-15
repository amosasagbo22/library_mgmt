const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');

const app = express();
const PORT = 5000;

// Middleware
app.use(bodyParser.json());

// MongoDB Connection
mongoose.connect('mongodb+srv://mongoSecretUrl/?retryWrites=true&w=majority&appName=libraryDB', {
    appName: 'libraryDB'
})
    .then(() => console.log('Connected to MongoDB Atlas!'))
    .catch(err => console.error('Error connecting to MongoDB Atlas:', err));

// Import Routes
const bookRoutes = require('./src/library-api/routes/bookRoutes');
const memberRoutes = require('./src/library-api/routes/memberRoutes');
const staffRoutes = require('./src/library-api/routes/staffRoutes');
// API Routes
app.use('/api/books', bookRoutes);
app.use('/api/members', memberRoutes);
app.use('/api/staffs', staffRoutes);

// Start Server

app.listen(PORT, () => console.log(`Server running on http://localhost:${PORT}`));
