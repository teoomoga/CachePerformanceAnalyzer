#include "benchmarks.h"
#include <string.h>

int main(int argc, char *argv[])
{
  if (argc == 1)
  {
    FILE *fp = fopen("results.csv", "w");
    if (!fp)
    {
      printf("Error: could not open results.csv\n");
      return 1;
    }

    printf("MEMORY PERFORMANCE TEST\n");

    run_latency_test(fp, -1, 5);
    run_bandwidth_test(fp, -1, 5);
    run_matrix_test(fp, -1, 5);
    run_sequential_test(fp, -1, 5);
    run_random_test(fp, -1, 5);
    run_stride_test(fp, -1, 5);

    fclose(fp);
    printf("\nResults saved to results.csv \n");
    return 0;
  }

  if (argc >= 4)
  {
    char *test_type = argv[1];
    int size = atoi(argv[2]);
    char *output_file = argv[3];
    int iterations = 5;
    if (argc >= 5)
    {
      iterations = atoi(argv[4]);
    }
    FILE *fp =
        fopen(output_file,
              "w");
    if (!fp)
    {
      printf("Error: could not open %s\n", output_file);
      return 1;
    }
    if (strcmp(test_type, "latency") == 0)
      run_latency_test(fp, size, iterations);
    else if (strcmp(test_type, "bandwidth") == 0)
      run_bandwidth_test(fp, size, iterations);
    else if (strcmp(test_type, "matrix") == 0)
      run_matrix_test(fp, size, iterations);
    else if (strcmp(test_type, "sequential") == 0)
      run_sequential_test(fp, size, iterations);
    else if (strcmp(test_type, "random") == 0)
      run_random_test(fp, size, iterations);
    else if (strcmp(test_type, "stride") == 0)
      run_stride_test(fp, size, iterations);
    else
      printf("Unknown test type: %s\n", test_type);
    fclose(fp);
    return 0;
  }
  return 1;
}