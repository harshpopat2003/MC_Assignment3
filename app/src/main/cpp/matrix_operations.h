#ifndef MATRIX_OPERATIONS_H
#define MATRIX_OPERATIONS_H

#include <jni.h>
#include <vector>

extern "C" {
// Matrix addition
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MatrixOperations_addMatrices(
        JNIEnv *env, jobject /* this */,
        jint rows1, jint cols1, jdoubleArray matrix1,
        jint rows2, jint cols2, jdoubleArray matrix2);

// Matrix subtraction
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MatrixOperations_subtractMatrices(
        JNIEnv *env, jobject /* this */,
        jint rows1, jint cols1, jdoubleArray matrix1,
        jint rows2, jint cols2, jdoubleArray matrix2);

// Matrix multiplication
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MatrixOperations_multiplyMatrices(
        JNIEnv *env, jobject /* this */,
        jint rows1, jint cols1, jdoubleArray matrix1,
        jint rows2, jint cols2, jdoubleArray matrix2);

// Matrix division (multiplication by inverse)
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MatrixOperations_divideMatrices(
        JNIEnv *env, jobject /* this */,
        jint rows1, jint cols1, jdoubleArray matrix1,
        jint rows2, jint cols2, jdoubleArray matrix2);
}

#endif // MATRIX_OPERATIONS_H