
.open ../dbs/log.db3

.mode column
.headers on
.nullvalue <NULL>

DROP TABLE IF EXISTS err_log;
DROP TRIGGER IF EXISTS msg_log_del;
DROP TABLE IF EXISTS msg_log;
DROP TABLE IF EXISTS bad_number;

CREATE TABLE bad_number ( 
       cid TEXT PRIMARY KEY NOT NULL
    ,  lastMsg TEXT NOT NULL
    ,  reason TEXT DEFAULT "spam" NOT NULL
    ,  count INTEGER DEFAULT 0 NOT NULL 
    ,  ttl_len INTEGER  DEFAULT 0 NOT NULL
    ,  last_date INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE TABLE msg_log (
       cid CHAR(13) NOT NULL
    ,  in_msg TEXT NOT NULL
    ,  msg_ts INTEGER NOT NULL
    ,  rcv_ts INTEGER NOT NULL
    ,  out_msg TEXT
    ,  snt_ts INTEGER
    ,  dlv_ts INTEGER
    ,  err INTEGER CHECK ( err IN ( 0, 1 ) ) DEFAULT 0 NOT NULL
    ,  _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
CREATE TRIGGER msg_log_del AFTER DELETE ON msg_log FOR EACH ROW BEGIN
  INSERT OR IGNORE INTO bad_number( cid, lastMsg ) VALUES ( OLD.cid, OLD.in_msg ); 
  UPDATE bad_number SET 
     lastMsg = OLD.in_msg
   , reason = 'spam'
   , count = count + 1 
   , ttl_len = ttl_len + LENGTH(OLD.in_msg)
   , last_date = CURRENT_TIMESTAMP 
  WHERE cid = OLD.cid; 
END;
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

.tables
