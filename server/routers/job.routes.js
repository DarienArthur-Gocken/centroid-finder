import express from "express";

import { startProcessingJob, getProcessingStatus } from "../controllers/processController.js";

const router = express.Router();

router.post("/process/:filename", startProcessingJob);

export default router;