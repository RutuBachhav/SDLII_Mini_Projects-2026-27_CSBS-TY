@echo off
set AS_JAVA=C:\Users\CHINMAY\.gradle\jdks\eclipse_adoptium-17-amd64-windows.2
call "C:\Users\CHINMAY\glassfish7\glassfish\bin\asadmin.bat" deploy --force=true --contextroot "/CampusCare" "target\CampusCare.war"
echo Application deployed! Open http://localhost:8080/CampusCare/ in your browser.
pause
