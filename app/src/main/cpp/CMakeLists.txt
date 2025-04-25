cmake_minimum_required(VERSION 3.4.1)
project(MatrixCalculator)

# Use the Eigen3 library
# NOTE: For this approach, you'll need to modify your app/build.gradle.kts file to include:
# android {
#     ...
#     externalNativeBuild {
#         cmake {
#             path = file("src/main/cpp/CMakeLists.txt")
#             version = "3.22.1"
#         }
#     }
#     ...
# }

# Define the library
add_library(
        matrix_operations
        SHARED
        matrix_operations.cpp
)

# For this approach, you'll need to install the Eigen library header files
# in your project manually:
# - Create directory: app/src/main/cpp/eigen3
# - Download Eigen 3.4.0 from https://gitlab.com/libeigen/eigen/-/archive/3.4.0/eigen-3.4.0.zip
# - Extract the contents to the eigen3 directory

# Include Eigen3 headers
target_include_directories(matrix_operations PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

# Find the required libraries
find_library(log-lib log)

# Link the libraries
target_link_libraries(
        matrix_operations
        ${log-lib}
)