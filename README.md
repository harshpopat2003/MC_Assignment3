# Wifi Signal Logger

## Overview
This project is a WiFi Signal Logger application for Android that scans available WiFi networks and logs their signal strengths. The app then displays these values in a matrix format and allows comparisons between different scans.

## App ScreenShot

### Main Homepage screen
<img src=./images/home_screen.jpg  alt="Home Screen" width="200"/>

### Wifi Scan Screen
<img src=./images/wifi_scan_screen,jpg  alt="flight Screen" width="200"/>

### Wifi signal Comparison Screen
<img src=./images/comparison_screen.jpg  alt="stored Screen" width="200"/>


## Key Functionalities

- **Scanning WiFi Signals:**  
  - Implemented in `ScanActivity.kt`  
  - Uses a custom `WifiScanner` to perform multiple WiFi scans until 100 signal readings are collected.  
  - Processes scan results into a grid (10x10 matrix), stores data in a Room database, and displays results in real-time.

- **Comparing WiFi Scans:**  
  - Implemented in `ComparisonActivity.kt`  
  - Loads the three most recent scan sessions and compares their WiFi signal statistics (min, max, average, and range).  
  - Presents data via multiple RecyclerViews and a statistics table.

- **Main Application Flow:**  
  - Handled in `MainActivity.kt`  
  - Provides a list of previous scan sessions and options to start a new scan or compare existing data.  
  - Enforces necessary permissions and uses Android’s navigation to switch between activities.

- **Database & Repository Pattern:**  
  - Utilizes Room database support via `AppDatabase.kt`, `LocationDao.kt`, and `WifiReadingDao.kt`.  
  - `WifiRepository.kt` abstracts data operations ensuring a clean separation of concerns.
  
## Project Structure
- **Activities:**  
  - `ScanActivity.kt`: Manages scanning and logging of WiFi signals.
  - `ComparisonActivity.kt`: Compares data from different scan sessions.
  - `MainActivity.kt`: Entry point offering scan and compare options.

- **Data Layer:**  
  - Models: `Location`, `WifiReading`, and `LocationWithReadings`.
  - Data Access: DAOs (`LocationDao.kt` and `WifiReadingDao.kt`) and the Room database (`AppDatabase.kt`).
  - Repository: `WifiRepository.kt` provides a unified interface to the data sources.

## Requirements & Setup
- **Permissions:**  
  The app requires the following permissions to function:  
  - `ACCESS_FINE_LOCATION`  
  - `ACCESS_WIFI_STATE`  
  - `CHANGE_WIFI_STATE`  
  - For Android 12+ devices, `BLUETOOTH_SCAN` is also requested.
  
- **Building the Project:**  
  - Open the project in Android Studio.
  - Sync Gradle and build the project.
  - Install the app on an Android device or emulator with WiFi capabilities.

## Usage
1. Launch the app.  
2. Start a new scan from the main menu (`MainActivity`).  
3. Wait for the scan to complete and view the WiFi signal matrix.  
4. Access the comparison screen (`ComparisonActivity`) to analyze statistics from the three most recent scans.

## Conclusion
This WiFi Signal Logger is built using Android Architecture Components, Kotlin Coroutines, and Room. It provides a clear demonstration of scanning, processing, and comparing WiFi signal data in a structured application.
