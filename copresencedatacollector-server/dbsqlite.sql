/*use database 'test'*/
sqlite3 index.db;
BEGIN TRANSACTION;

/*Device - table for device registration: UUID - key, NAME - customized name of device*/
CREATE TABLE IF NOT EXISTS Device (
Uuid TEXT NOT NULL PRIMARY KEY CHECK(Uuid!=''), 
Name TEXT NOT NULL DEFAULT '', 
Addr TEXT NOT NULL DEFAULT '',
Port INTEGER NOT NULL DEFAULT -1,
Ver TEXT NOT NULL DEFAULT '');

/*Bind - table for binding device registration: UUID1/UUID2 - keys, QNum - queue number for binding*/
CREATE TABLE IF NOT EXISTS Bind (
Uuid1 TEXT NOT NULL CHECK(Uuid1!=''), 
Uuid2 TEXT NOT NULL DEFAULT '' CHECK(Uuid2!=Uuid1), 
QNum INTEGER NOT NULL DEFAULT 0,
PRIMARY KEY(Uuid1, Uuid2));

/*Observ - table for observation list: UUID1/UUID2 - keys, TS_S - standard timestamp of starting point on Server-side, delta-T after which to start data collection on both clients, TS_D - time difference between 2 clients,  MS - set of common modality type names, GT - ground truth for colocation*/

CREATE TABLE IF NOT EXISTS Observ (
Id INTEGER PRIMARY KEY,
Uuid1 TEXT NOT NULL DEFAULT '', 
Uuid2 TEXT NOT NULL DEFAULT '', 
TS_S INTEGER NOT NULL UNIQUE DEFAULT 0,
TS_D INTEGER NOT NULL DEFAULT 0,
MS INTEGER NOT NULL DEFAULT 7,
GT INTEGER NOT NULL DEFAULT 0 );
COMMIT;
