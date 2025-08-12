#!/bin/bash

# A plain bash script to send parallel HTTP requests.
# It uses shell job control (& and wait) to achieve concurrency.

URL="http://localhost:8080/hello"

# The number of requests to run in parallel in each batch.
CONCURRENCY_LEVEL=50

echo "Starting parallel load test..."
echo "Target URL: $URL"
echo "Concurrency Level: $CONCURRENCY_LEVEL"
echo "Press [Ctrl+C] to stop the script."
echo "-------------------------------------"

BATCH_NUM=1

# Infinite loop to continuously send batches of requests.
while true
do
    # Launch a batch of requests in parallel.
    for i in $(seq 1 $CONCURRENCY_LEVEL)
    do
        # The '&' is the key. It tells the shell to run this command
        # in the background and immediately continue to the next iteration.
        curl --connect-timeout 2 -s -o /dev/null "$URL" &
    done

    # The 'wait' command is crucial. It pauses the script here and waits
    # for all background jobs started in the current shell to complete.
    # Without this, the 'while true' loop would spin out of control,
    # launching thousands of processes (a "fork bomb").
    wait

    echo "Batch #$BATCH_NUM completed. Fired $CONCURRENCY_LEVEL requests in parallel."
    ((BATCH_NUM++))
done

echo "-------------------------------------"
echo "Script terminated."