@echo off
set JAVA_HOME=C:\Users\CHINMAY\.gradle\jdks\eclipse_adoptium-17-amd64-windows.2
set PATH=%JAVA_HOME%\bin;C:\Windows\System32;%PATH%
call "C:\Users\CHINMAY\Downloads\netbeans-31-bin\netbeans\java\maven\bin\mvn.cmd" clean package
pause
