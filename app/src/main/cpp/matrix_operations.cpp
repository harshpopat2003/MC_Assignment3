#include "matrix_operations.h"
#include <eigen/Eigen/Dense>
#include <android/log.h>
#include <string>
#include <stdexcept>

#define LOG_TAG "MatrixOperations"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace Eigen;

// Convert JNI double array to Eigen Matrix
MatrixXd jniArrayToEigenMatrix(JNIEnv *env, jint rows, jint cols, jdoubleArray jArray) {
    jdouble *elements = env->GetDoubleArrayElements(jArray, nullptr);
    MatrixXd matrix(rows, cols);

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            matrix(i, j) = elements[i * cols + j];
        }
    }

    env->ReleaseDoubleArrayElements(jArray, elements, JNI_ABORT);
    return matrix;
}

// Convert Eigen Matrix to JNI double array
jdoubleArray eigenMatrixToJniArray(JNIEnv *env, const MatrixXd &matrix) {
    int rows = matrix.rows();
    int cols = matrix.cols();
    jdoubleArray result = env->NewDoubleArray(rows * cols);

    jdouble *elements = new jdouble[rows * cols];
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            elements[i * cols + j] = matrix(i, j);
        }
    }

    env->SetDoubleArrayRegion(result, 0, rows * cols, elements);
    delete[] elements;
    return result;
}

extern "C" {
// Matrix addition
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MatrixOperations_addMatrices(
        JNIEnv *env, jobject /* this */,
        jint rows1, jint cols1, jdoubleArray matrix1,
        jint rows2, jint cols2, jdoubleArray matrix2) {

    LOGI("Adding matrices: %dx%d and %dx%d", rows1, cols1, rows2, cols2);

    try {
        if (rows1 != rows2 || cols1 != cols2) {
            throw std::invalid_argument("Matrices must have the same dimensions for addition");
        }

        MatrixXd eigenMatrix1 = jniArrayToEigenMatrix(env, rows1, cols1, matrix1);
        MatrixXd eigenMatrix2 = jniArrayToEigenMatrix(env, rows2, cols2, matrix2);

        MatrixXd result = eigenMatrix1 + eigenMatrix2;

        return eigenMatrixToJniArray(env, result);
    } catch (const std::exception &e) {
        LOGE("Error in matrix addition: %s", e.what());
        return nullptr;
    }
}

// Matrix subtraction
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MatrixOperations_subtractMatrices(
        JNIEnv *env, jobject /* this */,
        jint rows1, jint cols1, jdoubleArray matrix1,
        jint rows2, jint cols2, jdoubleArray matrix2) {

    LOGI("Subtracting matrices: %dx%d and %dx%d", rows1, cols1, rows2, cols2);

    try {
        if (rows1 != rows2 || cols1 != cols2) {
            throw std::invalid_argument("Matrices must have the same dimensions for subtraction");
        }

        MatrixXd eigenMatrix1 = jniArrayToEigenMatrix(env, rows1, cols1, matrix1);
        MatrixXd eigenMatrix2 = jniArrayToEigenMatrix(env, rows2, cols2, matrix2);

        MatrixXd result = eigenMatrix1 - eigenMatrix2;

        return eigenMatrixToJniArray(env, result);
    } catch (const std::exception &e) {
        LOGE("Error in matrix subtraction: %s", e.what());
        return nullptr;
    }
}

// Matrix multiplication
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MatrixOperations_multiplyMatrices(
        JNIEnv *env, jobject /* this */,
        jint rows1, jint cols1, jdoubleArray matrix1,
        jint rows2, jint cols2, jdoubleArray matrix2) {

    LOGI("Multiplying matrices: %dx%d and %dx%d", rows1, cols1, rows2, cols2);

    try {
        if (cols1 != rows2) {
            throw std::invalid_argument("Number of columns in first matrix must equal number of rows in second matrix for multiplication");
        }

        MatrixXd eigenMatrix1 = jniArrayToEigenMatrix(env, rows1, cols1, matrix1);
        MatrixXd eigenMatrix2 = jniArrayToEigenMatrix(env, rows2, cols2, matrix2);

        MatrixXd result = eigenMatrix1 * eigenMatrix2;

        return eigenMatrixToJniArray(env, result);
    } catch (const std::exception &e) {
        LOGE("Error in matrix multiplication: %s", e.what());
        return nullptr;
    }
}

// Matrix division (multiplication by inverse)
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MatrixOperations_divideMatrices(
        JNIEnv *env, jobject /* this */,
        jint rows1, jint cols1, jdoubleArray matrix1,
        jint rows2, jint cols2, jdoubleArray matrix2) {

    LOGI("Dividing matrices: %dx%d and %dx%d", rows1, cols1, rows2, cols2);

    try {
        if (rows2 != cols2) {
            throw std::invalid_argument("Second matrix must be square for division (to compute inverse)");
        }

        MatrixXd eigenMatrix1 = jniArrayToEigenMatrix(env, rows1, cols1, matrix1);
        MatrixXd eigenMatrix2 = jniArrayToEigenMatrix(env, rows2, cols2, matrix2);

        // Check if matrix2 is invertible
        double det = eigenMatrix2.determinant();
        if (std::abs(det) < 1e-10) {
            throw std::invalid_argument("Second matrix is not invertible (determinant is close to zero)");
        }

        MatrixXd result = eigenMatrix1 * eigenMatrix2.inverse();

        return eigenMatrixToJniArray(env, result);
    } catch (const std::exception &e) {
        LOGE("Error in matrix division: %s", e.what());
        return nullptr;
    }
}
}