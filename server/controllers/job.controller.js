import { startJob } from "../services/job.service.js";

export async function startProcessingJob(req, res) {
    try {
        const { filename } = req.params;
        const { targetColor, threshold } = req.query;

        if (!targetColor || !threshold) {
            return res.status(400).json({
                error: "Missing targetColor or threshold query parameter."
            });
        }

        const jobId = await startJob({
            filename,
            targetColor,
            threshold
        });

        return res.status(202).json({
            "jobId": jobId
        });

    } catch (err) {

        return res.status(500).json({
            error: "Error starting job"
        });
    }
}