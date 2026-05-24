const jobs = {};

export function createJob(jobId, filename) {
    jobs[jobId] = {
        filename,
        status: "processing",
        result: null,
        error: null
    };
}

export function completeJob(jobId, resultPath) {
    jobs[jobId].status = "done";
    jobs[jobId].result = resultPath;
}

export function errorJob(jobId, error) {
    jobs[jobId].status = "error";
    jobs[jobId].error = error;
}

export function getJob(jobId) {
    return jobs[jobId];
}