@echo off
echo Cleaning Gradle caches for Android build issues...
echo.

echo Stopping Gradle daemon...
gradlew --stop

echo.
echo Cleaning project...
gradlew clean

echo.
echo Deleting Gradle cache directories...
rmdir /S %USERPROFILE%\.gradle\caches\transforms-3
rmdir /S %USERPROFILE%\.gradle\caches\modules-2\files-2.1\androidx.compose
rmdir /S %USERPROFILE%\.gradle\caches\modules-2\files-2.1\com.android.tools.build

echo.
echo Deleting Android Studio build caches...
rmdir /S build
rmdir /S app\build
rmdir /S .gradle

echo.
echo Cleanup complete! Now try rebuilding your project.
echo.
pause