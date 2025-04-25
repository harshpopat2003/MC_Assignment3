package com.example.matrixcalculator

class MatrixOperations {
    companion object {
        // Load the native library
        init {
            System.loadLibrary("matrix_operations")
        }
    }

    // Native method declarations
    external fun addMatrices(
        rows1: Int, cols1: Int, matrix1: DoubleArray,
        rows2: Int, cols2: Int, matrix2: DoubleArray
    ): DoubleArray?

    external fun subtractMatrices(
        rows1: Int, cols1: Int, matrix1: DoubleArray,
        rows2: Int, cols2: Int, matrix2: DoubleArray
    ): DoubleArray?

    external fun multiplyMatrices(
        rows1: Int, cols1: Int, matrix1: DoubleArray,
        rows2: Int, cols2: Int, matrix2: DoubleArray
    ): DoubleArray?

    external fun divideMatrices(
        rows1: Int, cols1: Int, matrix1: DoubleArray,
        rows2: Int, cols2: Int, matrix2: DoubleArray
    ): DoubleArray?
}