# README #

This README would normally document whatever steps are necessary to get your server up and running.

### What is this repository for? ###

* This is the repository for [CoCo project](http://www.se-sy.org/projects/coco/) - data collection framework - server.
* Version: v0.3a2
* Currently a server instance is hosted on http://colocation.minidns.net/dc/.

### How do I get set up? ###

* Server
    - testserver.v0.3a2.jar: the daemon executable handling connections.
    - upload4.php: the script to handle data uploading and filtering.
    - uploadlog4.php: the script to handle user log uploading.
    - dbsqlite.sql: the SQL sentences to setup Sqlite database.
* Configuration
    - All parameters are configured in `Constants.java`: `SERVER_INET` and `SERVER_PORT` for server address, and `DB_LOCAL` for database path.
* Dependencies (`libs/`)
    - JavaSE 6 environment
    - Apache server: httpd, php3, sqlite3
    - [Gson](https://code.google.com/p/google-gson/)
    - [Apache Commons libs](https://commons.apache.org/): commons-codec, commons-math
    - [Sqlite-JDBC](https://bitbucket.org/xerial/sqlite-jdbc/overview)
    - [JTransforms](https://sites.google.com/site/piotrwendykier/software/jtransforms)
    - [musicg](https://code.google.com/p/musicg/) 
* Database configuration
    - `index.db`: the Sqlite3 database file by default.
    - Follow `dbsqlite.sql` to create empty tables (Device, Bind, Observ).
* Deployment instructions
    0. [daemontools](http://cr.yp.to/daemontools.html) is used to make the jar a persistent daemon service. For details, refer to `daemon_setup.txt`.
    1. Before setting up the server, configure constants(e.g., server address) in server source code `Constants.java`, and export source into jar.
    2. Make a directory for server(e.g. `/dcserver`), and put all server jar and php files in this dir. Grant executable permissions to dir, jar and php files.
    3. Build a new sqlite database in server dir by `sqlite3 index.db`, and follow `dbsqlite.sql` to make tables.
    4. Run in background like `java -server -jar testserver.v0.3a2.jar`, or wrap jar into a service (recommend daemontools in http://cr.yp.to/daemontools.html for long-running service).

### Who do I talk to? ###

* Repo owner or admin: Xiang Gao (rekygx@gmail.com)
* Team contact: Hien Truong (hien.truong@cs.helsinki.fi or truongthithuhien@gmail.com)