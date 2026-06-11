import fs from "fs/promises";
import fsSync from "fs";
import path from "path";
import { spawn } from "child_process";
import { v4 } from "uuid";

const DEFAULT_MAX_CONCURRENT_JOBS = 2;

export function createJobService({
    spawnImpl = spawn,
    maxConcurrentJobs = Number(process.env.MAX_CONCURRENT_JOBS || DEFAULT_MAX_CONCURRENT_JOBS),
    jobsStore = {},
    queueStore = []
} = {}) {
    let activeJobs = 0;

    function createJob(jobId, filename) {
        jobsStore[jobId] = {
            filename,
            status: "queued",
            progress: 0,
            result: null,
            error: null
        };
    }

    function updateJobProgress(jobId, progress) {
        if (jobsStore[jobId]) {
            jobsStore[jobId].progress = progress;
        }
    }

    function completeJob(jobId, resultPath) {
        if (!jobsStore[jobId]) {
            return;
        }

        jobsStore[jobId].status = "done";
        jobsStore[jobId].progress = 100;
        jobsStore[jobId].result = resultPath;
    }

    function errorJob(jobId, error) {
        if (!jobsStore[jobId]) {
            return;
        }

        jobsStore[jobId].status = "error";
        jobsStore[jobId].error = error;
    }

    function restoreJobFromDisk(jobId) {
        const metadataPath = path.join(process.env.RESULT_DIR || '.', `${jobId}.meta.json`);
        const resultPath = path.join(process.env.RESULT_DIR || '.', `${jobId}.csv`);

        try {
            if (!fsSync.existsSync(metadataPath)) {
                return null;
            }

            const raw = fsSync.readFileSync(metadataPath, 'utf8');
            const metadata = JSON.parse(raw);
            const resultExists = fsSync.existsSync(resultPath);

            jobsStore[jobId] = {
                filename: metadata.filename || jobId,
                status: resultExists ? 'done' : 'error',
                progress: resultExists ? 100 : 0,
                result: resultExists ? resultPath : null,
                error: resultExists ? null : 'Result file not found',
                input: metadata.input || null,
                output: metadata.outputCsv || resultPath
            };

            return jobsStore[jobId];
        } catch (err) {
            return null;
        }
    }

    function getJob(jobId) {
        if (jobsStore[jobId]) {
            return jobsStore[jobId];
        }

        return restoreJobFromDisk(jobId);
    }

    async function startProcess({ jobId, filename, targetColor, threshold }) {
        const inputVideo = path.join(process.env.VIDEO_DIR, filename);
        const outputCsv = path.join(process.env.RESULT_DIR, `${jobId}.csv`);
        const metadataPath = path.join(process.env.RESULT_DIR, `${jobId}.meta.json`);

        jobsStore[jobId].status = "processing";
        jobsStore[jobId].input = inputVideo;
        jobsStore[jobId].output = outputCsv;

        await fs.writeFile(metadataPath, JSON.stringify({
            filename,
            outputCsv,
            createdAt: new Date().toISOString(),
            targetColor,
            threshold
        }, null, 2), "utf8");

        const javaProcess = spawnImpl("java", [
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
            activeJobs -= 1;
            drainQueue();
        });

        javaProcess.on("close", (code) => {
            if (code === 0) {
                completeJob(jobId, outputCsv);
            } else {
                errorJob(jobId, `Process exited with code ${code}`);
            }

            activeJobs -= 1;
            drainQueue();
        });
    }

    function drainQueue() {
        while (queueStore.length > 0 && activeJobs < maxConcurrentJobs) {
            const nextJob = queueStore.shift();
            activeJobs += 1;
            void startProcess(nextJob);
        }
    }

    async function startJob({ filename, targetColor, threshold }) {
        const jobId = v4();

        createJob(jobId, filename);
        queueStore.push({ jobId, filename, targetColor, threshold });

        drainQueue();

        return jobId;
    }

    return {
        createJob,
        updateJobProgress,
        completeJob,
        errorJob,
        getJob,
        startJob,
        drainQueue,
        getActiveJobs: () => activeJobs,
        getQueueLength: () => queueStore.length
    };
}

const jobService = createJobService();

export const createJob = jobService.createJob;
export const updateJobProgress = jobService.updateJobProgress;
export const completeJob = jobService.completeJob;
export const errorJob = jobService.errorJob;
export const getJob = jobService.getJob;
export const startJob = jobService.startJob;