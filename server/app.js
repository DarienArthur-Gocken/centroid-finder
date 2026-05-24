import express from 'express';
import jobRoutes from './routers/job.routes.js';

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use("/", jobRoutes);

export default app;