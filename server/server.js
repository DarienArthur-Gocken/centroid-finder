import fs from 'fs';
import path from 'path';
import dotenv from 'dotenv';
import app from './app.js';

dotenv.config();

const requiredEnv = ['PORT', 'VIDEO_DIR', 'RESULT_DIR', 'PROCESSOR_JAR'];
const missingEnv = requiredEnv.filter((name) => !process.env[name] || process.env[name].trim() === '');

if (missingEnv.length > 0) {
    console.error(`Missing required environment variables: ${missingEnv.join(', ')}`);
    process.exit(1);
}

const PORT = Number(process.env.PORT);
if (!Number.isInteger(PORT) || PORT <= 0 || PORT > 65535) {
    console.error(`Invalid PORT value: ${process.env.PORT}. PORT must be an integer between 1 and 65535.`);
    process.exit(1);
}

const processorJarPath = path.resolve(process.cwd(), process.env.PROCESSOR_JAR);
if (!fs.existsSync(processorJarPath)) {
    console.error(`Processor JAR not found at path: ${processorJarPath}`);
    process.exit(1);
}

const videoDirPath = path.resolve(process.cwd(), process.env.VIDEO_DIR);
const resultDirPath = path.resolve(process.cwd(), process.env.RESULT_DIR);

for (const entry of [
    { name: 'VIDEO_DIR', path: videoDirPath },
    { name: 'RESULT_DIR', path: resultDirPath }
]) {
    if (!fs.existsSync(entry.path)) {
        fs.mkdirSync(entry.path, { recursive: true });
        console.log(`Created ${entry.name} at ${entry.path}`);
    }
}

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});