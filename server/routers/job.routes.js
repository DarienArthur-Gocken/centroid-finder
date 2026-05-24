import express from "express";

import { startProcessingJob } from "../controllers/job.controller.js";

const router = express.Router();

router.post("/process/:filename", startProcessingJob);

export default router;