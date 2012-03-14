@echo off
set str=%0
set str=%str:bat=pl%
set str=%str:_update=%
extras\perl\bin\perl.exe %str% --xml-alpha --mythtv c:\tmp\list 