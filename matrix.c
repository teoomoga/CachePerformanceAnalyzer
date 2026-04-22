#include "benchmarks.h"
#include <string.h>

static void multiply_ijk(double **A, double **B, double **C, int N) {
  for (int i = 0; i < N; i++)
    for (int j = 0; j < N; j++)
      for (int k = 0; k < N; k++)
        C[i][j] += A[i][k] * B[k][j];
}

static void multiply_ikj(double **A, double **B, double **C, int N) {
  for (int i = 0; i < N; i++)
    for (int k = 0; k < N; k++)
      for (int j = 0; j < N; j++)
        C[i][j] += A[i][k] * B[k][j];
}

static void multiply_jik(double **A, double **B, double **C, int N) {
  for (int j = 0; j < N; j++)
    for (int i = 0; i < N; i++)
      for (int k = 0; k < N; k++)
        C[i][j] += A[i][k] * B[k][j];
}

static void multiply_jki(double **A, double **B, double **C, int N) {
  for (int j = 0; j < N; j++)
    for (int k = 0; k < N; k++)
      for (int i = 0; i < N; i++)
        C[i][j] += A[i][k] * B[k][j];
}

static void multiply_kij(double **A, double **B, double **C, int N) {
  for (int k = 0; k < N; k++)
    for (int i = 0; i < N; i++)
      for (int j = 0; j < N; j++)
        C[i][j] += A[i][k] * B[k][j];
}

static void multiply_kji(double **A, double **B, double **C, int N) {
  for (int k = 0; k < N; k++)
    for (int j = 0; j < N; j++)
      for (int i = 0; i < N; i++)
        C[i][j] += A[i][k] * B[k][j];
}

static double **alloc_matrix(int N) {
  double **M = malloc(N * sizeof(double *));
  for (int i = 0; i < N; i++) {
    M[i] = calloc(N, sizeof(double));
  }
  return M;
}

static void free_matrix(double **M, int N) {
  for (int i = 0; i < N; i++)
    free(M[i]);
  free(M);
}

void run_matrix_test(FILE *fp, int specific_size, int iterations) {
  printf("\nMATRIX TEST\n");
  double **A, **B, **C;
  struct {
    char *name;
    void (*func)(double **, double **, double **, int);
  } tests[] = {{"ijk", multiply_ijk}, {"ikj", multiply_ikj},
               {"jik", multiply_jik}, {"jki", multiply_jki},
               {"kij", multiply_kij}, {"kji", multiply_kji}};
  int nt = sizeof(tests) / sizeof(tests[0]);
  srand(time(NULL));
  if (specific_size == -1) {
    fprintf(fp, "MatrixTest,Order,Size,Time_ns\n");
    int sizes[] = {64, 128, 256, 512, 1024};
    int num_sizes = sizeof(sizes) / sizeof(sizes[0]);
    for (int s = 0; s < num_sizes; s++) {
      int N = sizes[s];
      printf("\n--- Matrix size: %d x %d ---\n", N, N);
      A = alloc_matrix(N);
      B = alloc_matrix(N);
      C = alloc_matrix(N);
      for (int i = 0; i < N; i++)
        for (int j = 0; j < N; j++) {
          A[i][j] = 1.0;
          B[i][j] = 1.0;
        }
      for (int t = 0; t < nt; t++) {
        double total_time = 0;
        int benchmark_iterations = 5;  
        for (int k = 0; k < benchmark_iterations; k++) {
          for (int i = 0; i < N; i++)
            memset(C[i], 0, N * sizeof(double));
          double start = get_time();
          tests[t].func(A, B, C, N);
          double end = get_time();
          total_time += (end - start);
        }
        double elapsed_ns = (total_time / benchmark_iterations) * 1e9;
        printf("%s order: %.0f ns\n", tests[t].name, elapsed_ns);
        fprintf(fp, "Matrix,%s,%d,%.0f\n", tests[t].name, N, elapsed_ns);
      }
      free_matrix(A, N);
      free_matrix(B, N);
      free_matrix(C, N);
    }
  } else {
    int N = specific_size;
    A = alloc_matrix(N);
    B = alloc_matrix(N);
    C = alloc_matrix(N);
    for (int i = 0; i < N; i++)
      for (int j = 0; j < N; j++) {
        A[i][j] = 1.0;
        B[i][j] = 1.0;
      }
    for (int t = 0; t < nt; t++) {
      for (int k = 1; k <= iterations; k++) {  
        for (int i = 0; i < N; i++)
          memset(C[i], 0, N * sizeof(double));
        double start = get_time();
        tests[t].func(A, B, C, N);
        double end = get_time();
        double result = (end - start) * 1e9;
        fprintf(fp, "%d_%s,%.0f\n", k, tests[t].name, result);
        printf("Run %d (%s): %.0f ns\n", k, tests[t].name, result);
      }
    }
    free_matrix(A, N);
    free_matrix(B, N);
    free_matrix(C, N);
  }
}