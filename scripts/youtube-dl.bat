@echo off
set str=%0
set str=%str:bat=py%
extras\Python27\python.exe %str% "-g" %1
