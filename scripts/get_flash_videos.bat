@echo off
set str=%0
set str=%str:bat=pl%
extras\perl\bin\perl.exe %str% -q --url "%*"