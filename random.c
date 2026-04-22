#include "benchmarks.h"
#include <stdlib.h>
#define REPEATS 5

static int *generate_random_indices(int elements)
{
  int *idx = malloc(elements * sizeof(int));
  for (int i = 0; i < elements; i++)
  {
    idx[i] = rand() % elements;
  }
  return idx;
}

double measure_random(int size_bytes)
{
  int elements = size_bytes / sizeof(int);
  int *A = malloc(size_bytes);

  if (!A)
    return -1;
  for (int i = 0; i < elements; i++)
    A[i] = i;

  int *indices = generate_random_indices(elements);

  volatile long long sum = 0;
  double start = get_time();
  for (int r = 0; r < REPEATS; r++)
  {
    for (int i = 0; i < elements; i++)
    {
      sum += A[indices[i]];
    }
  }

  double end = get_time();

  free(A);
  free(indices);

  double total_accesses = (double)elements * REPEATS;
  double total_time_ns = (end - start) * 1e9;
  return total_time_ns / total_accesses;
}

void run_random_test(FILE *fp, int specific_size, int iterations)
{
  printf("\nRANDOM ACCESS TEST\n");
  if (specific_size == -1)
  {
    printf("Block Size (KB)\tAccess Time (ns)\n");
    printf("---------------------------------\n");
    fprintf(fp, "Random,BlockSize_KB,AccessTime_ns\n");
    for (int size_kb = 1; size_kb <= 8192; size_kb *= 2)
    {
      double result = measure_random(size_kb * KB);
      printf("%8d\t\t%.4f\n", size_kb, result);
      fprintf(fp, "Random,%d,%.4f\n", size_kb, result);
    }
  }
  else
  {
    int size_kb = specific_size;
    for (int rep = 1; rep <= iterations; rep++)
    {
      double result = measure_random(size_kb * KB);
      fprintf(fp, "%d,%.4f\n", rep, result);
      printf("Run %d: %.4f ns\n", rep, result);
    }
  }
}