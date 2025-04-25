package com.example.matrixcalculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.matrixcalculator.model.Matrix
import com.example.matrixcalculator.model.MatrixResult
import com.example.matrixcalculator.model.OperationType
import com.example.matrixcalculator.viewmodel.MatrixViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private val viewModel: MatrixViewModel by viewModels()

    // UI Components
    private lateinit var spinnerOperation: AutoCompleteTextView
    private lateinit var buttonCalculate: MaterialButton
    private lateinit var editTextRows1: TextInputEditText
    private lateinit var editTextCols1: TextInputEditText
    private lateinit var editTextRows2: TextInputEditText
    private lateinit var editTextCols2: TextInputEditText
    private lateinit var buttonSetDimensions: MaterialButton
    private lateinit var gridLayoutMatrix1: GridLayout
    private lateinit var gridLayoutMatrix2: GridLayout
    private lateinit var gridLayoutResult: GridLayout
    private lateinit var cardMatrix1: MaterialCardView
    private lateinit var cardMatrix2: MaterialCardView
    private lateinit var cardResult: MaterialCardView
    private lateinit var textViewError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        initializeViews()

        // Set up operation dropdown
        setupOperationDropdown()

        // Add validation to dimension inputs
        setupDimensionValidation()

        // Set up button listeners
        setupButtonListeners()

        // Observe ViewModel state
        observeViewModel()
    }

    private fun initializeViews() {
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
        cardMatrix1 = findViewById(R.id.cardMatrix1)
        cardMatrix2 = findViewById(R.id.cardMatrix2)
        cardResult = findViewById(R.id.cardResult)
        textViewError = findViewById(R.id.textViewError)

        // Initially hide matrix cards and error
        cardMatrix1.visibility = View.GONE
        cardMatrix2.visibility = View.GONE
        cardResult.visibility = View.GONE
        textViewError.visibility = View.GONE
        buttonCalculate.isEnabled = false
    }

    private fun setupOperationDropdown() {
        val operations = OperationType.values().map { it.displayName }.toTypedArray()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            operations
        )
        spinnerOperation.setAdapter(adapter)
        spinnerOperation.setText(operations[0], false)

        spinnerOperation.setOnItemClickListener { _, _, position, _ ->
            viewModel.setOperation(OperationType.values()[position])
        }
    }

    private fun setupDimensionValidation() {
        val dimensionInputs = listOf(editTextRows1, editTextCols1, editTextRows2, editTextCols2)

        dimensionInputs.forEach { editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()
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
            })
        }
    }

    private fun setupButtonListeners() {
        buttonSetDimensions.setOnClickListener {
            setupMatrixGrids()
        }

        buttonCalculate.setOnClickListener {
            viewModel.calculate()
        }
    }

    private fun observeViewModel() {
        viewModel.matrix1.observe(this, Observer { matrix ->
            matrix?.let {
                updateMatrixUI(matrix, gridLayoutMatrix1, 1)
                cardMatrix1.visibility = View.VISIBLE
            }
        })

        viewModel.matrix2.observe(this, Observer { matrix ->
            matrix?.let {
                updateMatrixUI(matrix, gridLayoutMatrix2, 2)
                cardMatrix2.visibility = View.VISIBLE
            }
        })

        viewModel.result.observe(this, Observer { result ->
            result?.let {
                displayResult(it)
                cardResult.visibility = View.VISIBLE
            } ?: run {
                cardResult.visibility = View.GONE
            }
        })

        viewModel.error.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                textViewError.text = it
                textViewError.visibility = View.VISIBLE
            } ?: run {
                textViewError.visibility = View.GONE
            }
        })

        viewModel.isCalculating.observe(this, Observer { isCalculating ->
            buttonCalculate.isEnabled = !isCalculating
            if (isCalculating) {
                buttonCalculate.text = "Calculating..."
            } else {
                buttonCalculate.text = "Calculate"
            }
        })
    }

    private fun setupMatrixGrids() {
        try {
            viewModel.clearError()

            val rows1 = editTextRows1.text.toString().toIntOrNull() ?: 0
            val cols1 = editTextCols1.text.toString().toIntOrNull() ?: 0
            val rows2 = editTextRows2.text.toString().toIntOrNull() ?: 0
            val cols2 = editTextCols2.text.toString().toIntOrNull() ?: 0

            // Validate dimensions
            if (rows1 <= 0 || cols1 <= 0 || rows2 <= 0 || cols2 <= 0) {
                viewModel.clearError()
                textViewError.text = "All dimensions must be positive numbers"
                textViewError.visibility = View.VISIBLE
                return
            }

            if (rows1 > 10 || cols1 > 10 || rows2 > 10 || cols2 > 10) {
                viewModel.clearError()
                textViewError.text = "Maximum dimension is 10"
                textViewError.visibility = View.VISIBLE
                return
            }

            // Set dimensions in ViewModel
            viewModel.setMatrix1Dimensions(rows1, cols1)
            viewModel.setMatrix2Dimensions(rows2, cols2)

            // Enable calculate button
            buttonCalculate.isEnabled = true

        } catch (e: NumberFormatException) {
            viewModel.clearError()
            textViewError.text = "Please enter valid dimensions"
            textViewError.visibility = View.VISIBLE
        }
    }

    private fun updateMatrixUI(matrix: Matrix, gridLayout: GridLayout, matrixNumber: Int) {
        gridLayout.removeAllViews()
        gridLayout.rowCount = matrix.rows
        gridLayout.columnCount = matrix.cols

        for (i in 0 until matrix.rows) {
            for (j in 0 until matrix.cols) {
                val editText = EditText(this)
                editText.hint = "0"
                editText.minWidth = 100
                editText.setText(if (matrix.elements[i][j] != 0.0) matrix.elements[i][j].toString() else "")
                editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                        android.text.InputType.TYPE_NUMBER_FLAG_SIGNED

                // Add value change listener
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString().toDoubleOrNull() ?: 0.0
                        if (matrixNumber == 1) {
                            viewModel.updateMatrix1Value(i, j, value)
                        } else {
                            viewModel.updateMatrix2Value(i, j, value)
                        }
                    }
                })

                // Set up layout params
                val params = GridLayout.LayoutParams()
                params.width = GridLayout.LayoutParams.WRAP_CONTENT
                params.height = GridLayout.LayoutParams.WRAP_CONTENT
                params.setMargins(8, 8, 8, 8)
                params.rowSpec = GridLayout.spec(i)
                params.columnSpec = GridLayout.spec(j)

                editText.layoutParams = params
                gridLayout.addView(editText)
            }
        }
    }

    private fun displayResult(result: MatrixResult) {
        gridLayoutResult.removeAllViews()
        gridLayoutResult.rowCount = result.rows
        gridLayoutResult.columnCount = result.cols

        for (i in 0 until result.rows) {
            for (j in 0 until result.cols) {
                val textView = TextView(this)
                val value = result.elements[i][j]
                textView.text = String.format("%.4f", value)
                textView.textSize = 16f
                textView.setPadding(10, 10, 10, 10)
                textView.setBackgroundColor(ContextCompat.getColor(this, R.color.secondaryLightColor))

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
    }
}