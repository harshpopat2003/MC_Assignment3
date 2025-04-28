# Matrix Calculator

## Introduction
Matrix Calculator Android app that allows users to perform basic matrix operations including addition, subtraction, multiplication, and division using a combination of Kotlin and native C++ code.

## App ScreenShot

### Main Homepage screen
<img src=./images/main_homepage.jpg  alt="Main Screen" width="200"/>

### Calculating results 
<img src=./images/calculation_res.jpg  alt="flight Screen" width="200"/>

### All Operation Available 
<img src=./images/operations_avl.jpg  alt="operations" width="200"/>



## Features
- Define and validate matrix dimensions with input constraints (min: 1, max: 10)
- Perform matrix arithmetic operations:
  - **Addition:** Adds two matrices of equal dimensions.
  - **Subtraction:** Subtracts one matrix from another of equal dimensions.
  - **Multiplication:** Multiplies two matrices (columns of Matrix 1 must equal rows of Matrix 2).
  - **Division:** Multiplies Matrix 1 by the inverse of Matrix 2 (Matrix 2 must be square and invertible).
- Error handling and input validation.
- Integration with native C++ code via JNI using the Eigen library for efficient calculations.
- MVVM architecture using a ViewModel to manage state and business logic.

## How It Works
- **Data Model:**  
  The `Matrix.kt` file defines the data classes for matrices (`Matrix`) and their operation results (`MatrixResult`), along with the `OperationType` enum to represent supported operations.

- **ViewModel:**  
  The `MatrixViewModel.kt` handles input validations, matrix state updates, calculation logic, and error management. It prepares data from UI elements, flattens the matrices, and calls the native methods provided by `MatrixOperations.kt`.

- **UI Layer:**  
  In `MainActivity.kt`, matrix dimensions are input through text fields and matrices are dynamically displayed using GridLayout components. Users can update individual cell values, and the app reacts to changes in the ViewModel to display live results.

- **Native Integration:**  
  The `MatrixOperations.kt` declares native methods which are implemented in C++ (`matrix_operations.cpp`) using Eigen. These functions perform the actual matrix arithmetic and handle edge cases like mismatched dimensions or non-invertible matrices.

## Setup & Build Instructions
1. Clone the repository.
2. Open the project in Android Studio.
3. Ensure that the native library is properly built (using CMake or ndk-build).
4. Run the application on an Android device or emulator.

## Technologies Used
- Kotlin & Android SDK
- Android Architecture Components (ViewModel, LiveData)
- JNI and C++ (Eigen library)
- Material Design components for UI

