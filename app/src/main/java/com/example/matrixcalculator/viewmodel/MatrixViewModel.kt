package com.example.matrixcalculator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.matrixcalculator.MatrixOperations
import com.example.matrixcalculator.model.Matrix
import com.example.matrixcalculator.model.MatrixResult
import com.example.matrixcalculator.model.OperationType

/**
 * ViewModel class for managing matrix-related operations and state.
 * This class handles the logic for matrix initialization, updating values, and performing operations.
 */
class MatrixViewModel : ViewModel() {
    private val matrixOperations = MatrixOperations()  // Object to handle matrix operations (addition, subtraction, etc.)

    // LiveData properties to manage state
    private val _matrix1 = MutableLiveData<Matrix>()
    val matrix1: LiveData<Matrix> = _matrix1  // Exposed as LiveData to be observed in UI

    private val _matrix2 = MutableLiveData<Matrix>()
    val matrix2: LiveData<Matrix> = _matrix2  // Exposed as LiveData to be observed in UI

    private val _result = MutableLiveData<MatrixResult>()
    val result: LiveData<MatrixResult> = _result  // Exposed as LiveData to show operation result

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error  // Exposed as LiveData to show error messages

    private val _isCalculating = MutableLiveData(false)
    val isCalculating: LiveData<Boolean> = _isCalculating  // Exposed as LiveData to indicate if a calculation is in progress

    // LiveData for the selected operation (e.g., ADDITION, MULTIPLICATION)
    private val _selectedOperation = MutableLiveData(OperationType.ADDITION)
    val selectedOperation: LiveData<OperationType> = _selectedOperation

    /**
     * Sets the dimensions for Matrix 1 and initializes it with zero values.
     * Ensures that the dimensions are valid (between 1 and 10).
     */
    fun setMatrix1Dimensions(rows: Int, cols: Int) {
        if (rows <= 0 || cols <= 0 || rows > 10 || cols > 10) {
            _error.value = "Invalid matrix dimensions. Please use values between 1 and 10."
            return
        }

        // Initialize matrix with zeros
        val elements = Array(rows) { DoubleArray(cols) { 0.0 } }
        _matrix1.value = Matrix(rows, cols, elements)
        _result.value = null // Clear previous result
    }

    /**
     * Sets the dimensions for Matrix 2 and initializes it with zero values.
     * Ensures that the dimensions are valid (between 1 and 10).
     */
    fun setMatrix2Dimensions(rows: Int, cols: Int) {
        if (rows <= 0 || cols <= 0 || rows > 10 || cols > 10) {
            _error.value = "Invalid matrix dimensions. Please use values between 1 and 10."
            return
        }

        // Initialize matrix with zeros
        val elements = Array(rows) { DoubleArray(cols) { 0.0 } }
        _matrix2.value = Matrix(rows, cols, elements)
        _result.value = null // Clear previous result
    }

    /**
     * Updates the value of a specific element in Matrix 1 at the given row and column.
     */
    fun updateMatrix1Value(row: Int, col: Int, value: Double) {
        val currentMatrix = _matrix1.value ?: return
        val newElements = currentMatrix.elements.map { it.clone() }.toTypedArray()  // Create a new copy of the matrix elements
        newElements[row][col] = value  // Update the value at the specified position
        _matrix1.value = Matrix(currentMatrix.rows, currentMatrix.cols, newElements)  // Update Matrix 1
    }

    /**
     * Updates the value of a specific element in Matrix 2 at the given row and column.
     */
    fun updateMatrix2Value(row: Int, col: Int, value: Double) {
        val currentMatrix = _matrix2.value ?: return
        val newElements = currentMatrix.elements.map { it.clone() }.toTypedArray()  // Create a new copy of the matrix elements
        newElements[row][col] = value  // Update the value at the specified position
        _matrix2.value = Matrix(currentMatrix.rows, currentMatrix.cols, newElements)  // Update Matrix 2
    }

    /**
     * Sets the operation to be performed on the matrices (e.g., ADDITION, MULTIPLICATION).
     */
    fun setOperation(operation: OperationType) {
        _selectedOperation.value = operation
    }

