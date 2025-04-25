package com.example.matrixcalculator.model

/**
 * Represents a matrix with its dimensions and elements
 */
data class Matrix(
    val rows: Int,
    val cols: Int,
    val elements: Array<DoubleArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Matrix

        if (rows != other.rows) return false
        if (cols != other.cols) return false
        if (!elements.contentDeepEquals(other.elements)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + elements.contentDeepHashCode()
        return result
    }
}

/**
 * Represents the result of a matrix operation
 */
data class MatrixResult(
    val rows: Int,
    val cols: Int,
    val elements: Array<DoubleArray>,
    val operation: OperationType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MatrixResult

        if (rows != other.rows) return false
        if (cols != other.cols) return false
        if (!elements.contentDeepEquals(other.elements)) return false
        if (operation != other.operation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + elements.contentDeepHashCode()
        result = 31 * result + operation.hashCode()
        return result
    }
}

/**
 * Enum representing the different matrix operations
 */
enum class OperationType(val displayName: String) {
    ADDITION("Addition"),
    SUBTRACTION("Subtraction"),
    MULTIPLICATION("Multiplication"),
    DIVISION("Division");

    companion object {
        fun fromString(value: String): OperationType {
            return values().find { it.displayName == value } ?: ADDITION
        }
    }
}