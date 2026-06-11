import express from "express";
import { getCsvByName, getLatestCsv, getMetadataByJobId } from "../controllers/latestCsv.controller.js";

const router = express.Router();

router.get("/results/:filename", getCsvByName);
router.get("/latest/:filename", getLatestCsv);
router.get("/metadata/:jobId", getMetadataByJobId);

export default router;
