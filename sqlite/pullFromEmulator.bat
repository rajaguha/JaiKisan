
REM - transfer SQLite db files from emulator to fromEmulator dir

ECHO Pulling from Emulator
@ECHO off

C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 pull /data/data/site.swaraj.jaikisan/databases/pF.meta.1.db ./dbs/fromEmulator/pF.data.1.db
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 pull /data/data/site.swaraj.jaikisan/databases/pF.data.1.db-shm ./dbs/fromEmulator/pF.data.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 pull /data/data/site.swaraj.jaikisan/databases/pF.data.1.db-wal ./dbs/fromEmulator/pF.data.1.db-wal


C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 pull /data/data/site.swaraj.jaikisan/databases/pF.log.1.db ./dbs/fromEmulator/pF.log.1.db
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 pull /data/data/site.swaraj.jaikisan/databases/pF.log.1.db-shm ./dbs/fromEmulator/pF.log.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 pull /data/data/site.swaraj.jaikisan/databases/pF.log.1.db-wal ./dbs/fromEmulator/pF.log.1.db-wal


C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 pull /data/data/site.swaraj.jaikisan/databases/pF.meta.1.db ./dbs/fromEmulator/pF.meta.1.db
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 pull /data/data/site.swaraj.jaikisan/databases/pF.meta.1.db-shm ./dbs/fromEmulator/pF.meta.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 pull /data/data/site.swaraj.jaikisan/databases/pF.meta.1.db-wal ./dbs/fromEmulator/pF.meta.1.db-wal
