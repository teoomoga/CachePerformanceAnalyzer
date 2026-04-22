
#!/bin/bash

gcc -o memtest main.c latency.c bandwidth.c matrix.c sequential.c random.c stride.c
if [ $? -ne 0 ]; then
    echo "C Compilation failed."
    exit 1
fi

mkdir -p java_src
javac java_src/*.java
if [ $? -eq 0 ]; then
    java -cp java_src CacheMemoryVisualizer
else
    echo "Compilation failed."
fi
