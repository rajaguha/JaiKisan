
REM - transfer db files from fromDevice dir to emulator

ECHO Push to Emulator
@ECHO off

C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push ./dbs/fromDevice/pF.data.1.db /data/data/site.swaraj.jaikisan/databases/pF.meta.1.db
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push ./dbs/fromDevice/pF.data.1.db-shm /data/data/site.swaraj.jaikisan/databases/pF.data.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push ./dbs/fromDevice/pF.data.1.db-wal /data/data/site.swaraj.jaikisan/databases/pF.data.1.db-wal

C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push ./dbs/fromDevice/pF.log.1.db /data/data/site.swaraj.jaikisan/databases/pF.log.1.db
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push ./dbs/fromDevice/pF.log.1.db-shm /data/data/site.swaraj.jaikisan/databases/pF.log.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push ./dbs/fromDevice/pF.log.1.db-wal /data/data/site.swaraj.jaikisan/databases/pF.log.1.db-wal

C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push ./dbs/fromDevice/pF.meta.1.db /data/data/site.swaraj.jaikisan/databases/pF.meta.1.db
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push ./dbs/fromDevice/pF.meta.1.db-shm /data/data/site.swaraj.jaikisan/databases/pF.meta.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push ./dbs/fromDevice/pF.meta.1.db-wal /data/data/site.swaraj.jaikisan/databases/pF.meta.1.db-wal
