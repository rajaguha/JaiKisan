
.open tmp/jk.log.1.db

-- START
PRAGMA foreign_keys = ON;
-- STOP
-- START
CREATE TABLE bad_number ( 
       cid TEXT PRIMARY KEY NOT NULL
    ,  last_msg TEXT
    ,  reason TEXT DEFAULT "spam" NOT NULL
    ,  count INTEGER DEFAULT 0
    ,  ttl_len INTEGER DEFAULT 0
    ,  last_date DATETIME NOT NULL
);
-- STOP
-- START
-- TODO - does UPDATE ON UPDATE TRIGGER DO
CREATE TRIGGER bad_number_upd AFTER UPDATE ON bad_number 
    FOR EACH ROW WHEN NEW.count = OLD.count BEGIN
  UPDATE bad_number SET 
      count = count + 1
    , ttl_len = ttl_len + LENGTH(NEW.last_msg) 
  WHERE cid = NEW.cid;
END;
-- STOP 
-- START
CREATE VIEW bad_num AS
  SELECT 
      cid
    , last_msg
    , LENGTH(last_msg) AS len
    , tl_len/count AS avg_len
    , count
    , STRFTIME( '%d/%m/%Y %H:%M:%S', last_date/1000, 'unixepoch' ) 
  FROM bad_number;
-- STOP
-- START
CREATE TABLE msg_log (
       cid CHAR(13) NOT NULL
    ,  in_msg TEXT NOT NULL
    ,  msg_ts INTEGER NOT NULL
    ,  rcv_ts INTEGER NOT NULL
    ,  out_msg TEXT
    ,  enq_ts DATETIME
    ,  snt_ts DATETIME
    ,  dlv_ts DATETIME
    ,  send BOOLEAN CHECK ( send IN ( 0, 1 ) )
    ,  _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
-- STOP
-- START
CREATE TRIGGER msg_log_del AFTER DELETE ON msg_log FOR EACH ROW BEGIN
  INSERT OR IGNORE INTO bad_number( cid ) VALUES ( OLD.cid );
  UPDATE bad_number SET 
      last_msg = OLD.in_msg
    , last_date = OLD.msg_ts 
  WHERE cid = OLD.cid; 
END;
-- STOP
-- START
CREATE TABLE err_log ( 
      class TEXT NOT NULL
    , method TEXT NOT NULL
    , file TEXT NOT NULL
    , line_no INTEGER NOT NULL
    , stack TEXT NOT NULL
    , err_class TEXT NOT NULL
    , err_msg TEXT NOT NULL
    , SQL_Error_Code INTEGER
    , SQL_State TEXT
    , msg_rowid INTEGER REFERENCES msg_log(_id) NOT NULL
);
-- STOP
-- START
CREATE VIEW err_msg AS
  SELECT 
    ml.cid, ml.in_msg, ml.out_msg, 
    el.err_msg, el.err_class, el.class, el.method, el.line_no 
  FROM err_log el
  JOIN msg_log ml ON el.msg_rowid = ml.rowid
; 
-- STOP
/************************
    BSNL sms costs
    --------------
    Rs. count days  Rs/sms  Rs/day  sms/day
    12    130    7   0.092   1.714   18.571
    21    265   15   0.079   1.400   17.667
    22    500   10   0.044   2.200   50.000 <- best per sms rate
    32
    34    385   30   0.088   1.133   12.833 <- best per day rate
    52    860   30   0.060   1.733   28.667
    54
    147  3000   60   0.049   2.450   50.000
************************/
-- START
CREATE TABLE bsnl_sms_rate (
    cost REAL UNIQUE NOT NULL
  , count INTEGER NOT NULL
  , days INTEGER NOT NULL 
);
-- STOP
-- START
INSERT INTO bsnl_sms_rate( cost, count, days ) VALUES
    (  12.00,  130,  7 )
  , (  21.00,  265, 15 )
  , (  22.00,  500, 10 )
  , (  34.00,  385, 30 )
  , (  52.00,  860, 30 )
  , ( 147.00, 3000, 60 )
;
-- STOP
-- START
CREATE TABLE bsnl_sms_recharge (
    amt REAL REFERENCES bsnl_sms_rate(cost) NOT NULL
  , crdtd_at DATETIME NOT NULL  
  , ref_id TEXT UNIQUE NOT NULL
  , cmpltd_at DATE TIME
  , count INTEGER
  , valid_till DATE
  , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
-- STOP
-- START
INSERT INTO bsnl_sms_recharge( amt, crdtd_at, ref_id, cmpltd_at, count, valid_till, _id ) VALUES
  ( 34.00, '25/09/2017 03:35:34 AM', 'PGSM250917806615', '25/09/2017 03:36:34 AM', 358, '24/10/2017', 0 );
-- STOP
-- START
CREATE TRIGGER bsnl_sms_recharge_upd AFTER UPDATE ON bsnl_sms_recharge 
FOR EACH ROW WHEN NEW.count IS NULL BEGIN
  UPDATE bsnl_sms_recharge SET 
      count = (SELECT count FROM bsnl_sms_rate WHERE cost = NEW.amt)
    , valid_till = CURRENT_TIMESTAMP 
        + 1000*60*60*24*(SELECT days FROM bsnl_sms_rate WHERE cost = NEW.amt)
  WHERE _id = NEW._id;
  UPDATE bsnl_sms_recharge SET 
      amt = NEW.amt
    , crdtd_at = NEW.crdtd_at
    , ref_id = "_" + NEW.ref_id
    , cmpltd_at = NEW.cmpltd_at
    , count = count + (SELECT count FROM bsnl_sms_rate WHERE cost = NEW.amt)
    , valid_till = CURRENT_TIMESTAMP 
        + 1000*60*60*24*(SELECT days FROM bsnl_sms_rate WHERE cost = NEW.amt)
  WHERE _id = 0;
END; 
-- STOP

.tables