#include "benchmarks.h"
#include <string.h>
#define REPEATS 5
#define INNER_REPEATS 10000

double measure_bandwidth(int block_size)
{
  char *src = malloc(block_size);
  char *dst = malloc(block_size);

  if (!src || !dst)
    return -1;

  for (int i = 0; i < block_size; i++)
  {
    src[i] = (char)(i % 256);
  }

  double start = get_time();
  for (int r = 0; r < REPEATS; r++)
  {
    for (int i = 0; i < INNER_REPEATS; i++)
    {
      memcpy(dst, src, block_size);
    }
  }

  double end = get_time();

  free(src);
  free(dst);

  double total_bytes = (double)block_size * INNER_REPEATS * REPEATS;
  double total_MB = total_bytes / (1024.0 * 1024.0);
  double total_time = end - start;
  double bandwidth = total_MB / total_time;

  return bandwidth;
}

void run_bandwidth_test(FILE *fp, int specific_size, int iterations)
{
  printf("\nBANDWIDTH TEST\n");

  if (specific_size == -1)
  {
    printf("Block Size (KB)\tBandwidth (MB/s)\n");
    printf("----------------------------------\n");

    for (int size_kb = 1; size_kb <= 8192; size_kb *= 2)
    {
      double bw = measure_bandwidth(size_kb * KB);

      if (bw > 0)
        printf("%8d\t\t%.2f\n", size_kb, bw);
      else
        printf("%8d\t\tError\n", size_kb);
      fprintf(fp, "Bandwidth,%d,%.2f\n", size_kb, bw);
    }
  }
  else
  {
    int size_kb = specific_size;

    for (int rep = 1; rep <= iterations; rep++)
    {
      double bw = measure_bandwidth(size_kb * KB);
      fprintf(fp, "%d,%.4f\n", rep, bw);
      printf("Run %d: %.4f MB/s\n", rep, bw);
    }
  }
}