
.open ../dbs/data.db3

.mode column
.headers on
.nullvalue <NULL>

DROP TABLE IF EXISTS deal;
DROP TABLE IF EXISTS offer;
DROP TABLE IF EXISTS query;
DROP TABLE IF EXISTS buyer;
DROP TABLE IF EXISTS producer;
DROP TABLE IF EXISTS member;

CREATE TABLE member (
     cid CHAR(13) UNIQUE ON CONFLICT ROLLBACK CHECK ( cid GLOB '+91[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' ) NOT NULL 
   , pin CHAR(6) CHECK ( pin GLOB '[0-9][0-9][0-9][0-9][0-9][0-9]' )
   , lang CHAR(5) CHECK ( lang IN ('bn_bn','bn_EN','hi_hi','hi_EN','en_En') )
   , type TEXT CHECK (type IN ('PRODUCER', 'BUYER') )  NOT NULL
   , name TEXT
   , dob DATE
   , ano CHAR(14) UNIQUE ON CONFLICT ROLLBACK CHECK (ano GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]' )
   , upi TEXT UNIQUE CHECK ( upi LIKE '%_@_%' ) UNIQUE ON CONFLICT ROLLBACK
   , flags BYTE 
   , ref CHAR(10) REFERENCES member(phone) DEFERRABLE INITIALLY DEFERRED
   , state TEXT CHECK ( state IN ('RECD', 'ENRLD', 'SSPND', 'CANCEL')) DEFAULT 'RECD' NOT NULL
   , msg_count INTEGER NOT NULL
   , last_msg DATE NOT NULL
   , _insert_at INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL
   , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
CREATE TABLE producer (
     type CHAR(5) CHECK ( type IN ('OWNER', 'LEASE', 'LABOR') ) NOT NULL
   , acres REAL CHECK ( acres > 0 AND acres < 100 )
   , hh_size INTEGER CHECK ( hh_size > 0 AND hh_size < 25 )
   , mmbr_id INTEGER REFERENCES member(_id) ON DELETE CASCADE NOT NULL
   , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
CREATE TABLE buyer (
     qty REAL NOT NULL
   , type TEXT CHECK (type IN ('SPECULATOR', 'WHOLESALER', 'RETAILER', 'CONSUMER') ) 
   , mmbr_id INTEGER REFERENCES member(_id) ON DELETE CASCADE NOT NULL
   , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
CREATE TABLE query (
     member_id INTEGER REFERENCES member(_id) NOT NULL 
   , type CHAR(3) CHECK ( type IN ('BUY', 'SEL') ) NOT NULL
   , raw_item TEXT NOT NULL 
   , std_item TEXT NOT NULL 
   , delivery_date DATE NOT NULL 
   , delivery_pin CHAR(6) CHECK (delivery_pin GLOB '[0-9][0-9][0-9][0-9][0-9][0-9]') NOT NULL
   , quantity REAL NOT NULL
   , acres REAL
   , _insert_at INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL
   , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
CREATE TABLE offer (
      member_id INTEGER REFERENCES member(_id) NOT NULL
    , type CHAR(3) CHECK ( type IN ('BUY', 'SEL') ) NOT NULL
    , sub_category_id INTEGER REFERENCES sub_category(_id) NOT NULL
    , delivery_date DATE NOT NULL
    , delivery_pin CHAR(6) CHECK (delivery_pin GLOB '[0-9][0-9][0-9][0-9][0-9][0-9]') NOT NULL
    , quantity REAL NOT NULL
    , price REAL NOT NULL
    , min_qty REAL NOT NULL
    , max_tail_qty REAL NOT NULL
    , booked_qty REAL DEFAULT 0.0 NOT NULL
    , _insert_at INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
CREATE TABLE deal (
      buy_offer_id INTEGER REFERENCES offer(_id) NOT NULL
    , sel_offer_id INTEGER REFERENCES offer(_id) NOT NULL
    , quantity REAL NOT NULL
    , price REAL NOT NULL
    , distance REAL NOT NULL
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);

.tables
