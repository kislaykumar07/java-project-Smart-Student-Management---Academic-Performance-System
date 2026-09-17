#!/bin/bash
# Compile and run the validation test suite
set -e
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.sms.test.SimpleTests
