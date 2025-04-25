package com.example.matrixcalculator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.matrixcalculator.MatrixOperations
import com.example.matrixcalculator.model.Matrix
import com.example.matrixcalculator.model.MatrixResult
import com.example.matrixcalculator.model.OperationType

class MatrixViewModel : ViewModel() {
    private val matrixOperations = MatrixOperations()

    // State management
    private val _matrix1 = MutableLiveData<Matrix>()
    val matrix1: LiveData<Matrix> = _matrix1

    private val _matrix2 = MutableLiveData<Matrix>()
    val matrix2: LiveData<Matrix> = _matrix2

    private val _result = MutableLiveData<MatrixResult>()
    val result: LiveData<MatrixResult> = _result

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isCalculating = MutableLiveData(false)
    val isCalculating: LiveData<Boolean> = _isCalculating

    // Selected operation
    private val _selectedOperation = MutableLiveData(OperationType.ADDITION)
    val selectedOperation: LiveData<OperationType> = _selectedOperation

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

    fun updateMatrix1Value(row: Int, col: Int, value: Double) {
        val currentMatrix = _matrix1.value ?: return
        val newElements = currentMatrix.elements.map { it.clone() }.toTypedArray()
        newElements[row][col] = value
        _matrix1.value = Matrix(currentMatrix.rows, currentMatrix.cols, newElements)
    }

    fun updateMatrix2Value(row: Int, col: Int, value: Double) {
        val currentMatrix = _matrix2.value ?: return
        val newElements = currentMatrix.elements.map { it.clone() }.toTypedArray()
        newElements[row][col] = value
        _matrix2.value = Matrix(currentMatrix.rows, currentMatrix.cols, newElements)
    }

    fun setOperation(operation: OperationType) {
        _selectedOperation.value = operation
    }

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

            // Flatten the matrices
            val flatMatrix1 = flattenMatrix(matrix1)
            val flatMatrix2 = flattenMatrix(matrix2)

            // Determine result dimensions
            val (resultRows, resultCols) = when(_selectedOperation.value) {
                OperationType.ADDITION, OperationType.SUBTRACTION ->
                    Pair(matrix1.rows, matrix1.cols)
                OperationType.MULTIPLICATION, OperationType.DIVISION ->
                    Pair(matrix1.rows, matrix2.cols)
                else ->
                    Pair(0, 0)
            }

            // Perform the calculation
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

            if (resultArray == null) {
                _error.value = "Calculation failed. Please check your matrices."
                _isCalculating.value = false
                return
            }

            // Convert the flat array back to a 2D array
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
            _isCalculating.value = false
        }
    }

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

    private fun unflattenMatrix(flatArray: DoubleArray, rows: Int, cols: Int): Array<DoubleArray> {
        val result = Array(rows) { DoubleArray(cols) }

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i][j] = flatArray[i * cols + j]
            }
        }

        return result
    }

    fun clearError() {
        _error.value = null
    }
}