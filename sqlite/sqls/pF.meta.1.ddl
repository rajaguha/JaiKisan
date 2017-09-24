
/****** 
    Get PINCODE distances, locations 
        http://alldistancebetween.com/?calc=true
        https://www.mapdevelopers.com/

    Copy dbs to device
        C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push local remote
        C:\Users\rajag\Devel\sqlite\pF>C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push dbs/xxx.db /Removable/MicroSD/Android/data/site.swaraj.pricefinder/databases/xxx.db

    Manipulate dbs on device
        C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 shell
        root@android:/ # sqlite3 /data/data/site.swaraj.pricefinder/databases/SmsServer.db

******/

.open dbs/pF.meta.1.db

.mode column
.headers on
.nullvalue <NULL>
.echo on

PRAGMA foreign_keys = ON;

----------------------------------------------------------------
DROP VIEW std_name;
DROP VIEW conv;

DROP VIEW sub_item;
DROP VIEW item;
DROP VIEW quality;
DROP VIEW unit;

DROP TABLE translat;

DROP TABLE qual_alias;
DROP TABLE qual_std;

DROP TABLE sub_item_alias;
DROP TABLE sub_item_std;

DROP TABLE item_alias;
DROP TABLE item_std;

DROP TABLE unit_alias;
DROP TABLE unit_std;
DROP TABLE unit_si_std;

DROP TABLE pin2lang;
----------------------------------------------------------------
CREATE TABLE pin2lang (
      lang CHAR(5) NOT NULL
    , area TEXT UNIQUE NOT NULL  
    , pin_from CHAR(6) UNIQUE NOT NULL
    , pin_to CHAR(6) UNIQUE NOT NULL
    , CONSTRAINT pin2lang_chk CHECK ( pin_from < pin_to )
);
INSERT INTO pin2lang ( lang, area, pin_from, pin_to ) VALUES
      ( 'en_En', 'Delhi', '110001', '110097' )
    , ( 'hi_EN', 'Haryana,Punjab,HP', '121001', '179999' )
    , ( 'en_En', 'J&K', '180001', '199999' )
    , ( 'hi_EN', 'UP,Uttarakhand,Rajasthan,Gujrat', '201001', '399999' )
    , ( 'en_En', 'Mumbai', '400001', '400199' )
    , ( 'hi_EN', 'Maharashtra,MP,Chattisgarh', '400601', '499999' )
    , ( 'en_En', 'AP,Karnatak,Tamilnadu,Kerala', '500001', '699999' )
    , ( 'en_En', 'Calcutta', '700001', '700199' )
    , ( 'bn_EN', 'Bangla', '700201', '749999' )
    , ( 'hi_EN', 'Odisha', '752001', '779999' )
    , ( 'hi_EN', 'Assam', '781001', '789999' )
    , ( 'en_En', 'North East', '790001', '799299' )
    , ( 'hi_EN', 'Bihar,Jharkhand', '800001', '899999' )
    , ( 'en_En', 'TESTING-en_En', '999100', '999199' )
    , ( 'bn_EN', 'TESTING-bn_EN', '999200', '999299' )
    , ( 'bn_bn', 'TESTING-bn_bn', '999300', '999399' )
    , ( 'hi_EN', 'TESTING-hi_EN', '999400', '999499' )
    , ( 'hi_hi', 'TESTING-hi_hi', '999501', '999599' )
