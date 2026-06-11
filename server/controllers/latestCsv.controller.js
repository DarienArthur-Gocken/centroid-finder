import fs from 'fs/promises';
import path from 'path';

function normalizeFilename(value) {
    return String(value || '')
        .trim()
        .toLowerCase()
        .replace(/\\/g, '/');
}

export async function findLatestCsvPath(resultDir, filename) {
    const normalized = normalizeFilename(filename);

    if (!normalized) {
        return null;
    }

    const entries = await fs.readdir(resultDir, { withFileTypes: true });
    const metadataFiles = entries
        .filter((entry) => entry.isFile() && entry.name.toLowerCase().endsWith('.meta.json'));

    const candidates = [];

    for (const entry of metadataFiles) {
        try {
            const metadataPath = path.join(resultDir, entry.name);
            const raw = await fs.readFile(metadataPath, 'utf8');
            const metadata = JSON.parse(raw);
            const trackedFilename = normalizeFilename(metadata.filename);

            if (!trackedFilename) {
                continue;
            }

            const matches = trackedFilename === normalized ||
                trackedFilename.includes(normalized) ||
                normalized.includes(trackedFilename);

            if (matches) {
                const outputCsv = metadata.outputCsv || path.join(resultDir, entry.name.replace(/\.meta\.json$/, '.csv'));
                const stat = await fs.stat(outputCsv).catch(() => null);

                candidates.push({
                    outputCsv,
                    createdAt: metadata.createdAt ? new Date(metadata.createdAt).getTime() : (stat ? stat.mtimeMs : 0),
                    mtimeMs: stat ? stat.mtimeMs : 0
                });
            }
        } catch (err) {
            continue;
        }
    }

    if (candidates.length > 0) {
        candidates.sort((left, right) => {
            if (right.createdAt !== left.createdAt) {
                return right.createdAt - left.createdAt;
            }

            return right.mtimeMs - left.mtimeMs;
        });

        return candidates[0].outputCsv;
    }

    const csvFiles = entries
        .filter((entry) => entry.isFile() && entry.name.toLowerCase().endsWith('.csv'))
        .filter((entry) => entry.name.toLowerCase().includes(normalized.replace(/\.mp4|\.mov|\.avi/g, '')));

    if (csvFiles.length === 0) {
        return null;
    }

    const withStats = await Promise.all(
        csvFiles.map(async (entry) => ({
            name: entry.name,
            stat: await fs.stat(path.join(resultDir, entry.name))
        }))
    );

    withStats.sort((left, right) => right.stat.mtimeMs - left.stat.mtimeMs);

    return path.join(resultDir, withStats[0].name);
}

export async function getLatestCsv(req, res) {
    try {
        const { filename } = req.params;
        const latestPath = await findLatestCsvPath(process.env.RESULT_DIR, filename);

        if (!latestPath) {
            return res.status(404).json({ error: 'No CSV result found' });
        }

        const jobId = path.basename(latestPath, '.csv');

        return res.type('text/plain').send(`/download/${jobId}`);
    } catch (err) {
        console.error('Latest CSV lookup error:', err.message);
        return res.status(500).json({ error: 'Error locating latest CSV result' });
    }
}

export async function getCsvByName(req, res) {
    try {
        const { filename } = req.params;
        const requestedPath = path.join(process.env.RESULT_DIR, filename);

        const exists = await fs.access(requestedPath).then(() => true).catch(() => false);

        if (!exists) {
            return res.status(404).json({ error: 'CSV result not found' });
        }

        const jobId = path.basename(requestedPath, '.csv');

        return res.type('text/plain').send(`/download/${jobId}`);
    } catch (err) {
        console.error('CSV result lookup error:', err.message);
        return res.status(500).json({ error: 'Error locating CSV result' });
    }
}

export async function getMetadataByJobId(req, res) {
    try {
        const { jobId } = req.params;
        const metadataPath = path.join(process.env.RESULT_DIR, `${jobId}.meta.json`);

        const exists = await fs.access(metadataPath).then(() => true).catch(() => false);
        if (!exists) {
            return res.status(404).json({ error: 'Metadata not found' });
        }

        const metadata = await fs.readFile(metadataPath, 'utf8');
        return res.status(200).json(JSON.parse(metadata));
    } catch (err) {
        console.error('Metadata lookup error:', err.message);
        return res.status(500).json({ error: 'Error reading metadata' });
    }
}
