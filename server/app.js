import express from 'express';
import jobRoutes from './routers/job.routes.js';
import videoRoutes from "./routers/video.routes.js";
import thumbnailRoutes from "./routers/thumbnail.routes.js";
import resultRoutes from "./routers/result.routes.js";

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use("/", jobRoutes);
app.use("/", thumbnailRoutes);
app.use("/", resultRoutes);
app.use("/api", videoRoutes);

export default app;