#!/bin/bash

# Script to perform a complete clean build
# This removes all cached build artifacts and Gradle caches

echo "Cleaning Hotel Pere Maria project..."

# Clean using Gradle
echo "Running ./gradlew clean..."
./gradlew clean

# Remove Gradle daemon caches
echo "Removing .gradle directory..."
rm -rf .gradle/

# Remove all build directories
echo "Removing build directories..."
rm -rf build/
rm -rf app/build/

echo "Clean complete! You can now rebuild the project with:"
echo "  ./gradlew assembleDebug"
