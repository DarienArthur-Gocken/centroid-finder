keep track of things directly on filesystem.
store stuff like paths of video directory and videoprocessor jar in .env
install dotenv
use child_process to make a new side process to run the java jar. use detached mode, ignore std.io and unreference child process.
add method to jar to grab thumbnail via the java jar

jobid - keep associative array of jobid to video name linked somewhere.

need routes:
    GET
        - api/video
        - thumbnail/{filename}
        - /process/{jobID}/status
    POST
        /process/{filename}


client sends request to process video (post /process)
server creates UUID job ID
server stores initial job stuff, links job ID to video, status of video job.
server launches jar using child_process.spawn
jar analyzes video and writes CSV
server status changes to completed
cleint does get /process/{jobid}/status for updates


                Client Request
                    |
                    v
                +----------------+
                | Express Server |
        --------+----------------+
        |       |         |
        |       |         +-----> Reads video directory (get /api/video)
        |       |
        |       +-(/process/filename)-> Starts Java JAR
        |                                           |
        |                                           v
        |                               +----------------+
        v                               | Video Processor|
       -(process/jobid/status)          +----------------+
           |                                          |
            status                                    v
                                            CSV processed 
                                                    |
                                                    v
                                            job is finished

GET /API/Video
    - should give 200 ok or 500 if error reading

get /thumbnail/{filename}
    -path parameter of filename, string, required.
    - 200 ok or 500 error if error generating thumbnail


POST /process/{filename}
Query parameters: ?targetColor=<hex>&threshold=<int>
    - path parameter of filename, string, required
    -target color & threshold required
    - 202 accepted, returns jobid
    - 400 bad request gives vague or specific reason
    - 500 error starting job

GET /process/{jobId}/status
    -jobid required
    - 200 ok, processing (not done), returns status processing
    - 200 ok (done), returns status done & link to the csv result.
    - 200 ok, error, returns status error, unexpected ffpmeg error
    -404, job id not found
    - 500 error fetching job status