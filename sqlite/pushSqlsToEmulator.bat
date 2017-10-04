
@ECHO off

SET ADB_EXE=C:\tools\Android\sdk\platform-tools\adb
SET DEVICE=emulator-5554
SET DB_VER=1
SET SRC_DIR=./sqls
SET DST_DIR=/data/data/site.swaraj.jaikisan/databases/sqls

ECHO Push SQL Files to Emulator for DB_VER: %DB_VER%

%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.data.drops.%DB_VER%.sql		%DST_DIR%/jk.data.drops.%DB_VER%.sql
%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.log.drops.%DB_VER%.sql  	%DST_DIR%/jk.log.drops.%DB_VER%.sql
%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.meta.drops.%DB_VER%.sql 	%DST_DIR%/jk.meta.drops.%DB_VER%.sql

%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.data.creates.%DB_VER%.sql	%DST_DIR%/jk.data.creates.%DB_VER%.sql
%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.log.creates.%DB_VER%.sql	%DST_DIR%/jk.log.creates.%DB_VER%.sql
%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.meta.creates.%DB_VER%.sql	%DST_DIR%/jk.meta.creates.%DB_VER%.sql

