package com.example.matrixcalculator

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var matrixOperations: MatrixOperations
    private lateinit var spinnerOperation: Spinner
    private lateinit var buttonCalculate: Button
    private lateinit var editTextRows1: EditText
    private lateinit var editTextCols1: EditText
    private lateinit var editTextRows2: EditText
    private lateinit var editTextCols2: EditText
    private lateinit var buttonSetDimensions: Button
    private lateinit var gridLayoutMatrix1: GridLayout
    private lateinit var gridLayoutMatrix2: GridLayout
    private lateinit var gridLayoutResult: GridLayout
    private lateinit var scrollViewResult: ScrollView
    private lateinit var textViewError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize matrix operations
        matrixOperations = MatrixOperations()

        // Initialize UI elements
        spinnerOperation = findViewById(R.id.spinnerOperation)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        editTextRows1 = findViewById(R.id.editTextRows1)
        editTextCols1 = findViewById(R.id.editTextCols1)
        editTextRows2 = findViewById(R.id.editTextRows2)
        editTextCols2 = findViewById(R.id.editTextCols2)
        buttonSetDimensions = findViewById(R.id.buttonSetDimensions)
        gridLayoutMatrix1 = findViewById(R.id.gridLayoutMatrix1)
        gridLayoutMatrix2 = findViewById(R.id.gridLayoutMatrix2)
        gridLayoutResult = findViewById(R.id.gridLayoutResult)
        scrollViewResult = findViewById(R.id.scrollViewResult)
        textViewError = findViewById(R.id.textViewError)

        // Set up operation spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.matrix_operations,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerOperation.adapter = adapter
        }

        // Add validation to dimension inputs
        setupDimensionValidation()

        // Set up button listeners
        buttonSetDimensions.setOnClickListener {
            setupMatrixGrids()
        }

        buttonCalculate.setOnClickListener {
            calculateResult()
        }

        // Set initial visibility
        scrollViewResult.visibility = View.GONE
        textViewError.visibility = View.GONE
    }

    private fun setupDimensionValidation() {
        val dimensionInputs = listOf(editTextRows1, editTextCols1, editTextRows2, editTextCols2)

        dimensionInputs.forEach { editText ->
            editText.addTextChangedListener {
                val text = it.toString()
                if (text.isNotEmpty()) {
                    try {
                        val value = text.toInt()
                        if (value <= 0) {
                            editText.error = "Value must be positive"
                        } else if (value > 10) {
                            editText.error = "Maximum dimension is 10"
                        } else {
                            editText.error = null
                        }
                    } catch (e: NumberFormatException) {
                        editText.error = "Invalid number"
                    }
                }
            }
        }
    }

    private fun setupMatrixGrids() {
        try {
            val rows1 = editTextRows1.text.toString().toInt()
            val cols1 = editTextCols1.text.toString().toInt()
            val rows2 = editTextRows2.text.toString().toInt()
            val cols2 = editTextCols2.text.toString().toInt()

            // Validate dimensions
            if (rows1 <= 0 || cols1 <= 0 || rows2 <= 0 || cols2 <= 0) {
                showError("All dimensions must be positive numbers")
                return
            }

            if (rows1 > 10 || cols1 > 10 || rows2 > 10 || cols2 > 10) {
                showError("Maximum dimension is 10")
                return
            }

            // Clear existing grids
            gridLayoutMatrix1.removeAllViews()
            gridLayoutMatrix2.removeAllViews()
            gridLayoutResult.removeAllViews()
            scrollViewResult.visibility = View.GONE
            textViewError.visibility = View.GONE

            // Set up grid layouts - use fixed width
            gridLayoutMatrix1.rowCount = rows1
            gridLayoutMatrix1.columnCount = cols1
            gridLayoutMatrix2.rowCount = rows2
            gridLayoutMatrix2.columnCount = cols2

            // Create input fields for Matrix 1
            for (i in 0 until rows1) {
                for (j in 0 until cols1) {
                    val editText = EditText(this)
                    editText.hint = "0"
                    editText.minWidth = 100
                    editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                            android.text.InputType.TYPE_NUMBER_FLAG_SIGNED

                    // Use simpler layout params for better visibility
                    val params = GridLayout.LayoutParams()
                    params.width = GridLayout.LayoutParams.WRAP_CONTENT
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT
                    params.setMargins(8, 8, 8, 8)
                    params.rowSpec = GridLayout.spec(i)
                    params.columnSpec = GridLayout.spec(j)

                    editText.layoutParams = params
                    gridLayoutMatrix1.addView(editText)
                }
            }

            // Create input fields for Matrix 2
            for (i in 0 until rows2) {
                for (j in 0 until cols2) {
                    val editText = EditText(this)
                    editText.hint = "0"
                    editText.minWidth = 100
                    editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                            android.text.InputType.TYPE_NUMBER_FLAG_SIGNED

                    // Use simpler layout params for better visibility
                    val params = GridLayout.LayoutParams()
                    params.width = GridLayout.LayoutParams.WRAP_CONTENT
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT
                    params.setMargins(8, 8, 8, 8)
                    params.rowSpec = GridLayout.spec(i)
                    params.columnSpec = GridLayout.spec(j)

                    editText.layoutParams = params
                    gridLayoutMatrix2.addView(editText)
                }
            }

            // Ensure grids are visible
            gridLayoutMatrix1.visibility = View.VISIBLE
            gridLayoutMatrix2.visibility = View.VISIBLE

            buttonCalculate.isEnabled = true
        } catch (e: NumberFormatException) {
            showError("Please enter valid dimensions")
        }
    }

    private fun calculateResult() {
        try {
            val rows1 = editTextRows1.text.toString().toInt()
            val cols1 = editTextCols1.text.toString().toInt()
            val rows2 = editTextRows2.text.toString().toInt()
            val cols2 = editTextCols2.text.toString().toInt()

            // Get matrix values
            val matrix1 = getMatrixValues(gridLayoutMatrix1, rows1, cols1)
            val matrix2 = getMatrixValues(gridLayoutMatrix2, rows2, cols2)

            // Perform the selected operation
            val operation = spinnerOperation.selectedItem.toString()
            val resultMatrix = when (operation) {
                "Addition" -> {
                    if (rows1 != rows2 || cols1 != cols2) {
                        showError("Matrices must have the same dimensions for addition")
                        return
                    }
                    matrixOperations.addMatrices(rows1, cols1, matrix1, rows2, cols2, matrix2)
                }
                "Subtraction" -> {
                    if (rows1 != rows2 || cols1 != cols2) {
                        showError("Matrices must have the same dimensions for subtraction")
                        return
                    }
                    matrixOperations.subtractMatrices(rows1, cols1, matrix1, rows2, cols2, matrix2)
                }
                "Multiplication" -> {
                    if (cols1 != rows2) {
                        showError("Number of columns in first matrix must equal number of rows in second matrix for multiplication")
                        return
                    }
                    matrixOperations.multiplyMatrices(rows1, cols1, matrix1, rows2, cols2, matrix2)
                }
                "Division" -> {
                    if (rows2 != cols2) {
                        showError("Second matrix must be square for division (to compute inverse)")
                        return
                    }
                    matrixOperations.divideMatrices(rows1, cols1, matrix1, rows2, cols2, matrix2)
                }
                else -> null
            }

            if (resultMatrix == null) {
                showError("Operation failed. Please check the matrix dimensions and values.")
                return
            }

            // Calculate result dimensions
            val resultRows: Int
            val resultCols: Int
            when (operation) {
                "Addition", "Subtraction" -> {
                    resultRows = rows1
                    resultCols = cols1
                }
                "Multiplication", "Division" -> {
                    resultRows = rows1
                    resultCols = cols2
                }
                else -> {
                    resultRows = 0
                    resultCols = 0
                }
            }

            // Display the result
            displayResult(resultMatrix, resultRows, resultCols)
            textViewError.visibility = View.GONE
            scrollViewResult.visibility = View.VISIBLE
        } catch (e: Exception) {
            showError("Calculation error: ${e.message}")
        }
    }

    private fun getMatrixValues(gridLayout: GridLayout, rows: Int, cols: Int): DoubleArray {
        val values = DoubleArray(rows * cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val index = i * cols + j
                val editText = gridLayout.children.elementAt(index) as EditText
                val text = editText.text.toString()
                values[index] = if (text.isNotEmpty()) text.toDouble() else 0.0
            }
        }
        return values
    }

    private fun displayResult(matrix: DoubleArray, rows: Int, cols: Int) {
        gridLayoutResult.removeAllViews()
        gridLayoutResult.rowCount = rows
        gridLayoutResult.columnCount = cols

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val textView = TextView(this)
                val value = matrix[i * cols + j]
                textView.text = String.format("%.4f", value)
                textView.textSize = 16f
                textView.setPadding(10, 10, 10, 10)
                textView.setBackgroundColor(0xFFE8F5E9.toInt())

                // Use simpler layout params for better visibility
                val params = GridLayout.LayoutParams()
                params.width = GridLayout.LayoutParams.WRAP_CONTENT
                params.height = GridLayout.LayoutParams.WRAP_CONTENT
                params.setMargins(8, 8, 8, 8)
                params.rowSpec = GridLayout.spec(i)
                params.columnSpec = GridLayout.spec(j)

                textView.layoutParams = params
                gridLayoutResult.addView(textView)
            }
        }

        // Ensure the result is visible
        gridLayoutResult.visibility = View.VISIBLE
        scrollViewResult.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        textViewError.text = message
        textViewError.visibility = View.VISIBLE
        scrollViewResult.visibility = View.GONE
    }
}