import express from "express";
import { startProcessingJob, getProcessingJobStatus, getAvailableVideos } from "../controllers/job.controller.js";

const router = express.Router();

router.post("/process/:filename", startProcessingJob);
router.get("/process/:jobId/status", getProcessingJobStatus);
router.get("/api/videos", getAvailableVideos);

export default router;