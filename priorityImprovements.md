# Priority Improvements

## Refactoring Code

### 1. Break up the server routes/controller into separate video & thumbnail modules

### 2. Remove or replace the DFS centroid/group finding implementation

---

## Adding Tests

### 1. Add better video-processing integration tests

### 2. Add unit tests for `ImageBinarizer`, `BinaryGroupFinder`, and `ColorDistanceFinder`. Synthetic images.

---

## Improving Error Handling

### 1. Add startup validation for environment variables and jar existence

### 2. Improve centralized error handling and logging (express middleware??)

---

## Writing Documentation 

### 1. Add JavaDocs to the video processor

### 2. Expand the README with setup and troubleshooting instructions

---

## Improving Performance

### 1. Cache generated thumbnails

### 2. optimize frame-processing performance

---

## Hardening Security

### 1. Validate and sanitize all user input (filename, threshold, color)

### 2. Limit the number of concurrent processing jobs

---

## Bug Fixes 

### 1. Output (-1, -1) when no centroid is found

### 2. Review FFmpeg integration & update junit dependencies