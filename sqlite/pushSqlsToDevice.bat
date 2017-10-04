
@ECHO off

SET ADB_EXE=C:\tools\Android\sdk\platform-tools\adb
SET DEVICE=Medfield7682D10F
SET DB_VER=1
SET SRC_DIR=./sqls
SET DST_DIR=/Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/sqls

ECHO Push SQL Files to Device for DB_VER: %DB_VER%

%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.data.drops.%DB_VER%.sql		%DST_DIR%/jk.data.drops.%DB_VER%.sql
%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.log.drops.%DB_VER%.sql  	%DST_DIR%/jk.log.drops.%DB_VER%.sql
%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.meta.drops.%DB_VER%.sql 	%DST_DIR%/jk.meta.drops.%DB_VER%.sql

%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.data.creates.%DB_VER%.sql	%DST_DIR%/jk.data.creates.%DB_VER%.sql
%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.log.creates.%DB_VER%.sql	%DST_DIR%/jk.log.creates.%DB_VER%.sql
%ADB_EXE% -s %DEVICE% push %SRC_DIR%/jk.meta.creates.%DB_VER%.sql	%DST_DIR%/jk.meta.creates.%DB_VER%.sql

