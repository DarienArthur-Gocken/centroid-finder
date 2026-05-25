import express from "express";
import { startProcessingJob, getProcessingJobStatus } from "../controllers/job.controller.js";

const router = express.Router();

router.post("/process/:filename", startProcessingJob);
router.get("/process/:jobId/status", getProcessingJobStatus);

export default router;