
.open dbs/pF.data.1.db

.mode column
.headers on
.nullvalue <NULL>

DROP TABLE IF EXISTS deal;
DROP TABLE IF EXISTS offer;
DROP TABLE IF EXISTS farmer;
DROP TABLE IF EXISTS member;

CREATE TABLE member (
      phone CHAR(13) UNIQUE ON CONFLICT ROLLBACK CHECK ( phone GLOB '+91[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' ) NOT NULL 
    , pin CHAR(6) CHECK ( pin GLOB '[0-9][0-9][0-9][0-9][0-9][0-9]' )
    , lang CHAR(5) CHECK ( lang IN ('bn_bn','bn_EN','hi_hi','hi_EN','en_En') )
    , dob DATE
    , name TEXT
    , aadhaar CHAR(14) UNIQUE ON CONFLICT ROLLBACK CHECK ( aadhaar GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]' )
    , upi TEXT UNIQUE CHECK ( upi LIKE '%_@_%' ) UNIQUE ON CONFLICT ROLLBACK
    , ref CHAR(10) REFERENCES member(phone) DEFERRABLE INITIALLY DEFERRED
    , family INTEGER CHECK ( family > 0 AND family < 25 )
    , acres REAL CHECK ( acres > 0 AND acres < 100 )
    , own BOOLEAN CHECK ( own >= 0 AND own <= 1 )
    , _insert_at INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);

CREATE TABLE farmer (
      type CHAR(5) CHECK ( type IN ('OWNER', 'LEASE', 'LABOR') ) NOT NULL
    , acres REAL
    , family_size INTEGER
    , member_id INTEGER REFERENCES member(_id) ON DELETE CASCADE NOT NULL
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

.schema
