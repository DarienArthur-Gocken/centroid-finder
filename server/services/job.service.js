import path from "path";
import { spawn } from "child_process";
import { v4 } from "uuid";

const jobs = {};

export function createJob(jobId, filename) {
    jobs[jobId] = {
        filename,
        status: "processing",
        progress: 0,
        result: null,
        error: null
    };
}

export function updateJobProgress(jobId, progress) {
    if (jobs[jobId]) {
        jobs[jobId].progress = progress;
    }
}

export function completeJob(jobId, resultPath) {
    jobs[jobId].status = "done";
    jobs[jobId].progress = 100;
    jobs[jobId].result = resultPath;
}

export function errorJob(jobId, error) {
    jobs[jobId].status = "error";
    jobs[jobId].error = error;
}

export function getJob(jobId) {
    return jobs[jobId];
}

export async function startJob({ filename, targetColor, threshold }) {
    const jobId = v4();

    createJob(jobId, filename);

    const inputVideo = path.join(process.env.VIDEO_DIR, filename);

    const outputCsv = path.join(process.env.RESULT_DIR, `${jobId}.csv`);

    const javaProcess = spawn("java", [
        "-jar",
        process.env.PROCESSOR_JAR,
        inputVideo,
        outputCsv,
        targetColor,
        threshold
    ]);
    javaProcess.stdout.on("data", (data) => {
        const output = data.toString();
        console.log(`[JAVA OUT] ${output}`);

        const match = output.match(/PROGRESS:(\d+)/);

        if (match) {
            updateJobProgress(jobId, Number(match[1]));
        }
    });

    javaProcess.stderr.on("data", (data) => {
        console.error(`[JAVA ERR] ${data.toString()}`);
    });

    javaProcess.on("error", (err) => {
        errorJob(jobId, err.message);
    });

    javaProcess.on("close", (code) => {
        if (code === 0) completeJob(jobId, outputCsv);
        else errorJob(jobId, `Process exited with code ${code}`);
    });

    return jobId;
}