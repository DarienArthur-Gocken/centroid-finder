import express from "express";
import { getThumbnail } from "../controllers/thumbnail.controller.js";

const router = express.Router();

router.get("/thumbnail/:filename", getThumbnail);

export default router;