;
----------------------------------------------------------------
CREATE TABLE unit_si_std (
      name TEXT UNIQUE NOT NULL
    , abbrev CHAR(2) UNIQUE NOT NULL
    , type TEXT UNIQUE NOT NULL
    , _id INTEGER PRIMARY KEY NOT NULL
);
CREATE TABLE unit_std (
      name TEXT UNIQUE NOT NULL
    , abbrev CHAR(2) UNIQUE NOT NULL
    , to_si REAL NOT NULL
    , si_std TEXT REFERENCES unit_si_std(abbrev) NOT NULL
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
CREATE TABLE unit_alias (
      alias TEXT UNIQUE NOT NULL
    , std TEXT REFERENCES unit_std(abbrev) NOT NULL
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
INSERT INTO unit_si_std (name, abbrev, type, _id ) VALUES 
      ( 'Kilogram',  'kg', 'weight', 1 )
    , ( 'Litre', 'l', 'volume', 2 )
    , ( 'Meter', 'm', 'length', 4 )
    , ( 'Square Meter', 'm2', 'area', 3 )
    , ( 'Piece', 'pc', 'piece', 5 )
    , ( 'Rupee', 'Rs.', 'currency', 6 )
;
INSERT INTO unit_std (name, abbrev, to_si, si_std ) VALUES 
      ( 'Kilo', 'kg', 1.0, 'kg' )
    , ( 'Gram', 'gm', 0.001, 'kg' )
    , ( 'Quintal', 'Qt', 100.0, 'kg' )
    , ( 'Tonne', 'MT', 1000.0, 'kg' )

    , ( 'Liter', 'l', 1.0, 'l' )
    , ( 'Milliliter', 'ml', 0.001, 'l' )

    , ( 'Square Meter', 'm2', 1.0, 'm2' )
    , ( 'Bigha', 'bg', 1337.80378, 'm2' )
    , ( 'Acre', 'AC', 4046.85642, 'm2' )
    , ( 'Hectare', 'Ht', 10000.0, 'm2' )

    , ( 'Meter', 'm', 1.0, 'm' )
    , ( 'Millimeter', 'mm', 0.001, 'm' )

    , ( 'Piece', 'pc', 1.0, 'pc' )
    , ( 'Dozen', 'dz', 12, 'pc' )

    , ( 'Rupee', 'Rs.', 1.0, 'Rs.' )
;
INSERT INTO unit_alias (alias, std ) VALUES 
     ( 'Q', 'Qt' )
    , ( 'क्विंटल', 'Qt' )
    , ( 'क्वि', 'Qt' )
    , ( 'क्व', 'Qt' )
    , ( 'কুইন্টাল', 'Qt' )
    , ( 'কু', 'Qt' )

    , ( 'Metric', 'MT' )
    , ( 'Ton', 'MT' )
    , ( 'Tonne', 'MT' )
    -- , ( 'M', 1000, 'Kilo' )  -- M is Meter
    , ( 'মেট্রিক টন', 'MT' )
    , ( 'মে', 'MT' )
    , ( 'ম', 'MT' )
    , ( 'ট', 'MT' )
    , ( 'মট', 'MT' )
    , ( 'মেট্রিক', 'MT' )
    , ( 'টন', 'MT' )
    , ( 'म', 'MT' )
    , ( 'मे', 'MT' )
    , ( 'मीट्रिक टन', 'MT' )
    , ( 'मीट्रिक', 'MT' )
    , ( 'टन', 'MT' )
    , ( 'ट', 'MT' )
    , ( 'मट', 'MT' )

    , ( 'Kilogram', 'kg' )
    , ( 'K', 'kg' )
    , ( 'किलो', 'kg' )
    , ( 'कि', 'kg' )
    , ( 'क', 'kg' )
    , ( 'কিলো', 'kg' )
    , ( 'কি', 'kg' )
    , ( 'ক', 'kg' )

    , ( 'A', 'AC' )
    , ( 'एकर', 'AC' )
    , ( 'ए', 'AC' )
    , ( 'একর', 'AC' )
    , ( 'এ', 'AC' )

    , ( 'B', 'bg' )
    , ( 'बीघा', 'bg' )
    , ( 'बी', 'bg' )
    , ( 'ब', 'bg' )
    , ( 'বিঘা', 'bg' )
    , ( 'বি', 'bg' )
    , ( 'ব', 'bg' )

    , ( 'H', 'Ht' )
    , ( 'हेक्टेयर', 'Ht' )
    , ( 'हे', 'Ht' )
    , ( 'ह', 'Ht' )
    , ( 'হেক্টার', 'Ht' )
    , ( 'হে', 'Ht' )
    , ( 'হ', 'Ht' )

    , ( '₹', 'Rs.' )
    , ( 'Rs', 'Rs.' )
    , ( 'R', 'Rs.' )
    , ( 'र', 'Rs.' )
    , ( 'रु', 'Rs.' )
    , ( 'র', 'Rs.' )
    , ( 'রু', 'Rs.' )
    -- , ( 'ট', 'Rs.' ) --   -- ট -> টন
    , ( 'টা', 'Rs.' )
    , ( '#', 'Rs.' )
    , ( 'টাকা', 'Rs.' )
    , ( 'রুপি', 'Rs.' )
    , ( 'रुपिया', 'Rs.' )
;
----------------------------------------------------------------
CREATE TABLE item_std (
      name TEXT UNIQUE NOT NULL
    , dsc TEXT
    , unit TEXT REFERENCES unit_std(name) 
    , _id INTEGER PRIMARY KEY NOT NULL
);
CREATE TABLE item_alias (
      alias TEXT UNIQUE NOT NULL
    , std TEXT REFERENCES item_std(name) NOT NULL
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
INSERT INTO item_std (name, dsc, unit, _id ) VALUES 
      ( 'Potato', 'Vatata', 'Quintal',  1 )
    , ( 'Paddy', 'Rice paddy', 'Tonne',  2 )
    , ( 'Rice', 'Husked rice', 'Kilo',  3 )
    , ( 'Land', 'Agricultural acreage', 'Acre', 4 )
;
INSERT INTO item_alias (alias, std ) VALUES 
      ( 'Alu', 'Potato' )
    , ( 'Aloo', 'Potato' )
    , ( 'Dhan', 'Paddy' )
    , ( 'Chaawal', 'Rice' )
    , ( 'Jomi', 'Land' )
    , ( 'Chaal', 'Rice' )
    , ( 'Zameen', 'Land' )
;
----------------------------------------------------------------
CREATE TABLE sub_item_std (
      name TEXT NOT NULL
    , dsc TEXT
    , std_item TEXT REFERENCES item_std(name) ON DELETE CASCADE NOT NULL
    , _id INTEGER PRIMARY KEY NOT NULL
    , CONSTRAINT sub_item_std_unq UNIQUE ( std_item, name ) ON CONFLICT ROLLBACK
);
CREATE TABLE sub_item_alias (
      alias TEXT NOT NULL
    , std_item TEXT NOT NULL -- REFERENCES item(name) ON DELETE CASCADE NOT NULL
    , std TEXT NOT NULL -- REFERENCES sub_item_std(name) ON DELETE CASCADE NOT NULL
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
    , CONSTRAINT sub_item_alias_fk FOREIGN KEY (std_item, std) REFERENCES sub_item_std(std_item, name) --  ON CONFLICT ROLLBACK
    , CONSTRAINT sub_item_alias_unq UNIQUE (std_item, alias) ON CONFLICT ROLLBACK
);
INSERT INTO sub_item_std (name, dsc, std_item, _id ) VALUES 
      ( 'Jyoti', 'Medim priced', 'Potato', 1 )
    , ( 'Chandramukhi', 'Medim priced', 'Potato', 2 )
    , ( 'Basmati', 'Expensive', 'Rice', 3 )
    , ( '3-Crop', '3 crops per year', 'Land', 4 )
    , ( '2-Crop', '2 crops per year', 'Land', 5 )
;
INSERT INTO sub_item_alias (alias, std_item, std ) VALUES 
      ( 'Jooti', 'Potato', 'Jyoti' )
    , ( 'Bashmati', 'Rice', 'Basmati' )
    , ( '3_PHOSH-LA', 'Land', '3-Crop' )
;
----------------------------------------------------------------
CREATE TABLE qual_std (
      name TEXT UNIQUE NOT NULL
    , dsc TEXT  
    , std_item TEXT NULL REFERENCES item_std(name) ON DELETE CASCADE
    , std_sub_item TEXT NULL -- REFERENCES sub_item_std(name) ON DELETE CASCADE
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
    , CONSTRAINT qual_std_fk FOREIGN KEY (std_item, std_sub_item) REFERENCES sub_item_std(std_item, name)
    , CONSTRAINT qual_std_unq UNIQUE ( std_item, std_sub_item, name ) ON CONFLICT ROLLBACK
);
CREATE TABLE qual_alias (
      alias TEXT UNIQUE NOT NULL
    , std TEXT REFERENCES qual_std(name) NOT NULL
    , std_item TEXT NULL REFERENCES item(name) ON DELETE CASCADE
    , std_sub_item TEXT NULL REFERENCES sub_item(name) ON DELETE CASCADE
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
    , CONSTRAINT qual_alias_fk FOREIGN KEY (std_item, std_sub_item) REFERENCES sub_item_std(std_item, name)
    , CONSTRAINT qual_alias_unq UNIQUE ( std_item, std_sub_item, alias ) ON CONFLICT ROLLBACK
);
PRAGMA foreign_keys = OFF;
INSERT INTO qual_std (name, dsc, std_item, std_sub_item, _id) VALUES 
      ( 'Highest', NULL, NULL, NULL, 1 )
    , ( 'High', NULL, NULL, NULL, 2 )
    , ( 'Standard', NULL, NULL, NULL, 3 )
    , ( 'Low', NULL, NULL, NULL, 4 )

    , ( 'Lowest', NULL, NULL, NULL, 5 )
    , ( 'Prime', NULL, 'Potato', NULL, 6 )
    , ( 'Cuts', NULL, 'Potato', NULL, 7 )
    , ( 'Old', NULL, 'Potato', NULL, 8 )
    , ( 'Extra-Long-Grain', NULL, 'Rice', 'Basmati', 9 )
    , ( 'Medium-Grain', NULL, 'Rice', 'Basmati', 10 )
    , ( 'Broken', NULL, 'Rice', 'Basmati', 11 )
;
INSERT INTO qual_alias ( alias, std_item, std_sub_item, std ) VALUES 
      ( 'UUCHA', NULL, NULL, 'High' )
    , ( 'SA-DHA-RN', NULL, NULL, 'Standard' )
    , ( 'Grade-A', 'Potato', NULL, 'Prime' )
    , ( 'Wholesome', 'Potato', NULL, 'Prime' )
    , ( 'Bruised', 'Potato', NULL, 'Cuts' )
    , ( 'BHAN-GA', 'Rice', 'Basmati', 'Broken' )
    , ( 'TOO-TA', 'Rice', 'Basmati', 'Broken' )
    , ( 'PHOO_TA', 'Rice', 'Basmati', 'Broken' )
;
PRAGMA foreign_keys = ON;
----------------------------------------------------------------
CREATE TABLE translat (
      en_En TEXT UNIQUE NOT NULL
    , bn_EN TEXT UNIQUE
    , bn_bn TEXT UNIQUE
    , hi_EN TEXT UNIQUE
    , hi_hi TEXT UNIQUE
    , _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
);
INSERT INTO translat ( en_En, bn_EN, bn_bn, hi_EN, hi_hi ) VALUES
('Extra-Long-Grain','ATI-LOMBIT_DANA','অতি-লম্বা-দানা','ATI-LUMBIT_DANE','अति-लुम्बित-दाना')
,( 'Medium-Grain',    'MAJHARI_DANA',    'মাঝারি-দানা',    'BICH-KA-DANA',   'बिच-का-दाना')
,( 'Old',              'PURANO',           'পুরনো',        'PURANA',         'पुराना' )
,( 'Prime',            'PROTHOM',          'প্রথম',         'PEHELA',         'पहेला' )
,( 'Broken',           'BHAN-GA',          'ভাঙা',         'TOO-TA',         'टूटा' )
,( 'Cuts',             'CATA',             'কাটা',         'CUTA',           'कटा' )
,( 'Buy',              'KENA',             'কেনা',         'LENA',           'लेना' )
,( 'Sell',             'BECHA',            'বেচা',          'BECHNA',        'बेचना' )
,( 'Own',              'NI-JER',           'নিজের',         'APP-NA',        'अप्ना' )
,( 'Rent',             'BHA-RA',           'ভাড়া',          'BHA-RA',        'भारा' )
,( 'English',          'BANGLA',           'বাংলা',         'HINDI',         'हिन्दी' )
,( 'High',             'UCHU',             'ঊচু',            'UCHA',          'ऊचा' )
,( 'Highest',          'SHOB-CHE_UCHU',    'সবচে_ঊচু',       'SUB-SE_UCHA',   'सबसे_ऊचा' )
,( 'Low',              'NICHU',            'নিচু',           'NICHA',         'निचा' )
,( 'Standard',         'SHA-DHA-RON',      'সাধারণ',         'SA-DHA-RAN',    'साधारण' )
,( 'Lowest',           'SHOB-CHE_NICHU',   'সবচে_নিচু',      'SUB-SE_NICHA',  'सबसे_निचा' )
,( 'Acre',             'E-KAR',            'একর',            'E-KAR',         'एकर' )
,( 'Bigha',            'BI-GHA',           'বিঘা',           'BI-GHA',        'बीघा' )
,( 'Dozen',            'DOJON',            'ডজন',            'DAR-ZAN',       'दर्ज़ां' )
,( 'Gram',             'GRAM',             'গ্রাম',           'GRAM',          'ग्राम' )
,( 'Hectare',          'HEC-TAR',          'হেক্টার',         'HEC-TAR',       'हेक्टेयर' )
,( 'Kilo',             'KILO',             'কিলো',           'KILO',          'किलो' )
,( 'Liter',            'LI-TAR',           'লিটার',           'LI-TAR',        'लीटर' )
,( 'Meter',            'MI-TAR',           'মিটার',           'MI-TAR',        'मीटर' )
,( 'Milliliter',       'MILI-LI-TAR',      'মিলিলিটার',       'MILI-LI-TAR',   'मिलिलीटर' )
,( 'Millimeter',       'MILI-MI-TAR',      'মিলিমিটার',       'MILI-MI-TAR',   'मिलिमीटर' )
,( 'Piece',            'TA',               'টা',              'THO',           'ठो' )
,( 'Quintal',          'KUIN-TAL',         'কুইন্টাল',         'KUIN-TAL',      'क्विंटल' )
,( 'Tonne',             'TON',              'টন',               'TAN',          'टन' )
,( 'Metric',           'METRIC',           'মেট্রিক',          'METRIC',        'मीट्रिक' )
,( 'Land',             'JOMI',             'জোমি',            'ZAM-EEN',       'ज़मीन' )
,( 'Paddy',            'DHAN',             'ধান',              'DHAAN',         'धान' )
,( 'Potato',           'AALU',             'আলু',             'AA-LU',         'आलू' )
,( 'Square Meter',     'BORGO MI-TAR',     'বর্গ মিটার',       'BURGA MI-TAR',   'बर्ग-मीटर')
,( 'Rice',             'CHAAL',            'চাল',             'CHAA-WAL',       'चावल' )
,( '2-Crop',           'DUI-PHOSHLA',      'দুই-ফস্লা',        'DO-PHASAL',      'दो-फसल' )
,( '3-Crop',           'TEEN-PHOSHLA',     'তিন-ফস্লা',       'TEEN-PHASAL',    'थीं-फसल' )
,( 'Chandramukhi',     'CHNDRA-MUKHI',     'চন্দ্রমুখী',        'CHNDRA-MUKHI', 'चंद्रमुखी' )
,( 'Jyoti',            'JOTI',             'জ্যোতি',          'JYO-TI',         'ज्योति' )
,( 'Basmati',          'BASHMATI',         'বাশমতি',           'BAS-MATI',       'बासमति' )
,( 'Rupee',            'TAKA',             'টাকা',             'RUP-YAH',       'रुपयाः' )
; 
INSERT INTO translat ( en_En ) SELECT name FROM unit_std  src
        WHERE NOT EXISTS (SELECT en_En FROM translat t WHERE t.en_En = src.name);
INSERT INTO translat ( en_En ) SELECT name FROM item_std src
        WHERE NOT EXISTS (SELECT en_En FROM translat t WHERE t.en_En = src.name);
INSERT INTO translat ( en_En ) SELECT name FROM sub_item_std src
        WHERE NOT EXISTS (SELECT en_En FROM translat t WHERE t.en_En = src.name);
INSERT INTO translat ( en_En ) SELECT name FROM qual_std src
        WHERE NOT EXISTS (SELECT en_En FROM translat t WHERE t.en_En = src.name);

----------------------------------------------------------------
CREATE VIEW unit AS
    SELECT us.name AS std, us.abbrev, ua.alias, us.to_si AS conv, us.si_std 
    FROM ( SELECT std, alias FROM unit_alias
        UNION SELECT abbrev AS std, name AS alias FROM unit_std
        UNION SELECT abbrev AS std, abbrev AS alias FROM unit_std
    ) AS ua
    JOIN unit_std us ON us.abbrev = ua.std
    ORDER BY std, alias
;
CREATE VIEW quality AS 
    SELECT 
          qa.std_item
        , qa.std_sub_item
        , qa.std
        , qa.alias
        , qs.dsc
        , qs._id AS sid
        , qa._id AS aid
        FROM ( SELECT std_item, std_sub_item, std, alias, _id+2000 AS _id 
            FROM qual_alias
        UNION SELECT std_item, std_sub_item, name AS std
            , CAST(_id AS TEXT) AS alias, _id+1000 AS _id FROM qual_std
        UNION SELECT std_item, std_sub_item, name AS std, name AS alias, _id 
            FROM qual_std
    ) AS qa
    JOIN qual_std qs ON ( qs.std_item IS NULL OR qs.std_item = qa.std_item )
        AND ( qs.std_sub_item IS NULL OR qs.std_sub_item = qs.std_sub_item )
        AND ( qs.name IS NULL OR qs.name = qa.std )
;
CREATE VIEW item AS
    SELECT ia.std
        , ia.alias
        , q.std AS qs
        , q.alias AS qa
        , q.sid AS qid
        , _is.unit
        , _is.dsc
        , _is._id AS sid
        , ia._id AS aid 
    FROM ( SELECT alias, std, _id+2000 AS _id FROM item_alias 
        UNION SELECT CAST(_id AS TEXT) AS alias, name AS std, _id+1000 AS _id 
        FROM item_std 
        UNION SELECT name AS alias, name AS std, _id FROM item_std 
    ) AS ia
    JOIN item_std _is ON _is.name = ia.std
    LEFT JOIN quality q ON q.std_item = ia.std AND q.std_sub_item IS NULL
;
CREATE VIEW sub_item AS
    SELECT sia.std_item
        , sia.std
        , sia.alias
        , q.std AS qs
        , q.alias AS qa
        , q.sid AS qid
        , sis.dsc
        , sis._id AS sid
        , sia._id AS aid 
    FROM ( SELECT std_item, std, alias, _id+2000 AS _id FROM sub_item_alias
        UNION SELECT std_item, name AS std
        , CAST(_id AS TEXT) AS alias, _id+1000 AS _id FROM sub_item_std
        UNION SELECT std_item, name AS std, name AS alias, _id FROM sub_item_std
    ) AS sia
    JOIN sub_item_std sis ON sis.std_item = sia.std_item AND sis.name = sia.std
    LEFT JOIN quality q ON q.std_item = sia.std_item AND q.std_sub_item = sia.std
;
-- CREATE VIEW std_name AS 
--     SELECT 
--           su.alias AS src_unit
--         , su.std AS from_unit
--         , du.std AS to_unit
--         , du.conv/su.conv AS conv

--         , i.alias AS alias_main
--         , si.alias AS alias_sub
--         , COALESCE(si.qa, i.qa, q.alias) AS alias_qual
--         , i.alias||'#'||si.alias||'#'||COALESCE(si.qa, i.qa, q.alias) AS alias

--         , i.std AS std_main
--         , si.std AS std_sub
--         , COALESCE(si.qs, i.qs, q.std) AS std_qual
--         , i.std||'#'||si.std||'#'||COALESCE(si.qs, i.qs, q.std) AS std

--         , i.sid AS sid_main
--         , si.sid AS sid_sub
--         , COALESCE(si.qid, i.qid, q.sid) AS sid_qual
--         , i.sid||'#'||si.sid||'#'||COALESCE(si.qid, i.qid, q.sid) AS sid

--         , i.std||'#'||si.std||'#'||COALESCE(si.qs, i.qs, q.std)||'('||
--             i.sid||'#'||si.sid||'#'||COALESCE(si.qid, i.qid, q.sid)||')' AS display
        
--     FROM item AS i
--     LEFT JOIN unit AS du ON du.alias = i.unit
--     LEFT JOIN unit AS su ON su.si_std = du.si_std
--     LEFT JOIN sub_item AS si ON si.std_item = i.std
--     LEFT JOIN quality AS q ON i.qa IS NULL AND si.qa IS NULL AND q.std_item IS NULL
-- ;
CREATE VIEW conv AS
    SELECT
          s.alias AS src_alias
        , s.std AS src_std
        , d.alias AS dst_alias
        , d.std AS dst_std
        , s.conv/d.conv AS conv
    FROM unit AS d
    LEFT JOIN unit AS s ON s.si_std = d.si_std
;
CREATE VIEW std_name AS 
    SELECT 
          c.src_alias AS src_unit
        , c.src_std AS from_unit
        , c.dst_std AS to_unit
        , c.conv AS conv

        , i.alias AS alias_main
        , si.alias AS alias_sub
        , COALESCE(si.qa, i.qa, q.alias) AS alias_qual
        , i.alias||'#'||si.alias||'#'||COALESCE(si.qa, i.qa, q.alias) AS alias

        , i.std AS std_main
        , si.std AS std_sub
        , COALESCE(si.qs, i.qs, q.std) AS std_qual
        , i.std||'#'||si.std||'#'||COALESCE(si.qs, i.qs, q.std) AS std

        , i.sid AS sid_main
        , si.sid AS sid_sub
        , COALESCE(si.qid, i.qid, q.sid) AS sid_qual
        , i.sid||'#'||si.sid||'#'||COALESCE(si.qid, i.qid, q.sid) AS sid

        , i.std||'#'||si.std||'#'||COALESCE(si.qs, i.qs, q.std)||'('||
            i.sid||'#'||si.sid||'#'||COALESCE(si.qid, i.qid, q.sid)||')' AS display
        
    FROM item AS i
    LEFT JOIN conv AS c ON c.dst_alias = i.unit
    LEFT JOIN sub_item AS si ON si.std_item = i.std
    LEFT JOIN quality AS q ON i.qa IS NULL AND si.qa IS NULL AND q.std_item IS NULL
;
----------------------------------------------------------------
----------------------------------------------------------------
.echo off
.w 15 15 15 15 10
SELECT * FROM conv WHERE src_alias = 'bg' AND dst_alias = 'H';
.w 30 40 5 5 5 5
SELECT alias, display, src_unit, from_unit, to_unit, conv 
    FROM std_name WHERE alias LIKE 'Jomi#3_PHOSH-LA%' AND src_unit = 'bg';
SELECT alias, display, src_unit, from_unit, to_unit, conv 
    FROM std_name WHERE alias LIKE 'Aloo#Jooti#%' AND src_unit = 'kg';
SELECT alias, display, src_unit, from_unit, to_unit, conv 
    FROM std_name WHERE alias LIKE 'Rice#Basmati#%' AND src_unit = 'Qt';
SELECT alias, display, src_unit, from_unit, to_unit, conv 
    FROM std_name WHERE alias LIKE 'Chaal#Bashmati#BHAN-GA' AND src_unit = 'Qt';
