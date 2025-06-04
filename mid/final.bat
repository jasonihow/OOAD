@echo off
REM Compile WorkflowEditor.java with UTF-8 encoding and output to 'out' directory
javac -encoding UTF-8 -d out .\WorkflowEditor.java

REM Run the compiled WorkflowEditor class
cd out
java WorkflowEditor
cd ..
