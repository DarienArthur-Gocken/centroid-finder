import express from "express";
import { getAvailableVideos } from "../controllers/video.controller.js";

const router = express.Router();

router.get("/videos", getAvailableVideos);

export default router;