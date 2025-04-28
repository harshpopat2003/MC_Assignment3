package com.example.matrixcalculator.model

/**
 * Represents a matrix with its dimensions (rows and columns) and elements.
 * The matrix elements are stored as a 2D array of Doubles.
 */
data class Matrix(
    val rows: Int,  // Number of rows in the matrix
    val cols: Int,  // Number of columns in the matrix
    val elements: Array<DoubleArray>  // 2D array to store the matrix elements
) {
    /**
     * Checks if the current matrix is equal to another matrix.
     * The matrices are considered equal if they have the same dimensions (rows and cols)
     * and if all the corresponding elements are equal.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true  // If both are the same object, they are equal
        if (javaClass != other?.javaClass) return false  // If they are not of the same class, they are not equal

        other as Matrix  // Cast the other object to a Matrix for comparison

        if (rows != other.rows) return false  // If the number of rows is different, they are not equal
        if (cols != other.cols) return false  // If the number of columns is different, they are not equal
        if (!elements.contentDeepEquals(other.elements)) return false  // If the elements are not the same, they are not equal

        return true  // The matrices are equal
    }

    /**
     * Returns the hash code for the matrix. The hash code is computed using the
     * number of rows, columns, and the elements of the matrix.
     */
    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols  // Include the number of columns in the hash code
        result = 31 * result + elements.contentDeepHashCode()  // Include the hash of elements in the hash code
        return result
    }
}

/**
 * Represents the result of a matrix operation.
 * Contains the resulting matrix's dimensions, elements, and the type of operation performed.
 */
data class MatrixResult(
    val rows: Int,  // Number of rows in the result matrix
    val cols: Int,  // Number of columns in the result matrix
    val elements: Array<DoubleArray>,  // 2D array to store the result matrix elements
    val operation: OperationType  // The type of matrix operation performed (e.g., addition, subtraction)
) {
    /**
     * Checks if the current result is equal to another result.
     * The results are considered equal if they have the same dimensions,
     * elements, and operation type.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true  // If both are the same object, they are equal
        if (javaClass != other?.javaClass) return false  // If they are not of the same class, they are not equal

        other as MatrixResult  // Cast the other object to a MatrixResult for comparison

        if (rows != other.rows) return false  // If the number of rows is different, they are not equal
        if (cols != other.cols) return false  // If the number of columns is different, they are not equal
        if (!elements.contentDeepEquals(other.elements)) return false  // If the elements are not the same, they are not equal
        if (operation != other.operation) return false  // If the operation type is different, they are not equal

        return true  // The matrix results are equal
    }

    /**
     * Returns the hash code for the matrix result. The hash code is computed using the
     * number of rows, columns, elements, and the operation type.
     */
    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols  // Include the number of columns in the hash code
        result = 31 * result + elements.contentDeepHashCode()  // Include the hash of elements in the hash code
        result = 31 * result + operation.hashCode()  // Include the hash code of the operation type
        return result
    }
}

/**
 * Enum representing the different types of matrix operations.
 * Each operation has a display name for easy identification.
 */
enum class OperationType(val displayName: String) {
    ADDITION("Addition"),  // Represents matrix addition
    SUBTRACTION("Subtraction"),  // Represents matrix subtraction
    MULTIPLICATION("Multiplication"),  // Represents matrix multiplication
    DIVISION("Division");  // Represents matrix division

    /**
     * Converts a string value to the corresponding OperationType enum.
     * If the string value doesn't match any operation, defaults to ADDITION.
     */
    companion object {
        fun fromString(value: String): OperationType {
            return values().find { it.displayName == value } ?: ADDITION  // Return the matching operation or ADDITION as default
        }
    }
}
