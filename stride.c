#include "benchmarks.h"
#define REPEATS 5

static double measure_stride(int array_size_bytes, int stride_bytes)
{
  int elements = array_size_bytes / sizeof(int);
  int stride_elems = stride_bytes / (int)sizeof(int);

  if (stride_elems <= 0)
    stride_elems = 1;

  int *A = malloc(array_size_bytes);
  if (!A)
    return -1.0;

  for (int i = 0; i < elements; i++)
  {
    A[i] = i;
  }

  volatile long long sum = 0;
  double start = get_time();
  for (int r = 0; r < REPEATS; r++)
  {
    for (int i = 0; i < elements; i += stride_elems)
    {
      sum += A[i];
    }
  }
  double end = get_time();

  free(A);

  double total_accesses = 0.0;
  for (int r = 0; r < REPEATS; r++)
  {
    total_accesses += (elements + stride_elems - 1) / stride_elems;
  }
  double total_time_ns = (end - start) * 1e9;
  return total_time_ns / total_accesses;
}

void run_stride_test(FILE *fp, int specific_stride, int iterations)
{
  printf("\nSTRIDE ACCESS TEST\n");
  int array_size_bytes = 8 * MB;
  if (specific_stride == -1)
  {
    printf("Stride (bytes)\tAccess Time (ns)\n");
    printf("---------------------------------\n");
    fprintf(fp, "Stride,StrideBytes,AccessTime_ns\n");

    int strides_bytes[] = {4, 8, 16, 32, 64, 128,
                           256, 512, 1024, 2048, 4096, 8192};
    int num_strides = sizeof(strides_bytes) / sizeof(strides_bytes[0]);

    for (int i = 0; i < num_strides; i++)
    {
      int stride = strides_bytes[i];
      double t = measure_stride(array_size_bytes, stride);
      printf("%8d\t\t%.4f\n", stride, t);
      fprintf(fp, "Stride,%d,%.4f\n", stride, t);
    }
  }
  else
  {
    int stride = specific_stride;
    for (int rep = 1; rep <= iterations; rep++)
    {
      double t = measure_stride(array_size_bytes, stride);
      fprintf(fp, "%d,%.4f\n", rep, t);
      printf("Run %d: %.4f ns\n", rep, t);
    }
  }
}