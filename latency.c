#include "benchmarks.h"
#include <stdlib.h>
#include <time.h>
#define NUM_ACCESSES 1000000
#define REPEATS 5

void shuffle(int *array, int n)
{
  if (n > 1)
  {
    for (int i = 0; i < n - 1; i++)
    {
      int j = i + rand() / (RAND_MAX / (n - i) + 1);
      int t = array[j];
      array[j] = array[i];
      array[i] = t;
    }
  }
}

double measure_latency(int size_bytes)
{
  int element_size = sizeof(void *);
  int num_elements = size_bytes / element_size;

  if (num_elements < 2)
    return 0;

  void **buffer = malloc(size_bytes);
  if (!buffer)
    return -1;

  int *indices = malloc(num_elements * sizeof(int));
  if (!indices)
  {
    free(buffer);
    return -1;
  }

  for (int i = 0; i < num_elements; i++)
  {
    indices[i] = i;
  }

  shuffle(indices, num_elements);

  for (int i = 0; i < num_elements - 1; i++)
  {
    buffer[indices[i]] = (void *)&buffer[indices[i + 1]];
  }

  buffer[indices[num_elements - 1]] = (void *)&buffer[indices[0]];

  void **p = (void **)buffer[indices[0]];

  double start = get_time();

  for (int i = 0; i < NUM_ACCESSES; i += 10)
  {
    p = (void **)*p;
    p = (void **)*p;
    p = (void **)*p;
    p = (void **)*p;
    p = (void **)*p;
    p = (void **)*p;
    p = (void **)*p;
    p = (void **)*p;
    p = (void **)*p;
    p = (void **)*p;
  }

  double end = get_time();

  free(indices);
  free(buffer);

  double elapsed = end - start;
  return (elapsed / NUM_ACCESSES) * 1e9;
}
void run_latency_test(FILE *fp, int specific_size, int iterations)
{
  printf("\nLATENCY TEST (Pointer Chasing)\n");
  srand(time(NULL));
  if (specific_size == -1)
  {
    printf("Block Size (KB)\tAverage Access Time (ns)\n");
    printf("-------------------------------------------\n");
    fprintf(fp, "TestType,BlockSize_KB,AccessTime_ns\n");
    for (int size_kb = 1; size_kb <= 8192; size_kb *= 2)
    {
      double total = 0.0;
      for (int rep = 0; rep < REPEATS; rep++)
      {
        total += measure_latency(size_kb * KB);
      }
      double avg_latency = total / REPEATS;
      printf("%8d\t\t%.2f\n", size_kb, avg_latency);
      fprintf(fp, "Latency,%d,%.2f\n", size_kb, avg_latency);
    }
  }
  else
  {
    int size_kb = specific_size;
    for (int rep = 1; rep <= iterations; rep++)
    {
      double result = measure_latency(size_kb * KB);
      if (result > 0)
      {
        fprintf(fp, "%d,%.4f\n", rep, result);
        printf("Run %d: %.4f ns\n", rep, result);
      }
    }
  }
}