    /**
     * Validates the matrices and the selected operation before performing a calculation.
     * Checks for compatible dimensions and operation-specific constraints.
     */
    fun validateCalculation(): Boolean {
        val matrix1 = _matrix1.value
        val matrix2 = _matrix2.value

        if (matrix1 == null || matrix2 == null) {
            _error.value = "Please set dimensions for both matrices"
            return false
        }

        // Validation based on operation type
        return when (_selectedOperation.value) {
            OperationType.ADDITION, OperationType.SUBTRACTION -> {
                if (matrix1.rows != matrix2.rows || matrix1.cols != matrix2.cols) {
                    _error.value = "Matrices must have the same dimensions for ${_selectedOperation.value?.displayName}"
                    false
                } else {
                    true
                }
            }
            OperationType.MULTIPLICATION -> {
                if (matrix1.cols != matrix2.rows) {
                    _error.value = "For multiplication, columns of Matrix 1 must equal rows of Matrix 2"
                    false
                } else {
                    true
                }
            }
            OperationType.DIVISION -> {
                if (matrix2.rows != matrix2.cols) {
                    _error.value = "For division, Matrix 2 must be square"
                    false
                } else {
                    true
                }
            }
            else -> {
                _error.value = "Unknown operation"
                false
            }
        }
    }

    /**
     * Performs the matrix calculation based on the selected operation and updates the result.
     */
    fun calculate() {
        // Clear previous errors and results
        _error.value = null

        if (!validateCalculation()) {
            return
        }

        _isCalculating.value = true

        try {
            val matrix1 = _matrix1.value!!
            val matrix2 = _matrix2.value!!

            // Flatten the matrices to 1D arrays for easier calculation
            val flatMatrix1 = flattenMatrix(matrix1)
            val flatMatrix2 = flattenMatrix(matrix2)

            // Determine result dimensions based on the operation
            val (resultRows, resultCols) = when(_selectedOperation.value) {
                OperationType.ADDITION, OperationType.SUBTRACTION ->
                    Pair(matrix1.rows, matrix1.cols)
                OperationType.MULTIPLICATION, OperationType.DIVISION ->
                    Pair(matrix1.rows, matrix2.cols)
                else -> Pair(0, 0)
            }

            // Perform the calculation based on the selected operation
            val resultArray = when(_selectedOperation.value) {
                OperationType.ADDITION ->
                    matrixOperations.addMatrices(
                        matrix1.rows, matrix1.cols, flatMatrix1,
                        matrix2.rows, matrix2.cols, flatMatrix2
                    )
                OperationType.SUBTRACTION ->
                    matrixOperations.subtractMatrices(
                        matrix1.rows, matrix1.cols, flatMatrix1,
                        matrix2.rows, matrix2.cols, flatMatrix2
                    )
                OperationType.MULTIPLICATION ->
                    matrixOperations.multiplyMatrices(
                        matrix1.rows, matrix1.cols, flatMatrix1,
                        matrix2.rows, matrix2.cols, flatMatrix2
                    )
                OperationType.DIVISION ->
                    matrixOperations.divideMatrices(
                        matrix1.rows, matrix1.cols, flatMatrix1,
                        matrix2.rows, matrix2.cols, flatMatrix2
                    )
                else -> null
            }

            // If calculation fails, show error
            if (resultArray == null) {
                _error.value = "Calculation failed. Please check your matrices."
                _isCalculating.value = false
                return
            }

            // Convert the result array back to a 2D array
            val resultMatrix = unflattenMatrix(resultArray, resultRows, resultCols)
            _result.value = MatrixResult(
                resultRows,
                resultCols,
                resultMatrix,
                _selectedOperation.value!!
            )

        } catch (e: Exception) {
            _error.value = "Calculation error: ${e.message}"
        } finally {
            _isCalculating.value = false  // Reset the calculation state
        }
    }

    /**
     * Flattens a 2D matrix to a 1D array for easier manipulation.
     */
    private fun flattenMatrix(matrix: Matrix): DoubleArray {
        val result = DoubleArray(matrix.rows * matrix.cols)
        var index = 0

        for (i in 0 until matrix.rows) {
            for (j in 0 until matrix.cols) {
                result[index++] = matrix.elements[i][j]
            }
        }

        return result
    }

    /**
     * Converts a flattened 1D array back into a 2D matrix with the specified number of rows and columns.
     */
    private fun unflattenMatrix(flatArray: DoubleArray, rows: Int, cols: Int): Array<DoubleArray> {
        val result = Array(rows) { DoubleArray(cols) }

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i][j] = flatArray[i * cols + j]
            }
        }

        return result
    }

    /**
     * Clears any errors currently stored in the ViewModel state.
     */
    fun clearError() {
        _error.value = null
    }
}
