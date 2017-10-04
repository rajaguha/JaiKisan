-- START
PRAGMA foreign_keys = ON;
-- STOP
-- START
CREATE TABLE member (
     cid CHAR(13) UNIQUE ON CONFLICT ROLLBACK CHECK ( cid GLOB '+91[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' ) NOT NULL 
   , pin CHAR(6) CHECK ( pin GLOB '[0-9][0-9][0-9][0-9][0-9][0-9]' )
   , lang CHAR(5) CHECK ( lang IN ('bn_bn','bn_EN','hi_hi','hi_EN','en_En') )
   , type TEXT CHECK (type IN ('PRODUCER', 'BUYER') )
   , name TEXT
   , dob DATE
   , ano CHAR(14) UNIQUE ON CONFLICT ROLLBACK CHECK (ano GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]' )
   , upi TEXT UNIQUE CHECK ( upi LIKE '%_@_%' ) UNIQUE ON CONFLICT ROLLBACK
   , p_type CHAR(5) CHECK ( p_type IN ('OWNER', 'LEASE', 'LABOR') )
   , p_acres REAL CHECK ( p_acres > 0 AND p_acres < 100 )
   , p_hh_size INTEGER CHECK ( p_hh_size > 0 AND p_hh_size < 25 )
   , b_type TEXT CHECK ( b_type IN ('SPECULATOR', 'WHOLESALER', 'RETAILER', 'CONSUMER') ) 
   , b_qty REAL 
   , ref CHAR(13) REFERENCES member(cid) DEFERRABLE INITIALLY DEFERRED  NOT NULL
   , flags BYTE NOT NULL
   , state TEXT CHECK ( state IN ('RECORDED', 'ENROLED', 'SUSPENDED', 'CANCELED')) NOT NULL
   , msgC INTEGER NOT NULL
   , last_msg DATE NOT NULL
   , _insert_at INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL
   , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
-- STOP
-- START
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
-- STOP
-- START
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
-- STOP
-- START
CREATE TABLE deal (
      buy_offer_id INTEGER REFERENCES offer(_id) NOT NULL
    , sel_offer_id INTEGER REFERENCES offer(_id) NOT NULL
    , quantity REAL NOT NULL
    , price REAL NOT NULL
    , distance REAL NOT NULL
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
-- STOP
