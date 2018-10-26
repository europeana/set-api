@echo off

set len=1001

call :sub >output.txt
exit /b

:sub

:: Loop %len% times
for /L %%b IN (1, 1, %len%) do (

  echo "http://data.europeana.eu/item/100000/%%b",
)




