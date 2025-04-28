package com.example.matrixcalculator

/**
 * This class provides methods for matrix operations (addition, subtraction, multiplication, and division).
 * The operations are implemented natively using C/C++ via JNI (Java Native Interface).
 */
class MatrixOperations {

    // Companion object to load the native library that contains the C/C++ implementations of matrix operations
    companion object {
        // Load the "matrix_operations" native library which contains the native methods
        init {
            System.loadLibrary("matrix_operations")
        }
    }

    /**
     * Adds two matrices.
     * The matrices are passed as flattened arrays (1D) and the rows and columns of both matrices are also passed for validation.
     *
     * @param rows1 Number of rows in matrix 1
     * @param cols1 Number of columns in matrix 1
     * @param matrix1 Flattened 1D array representing matrix 1
     * @param rows2 Number of rows in matrix 2
     * @param cols2 Number of columns in matrix 2
     * @param matrix2 Flattened 1D array representing matrix 2
     * @return A flattened 1D array representing the result matrix after addition
     */
    external fun addMatrices(
        rows1: Int, cols1: Int, matrix1: DoubleArray,
        rows2: Int, cols2: Int, matrix2: DoubleArray
    ): DoubleArray?

    /**
     * Subtracts one matrix from another.
     * The matrices are passed as flattened arrays (1D) and the rows and columns of both matrices are also passed for validation.
     *
     * @param rows1 Number of rows in matrix 1
     * @param cols1 Number of columns in matrix 1
     * @param matrix1 Flattened 1D array representing matrix 1
     * @param rows2 Number of rows in matrix 2
     * @param cols2 Number of columns in matrix 2
     * @param matrix2 Flattened 1D array representing matrix 2
     * @return A flattened 1D array representing the result matrix after subtraction
     */
    external fun subtractMatrices(
        rows1: Int, cols1: Int, matrix1: DoubleArray,
        rows2: Int, cols2: Int, matrix2: DoubleArray
    ): DoubleArray?

    /**
     * Multiplies two matrices.
     * The matrices are passed as flattened arrays (1D) and the rows and columns of both matrices are also passed for validation.
     *
     * @param rows1 Number of rows in matrix 1
     * @param cols1 Number of columns in matrix 1
     * @param matrix1 Flattened 1D array representing matrix 1
     * @param rows2 Number of rows in matrix 2
     * @param cols2 Number of columns in matrix 2
     * @param matrix2 Flattened 1D array representing matrix 2
     * @return A flattened 1D array representing the result matrix after multiplication
     */
    external fun multiplyMatrices(
        rows1: Int, cols1: Int, matrix1: DoubleArray,
        rows2: Int, cols2: Int, matrix2: DoubleArray
    ): DoubleArray?

    /**
     * Divides one matrix by another (element-wise division).
     * The matrices are passed as flattened arrays (1D) and the rows and columns of both matrices are also passed for validation.
     *
     * @param rows1 Number of rows in matrix 1
     * @param cols1 Number of columns in matrix 1
     * @param matrix1 Flattened 1D array representing matrix 1
     * @param rows2 Number of rows in matrix 2
     * @param cols2 Number of columns in matrix 2
     * @param matrix2 Flattened 1D array representing matrix 2
     * @return A flattened 1D array representing the result matrix after division
     */
    external fun divideMatrices(
        rows1: Int, cols1: Int, matrix1: DoubleArray,
        rows2: Int, cols2: Int, matrix2: DoubleArray
    ): DoubleArray?
}
