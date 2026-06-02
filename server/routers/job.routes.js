import express from "express";
import { startProcessingJob, getProcessingJobStatus, getAvailableVideos, getThumbnail, downloadJobResult } from "../controllers/job.controller.js";

const router = express.Router();

router.post("/process/:filename", startProcessingJob);
router.get("/process/:jobId/status", getProcessingJobStatus);
router.get("/download/:jobId", downloadJobResult);
router.get("/api/videos", getAvailableVideos);
router.get("/thumbnail/:filename", getThumbnail);

export default router;