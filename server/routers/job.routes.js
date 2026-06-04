import express from "express";
import { startProcessingJob, getProcessingJobStatus, downloadJobResult } from "../controllers/job.controller.js";

const router = express.Router();

router.post("/process/:filename", startProcessingJob);
router.get("/process/:jobId/status", getProcessingJobStatus);
router.get("/download/:jobId", downloadJobResult);

export default router;