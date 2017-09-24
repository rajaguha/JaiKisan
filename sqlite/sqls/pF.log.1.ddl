
.open dbs/pF.log.1.db

.mode column
.headers on
.nullvalue <NULL>

DROP TABLE IF EXISTS err_log;
DROP TABLE IF EXISTS msg_log;
DROP TABLE IF EXISTS bad_number;

CREATE TABLE bad_number ( 
      phoneNo TEXT PRIMARY KEY NOT NULL
    , count INTEGER DEFAULT 0 NOT NULL
    , avg_msg_len INTEGER DEFAULT 0 NOT NULL
    , last_date INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL
    , reason TEXT
);
CREATE TABLE msg_log ( 
      phoneNo CHAR(10) NOT NULL
    , in_msg TEXT NOT NULL
    , sms_ts INTEGER NOT NULL
    , ins_ts INTEGER NOT NULL
    , out_msg TEXT
    , snt_ts INTEGER
    , dlv_ts INTEGER
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
CREATE TABLE err_log ( 
      class TEXT NOT NULL
    , method TEXT NOT NULL
    , file TEXT NOT NULL
    , line INTEGER NOT NULL
    , stack TEXT NOT NULL
    , err_class TEXT NOT NULL
    , err_msg TEXT NOT NULL
    , SQL_Error_Code INTEGER
    , SQL_State TEXT
    , msg_rowid INTEGER REFERENCES msg_log(rowid) NOT NULL
);

.schema
