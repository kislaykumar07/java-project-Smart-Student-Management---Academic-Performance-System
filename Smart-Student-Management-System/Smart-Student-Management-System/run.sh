#!/bin/bash
# Compile and run the Smart Student Management System (Linux / macOS)
set -e
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.sms.app.Main
