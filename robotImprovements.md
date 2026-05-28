# Robot Improvements

This file records prioritized improvement tasks with clarifications and concrete next actions.

- [ ] Add linting and formatter
	- What: Add `ESLint` + `Prettier` for `server/` and `Spotless` or `google-java-format` for Java in `processor/`.
	- Why: Consistent style, fewer formatting churn PRs, enforceable in CI.
	- Next step: Add configs and a CI lint check.

- [ ] Improve error handling and logging
	- What: Replace ad-hoc `console` logs with a structured logger (e.g., `pino` or `winston`) and add centralized Express error middleware.
	- Why: Easier filtering/aggregation, include `jobId` and context, better production observability.
	- Next step: Add logger, wire `jobId` into request/context, and convert key log calls.

- [ ] Refactor and modularize processor and server
	- What: Split responsibilities into layers: CLI/entrypoint, I/O (file, ffmpeg), processing (frame/centroid logic), and utilities (CSV, color parsing).
	- Why: Improves testability, reduces coupling to native libs, simplifies maintenance.
	- Next step: Identify top-level classes/functions to extract (e.g., `VideoProcessor` helpers).

- [ ] Add and expand unit/integration tests
	- What: Unit tests for `ImageBinarizer`, `BinaryGroupFinder`, `ColorDistanceFinder`; integration test that packages the jar and runs it on a tiny sample video to assert CSV output.
	- Why: Prevent regressions, verify end-to-end behavior.
	- Next step: Add sample fixtures and a GitHub Actions job to run the integration test on PRs.

- [ ] Performance and resource management
	- What: Profile hot paths (JFR/async-profiler), reuse frame/pixel buffers, avoid per-frame allocations, consider controlled parallelism with ExecutorService if safe with JavaCV.
	- Why: Reduce GC pauses and improve throughput for large videos.
	- Next step: Add a simple micro-benchmark and profile a representative input.

- [ ] Security and input validation hardening
	- What: Validate and sanitize `filename`, `targetColor`, and `threshold`. Whitelist extensions (mp4,mov,avi), reject `..` or absolute paths, resolve canonical path and ensure it's inside `VIDEO_DIR`.
	- Why: Prevent path traversal, injection, and unexpected file access.
	- Next step: Implement canonical path checks and extension whitelist in controller.

- [ ] Documentation and developer README
	- What: Document build steps (`mvn package` -> jar-with-dependencies), required env vars (`VIDEO_DIR`, `RESULT_DIR`, `PROCESSOR_JAR`), example `curl` commands, and CSV schema.
	- Why: Lowers onboarding friction and reduces support questions.
	- Next step: Add a short `README` section and troubleshooting tips.

- [ ] Validate environment variables and startup checks
	- What: At server startup, verify `VIDEO_DIR`, `RESULT_DIR`, and `PROCESSOR_JAR` exist and are accessible; fail fast with clear logs.
	- Next step: Add a startup check that exits with descriptive error codes if misconfigured.

- [ ] Add child-process timeouts and exit handling in server/service
	- What: Set a configurable timeout for the spawned Java process; kill and cleanup on timeout; parse exit codes and structured JSON output from Java.
	- Why: Avoid orphaned processes and provide clearer error messages to clients.
	- Next step: Add timeout, stream logging, and robust `close` handling in `server/services/job.service.js`.

## Further details

### Refactoring (detailed)
- Break `VideoProcessor` into: `FrameReader` (captures frames), `FrameProcessor` (binarize, find groups), and `ResultWriter` (CSV, thumbnails).
- Create adapters for external systems: `FFmpegAdapter` and `JavaCVAdapter` to isolate native usage. -- clarify why you would need to use adapters
- Add `ProcessingConfig` immutable object for thresholds, colors, and performance tuning.

### Tests (detailed)
- Add unit tests with small synthetic frames under `processor/src/test/java/.../fixtures`. -- what is synthetic frames and why is it a fixture?
 - Add unit tests with small synthetic frames under `processor/src/test/java/.../fixtures`.
	 - Synthetic frames are programmatically generated small image buffers (primitive arrays) that represent controlled patterns (solid color, a single blob, edge-cases). They are stored as fixtures so tests are deterministic, fast, and do not depend on large binary video files.

### Error handling (detailed)
- Replace `catch (Exception e)` with targeted catches and rethrow domain exceptions with context.
- Node: validate inputs early and return `400` with structured JSON errors. Log full stack traces server-side only.

### Performance (detailed)
- Consider using `ByteBuffer` or primitive arrays and reuse them across frames. -- what is bytebuffer and why would you reuse it?
 - Consider using `ByteBuffer` or primitive arrays and reuse them across frames.
	 - `ByteBuffer` (Java NIO) is a primitive-backed buffer for efficient byte access and I/O. Reusing the same `ByteBuffer` or primitive arrays avoids per-frame allocations, reduces GC pressure, and improves throughput when processing many frames.
### Security (detailed)
- Implement path canonicalization and whitelist checks in `server/controllers/job.controller.js` and `server/services/job.service.js`.