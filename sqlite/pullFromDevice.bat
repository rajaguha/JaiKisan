
REM - transfer SQLite db files from device to fromDevice dir

ECHO Pull from Device
@ECHO off

C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F pull /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.data.1.db ./dbs/fromDevice/pF.data.1.db
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F pull /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.data.1.db-shm ./dbs/fromDevice/pF.data.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F pull /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.data.1.db-wal ./dbs/fromDevice/pF.data.1.db-wal

C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F pull /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.log.1.db ./dbs/fromDevice/pF.log.1.db
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F pull /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.log.1.db-shm ./dbs/fromDevice/pF.log.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F pull /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.log.1.db-wal ./dbs/fromDevice/pF.log.1.db-wal

C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F pull /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.meta.1.db ./dbs/fromDevice/pF.meta.1.db
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F pull /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.meta.1.db-shm ./dbs/fromDevice/pF.meta.1.db-shm
C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F pull /Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases/pF.meta.1.db-wal ./db/fromDevices/pF.meta.1.db-wal
