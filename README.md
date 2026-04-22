# 🖥️ Cache Performance Analyzer

## Project Overview
This benchmarking tool is designed to explore memory hierarchy and processor performance through rigorous latency and bandwidth testing. The project utilizes a high-performance execution engine written in **C** to interact directly with the hardware, paired with a modern graphical interface built in **Java (Swing)** for the visual interpretation of results.

The primary objective is to identify the physical limits of the cache hierarchy (**L1, L2, and L3**), as well as the impact of various access patterns on execution speed.

## 🛠️ Tools Used
### **Core Languages**
* **C:** Implements low-level benchmarking kernels to measure hardware performance directly.
* **Java:** Powers the analytics engine and the graphical interface for data visualization.

### **Build & Execution**
* **GCC:** Compiles the C source code into the `memtest` benchmarking executable.
* **Bash Scripting:** Automates the entire compilation and launch process via `run_gui.sh`.

### **Data & UI**
* **CSV:** Transfers raw performance data from the C execution engine to the Java visualizer.
* **Java Swing & AWT:** Renders the dashboard and custom graphs natively without any external libraries.

## ✨ Key Features

* **Live Benchmark Execution:** Select, configure, and run hardware tests (Latency, Bandwidth, Matrix) directly from the UI.
* **Dynamic Visualization:** Automatically plots and scales results on a custom-built graph as soon as tests complete.
* **Integrated Console:** Captures and displays live output from the C benchmarking engine directly within the dashboard.
* **Cache Hierarchy Profiling:** Provides the data necessary to see the performance shifts between L1, L2, L3 caches, and Main Memory.
* **System Auto-Detection:** Automatically fetches and displays your machine's CPU model, RAM, and cache capacities.

## 🧪 Included Benchmarks

* `latency.c`: Implements pointer-chasing to measure exact access times in nanoseconds. It intentionally triggers **cache misses** across L1, L2, and L3 boundaries by bypassing the hardware prefetcher.
* `bandwidth.c`: Measures data throughput in GB/s for sequential memory operations.
* `matrix.c`: Compares loop orders (`ijk`, `ikj`, etc.) to demonstrate the massive impact of **spatial locality**. It proves how contiguous row-major access maximizes **cache hits**, whereas jumping across columns results in continuous **cache misses** and severe performance degradation.
* `sequential.c`: Tests highly predictable memory traversals, ensuring nearly 100% **cache hits** by fully utilizing spatial locality and hardware prefetchers.
* `random.c`: Tests unpredictable memory access, actively destroying both temporal and spatial locality to force the hardware into expensive **cache misses**.
* `stride.c`: Tests memory accesses with specific jump intervals (strides) to evaluate how well the cache lines and prefetchers handle gaps in data locality.

## 🚀 Running the Project

To run the project in your local environment, follow these steps:

1. Clone the repository to your local machine.
2. Open your terminal and **navigate to the project directory**.
3. Ensure you have the required prerequisites: A standard UNIX environment with `gcc` (for C compilation) and `java` (for the GUI) installed.
4. Make the script executable by running `chmod +x run_gui.sh`.
5. Run `./run_gui.sh` to automatically compile the benchmarking kernels and start the visualizer.



