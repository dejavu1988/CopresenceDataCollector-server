package fi.helsinki.cs.server;

public final class Constants {
	
	// Device status
	public static final int STATUS_WORK = 1;
	public static final int STATUS_BLOCKED = 2;
	public static final int STATUS_SENSOR_OFF = 3;
	public static final int STATUS_NETWORK_OFF = 4;
	public static final int STATUS_SERVER_OFF = 5;
	public static final int STATUS_REG_OFF = 6;
	public static final int STATUS_BIND_OFF = 7;
	public static final int STATUS_PEER_OFF = 8;
	public static final int STATUS_PEER_ON = 9;
	
	// Server address
	public static final String SERVER_INET = "54.229.32.28"; 
    public static final int PORT = 8887;
    
    // Sqlite database
    public static final String DB_LOCAL =  "jdbc:sqlite:index.db";
    public static final String DB_MEM =  "jdbc:sqlite:";

    // C/S msg code    
    public static final int REQ_UUID = 1;
    public static final int ACK_UUID = 2;    
    public static final int REQ_GETQ = 3;
    public static final int ACK_GETQ = 4;
    public static final int REQ_VALQ = 5;
    public static final int ACK_VALQ = 6;
    public static final int REQ_REACH = 7;
    public static final int ACK_REACH = 8;
    public static final int REQ_TASK = 9;
    public static final int ACK_TASK = 10;
    public static final int SEND = 11;
    public static final int REQ_UNBIND = 12;
    public static final int ACK_UNBIND = 13;
    public static final int UPLOAD = 14;
    public static final int EXIT = 15;
}
