
REM - transfer SQLite db files fromEmulator dir to ASUS device

ECHO Push to Device
@ECHO off

C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push ./dbs/fromEmulator/pF.data.1.db /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.data.1.db
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push ./dbs/fromEmulator/pF.data.1.db-shm /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.data.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push ./dbs/fromEmulator/pF.data.1.db-wal /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.data.1.db-wal

C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push ./dbs/fromEmulator/pF.log.1.db /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.log.1.db
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push ./dbs/fromEmulator/pF.log.1.db-shm /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.log.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push ./dbs/fromEmulator/pF.log.1.db-wal /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.log.1.db-wal

C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push ./dbs/fromEmulator/pF.meta.1.db /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.meta.1.db
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push ./dbs/fromEmulator/pF.meta.1.db-shm /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.meta.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push ./dbs/fromEmulator/pF.meta.1.db-wal /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.meta.1.db-wal
