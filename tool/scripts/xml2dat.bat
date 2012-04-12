@echo off
REM *** java.exe should be in your PATH

java -classpath ../lib/cb2xml.jar net.sf.cb2xml.Xml2Dat %1 %2

