# Centroid Finder API

This document reflects the current Express API implemented in the server project.

## List Available Videos

**GET** `/api/videos`

**Description:**
Returns the filenames currently available in the configured video directory. The server filters the directory listing to common video extensions: `.mp4`, `.mov`, and `.avi`.

**Responses:**

- **200 OK**

  ```json
  ["intro.mp4", "demo.mov"]
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Error reading video directory"
  }
  ```

---

## Generate Thumbnail

**GET** `/thumbnail/{filename}`

**Path Parameters:**

- `filename` (string, required) — Video filename to thumbnail (for example, `demo.mov`).

**Description:**
Generates a JPEG thumbnail from the first video frame and returns it as binary image data.

**Responses:**

- **200 OK**
  JPEG binary data
  _Content-Type: image/jpeg_

- **500 Internal Server Error**

  ```json
  {
    "error": "Error generating thumbnail"
  }
  ```

---

## Resolve the Latest CSV Result

**GET** `/latest/{filename}`

**Path Parameters:**

- `filename` (string, required) — A video or result filename fragment to match against the generated CSVs in the results directory.

**Description:**
Returns the most recently modified matching CSV result as a plain-text download path, which the frontend can use directly.

**Responses:**

- **200 OK**

  ```text
  /download/123e4567-e89b-12d3-a456-426614174000
  ```

- **404 Not Found**

  ```json
  {
    "error": "No CSV result found"
  }
  ```

---

## Retrieve Job Metadata

**GET** `/metadata/{jobId}`

**Path Parameters:**

- `jobId` (string, required) — The job ID whose saved `.meta.json` file should be returned.

**Description:**
Returns the persisted metadata for a completed or in-progress job as JSON. This includes the original input filename, generated CSV path, timestamp, and the processing parameters used.

**Responses:**

- **200 OK**

  ```json
  {
    "filename": "sample.mp4",
    "outputCsv": "./results/123e4567-e89b-12d3-a456-426614174000.csv",
    "createdAt": "2026-06-11T18:54:29.000Z",
    "targetColor": "ff0000",
    "threshold": "120"
  }
  ```

- **404 Not Found**

  ```json
  {
    "error": "Metadata not found"
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Error reading metadata"
  }
  ```

---

## Start a Processing Job

**POST** `/process/{filename}`

**Path Parameters:**

- `filename` (string, required) — Video filename to process (for example, `intro.mp4`).

**Query Parameters:**

- `targetColor` (string, required) — Hex color to match (for example, `ff0000`).
- `threshold` (number, required) — Matching threshold passed to the processor (for example, `120`).

**Description:**
Starts an asynchronous centroid-finding job. The returned `jobId` can be polled with the status endpoint.

**Responses:**

- **202 Accepted**

  ```json
  {
    "jobId": "123e4567-e89b-12d3-a456-426614174000"
  }
  ```

- **400 Bad Request**

  ```json
  {
    "error": "Missing targetColor or threshold query parameter."
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Error starting job"
  }
  ```

---

## Get Processing Job Status

**GET** `/process/{jobId}/status`

**Path Parameters:**

- `jobId` (string, required) — ID returned by the start-job request.

**Description:**
Returns the current job status, progress, and result location when the job finishes.

**Responses:**

- **200 OK** (processing)

  ```json
  {
    "status": "processing",
    "progress": 42
  }
  ```

- **200 OK** (done)

  ```json
  {
    "status": "done",
    "progress": 100,
    "result": "/download/123e4567-e89b-12d3-a456-426614174000"
  }
  ```

- **200 OK** (error)

  ```json
  {
    "status": "error",
    "progress": 0,
    "error": "Process exited with code 1"
  }
  ```

- **404 Not Found**

  ```json
  {
    "error": "Job ID not found"
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Error fetching job status"
  }
  ```

---

## Download the Result CSV

**GET** `/download/{jobId}`

**Path Parameters:**

- `jobId` (string, required) — ID returned by the start-job request.

**Description:**
Downloads the generated CSV file for a completed job. The file is returned as an attachment named `{jobId}.csv`.

**Responses:**

- **200 OK**
  CSV file download

- **400 Bad Request**

  ```json
  {
    "error": "Result is not available yet"
  }
  ```

- **404 Not Found**

  ```json
  {
    "error": "Job ID not found"
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Error downloading result"
  }
  ```