#ifndef BENCHMARKS_H
#define BENCHMARKS_H
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#define KB 1024
#define MB (1024 * 1024)
#if defined(__APPLE__)
#include <mach/mach_time.h>
#elif defined(_WIN32)
#include <windows.h>
#else
#include <time.h>
#endif
static inline double get_time() {
#if defined(__APPLE__)
  static mach_timebase_info_data_t info = {0};
  if (info.denom == 0)
    mach_timebase_info(&info);
  uint64_t t = mach_absolute_time();
  double nanos = (double)t * info.numer / info.denom;
  return nanos / 1e9;
#elif defined(_WIN32)
  static LARGE_INTEGER freq;
  static int initialized = 0;
  if (!initialized) {
    QueryPerformanceFrequency(&freq);
    initialized = 1;
  }
  LARGE_INTEGER counter;
  QueryPerformanceCounter(&counter);
  return (double)counter.QuadPart / (double)freq.QuadPart;
#else
  struct timespec ts;
  clock_gettime(CLOCK_MONOTONIC, &ts);
  return ts.tv_sec + ts.tv_nsec / 1e9;
#endif
}
void run_latency_test(FILE *fp, int specific_size, int iterations);
void run_bandwidth_test(FILE *fp, int specific_size, int iterations);
void run_matrix_test(FILE *fp, int specific_size, int iterations);
void run_sequential_test(FILE *fp, int specific_size, int iterations);
void run_random_test(FILE *fp, int specific_size, int iterations);
void run_stride_test(FILE *fp, int specific_size, int iterations);
#endif