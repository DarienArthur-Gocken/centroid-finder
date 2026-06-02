# centroid-finder

## Maven / Video Edition.

Made by Darien Arthur-Gocken & Lilian Nyugen.

## dotenv format in server.
```
PORT=3000

VIDEO_DIR=./videos
RESULT_DIR=./results

PROCESSOR_JAR=../processor/target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## How to Run

There needs to be a videos & results folder in processor.

To compile the processor you have to have the current directory in processor and do `mvn clean package`. This will take a couple of minutes.

When you have that, you can switch your directory to server, ensure you have everything installed via npm i

Then you can run npm run dev. After that, you can run the frontend freely.