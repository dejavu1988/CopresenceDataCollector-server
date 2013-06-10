package fi.helsinki.cs.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHelper {
	private static final String DB_NAME =  "inddex.db";
	private static final String DB_MEM =  "jdbc:sqlite:";
	private Connection connection;
	private Statement statement;
	
	public DBHelper() throws ClassNotFoundException{
		Class.forName("org.sqlite.JDBC");
		this.connection = null;
	}
	
	public void prepare() throws SQLException{
		connection = DriverManager.getConnection(DB_MEM);
		statement = connection.createStatement();
	}
	
	public void startToMem(){
		try{			
			statement.setQueryTimeout(15);
			statement.executeUpdate("restore from backup.db");
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}		
	}
	
	public void backupFromMem(){
		try{
			statement.executeUpdate("backup to backup.db");
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	public void registerDevice(Device device) throws SQLException{
		try{
			if(device.isPrepared()){
				String sql = "insert into Device(Uuid, Name, Addr, Port) values(, '" + device.getUuid() + "', '" + device.getName()
					+ "', '" + device.getIpAddress().getHostAddress() + "', " + String.valueOf(device.getPort()) + ")";
				statement.executeUpdate(sql);
			}
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	public void unregisterDevice(String uuid){
		try{
			String sql = "delete from Device where Uuid = '" + uuid +"'";
			statement.executeUpdate(sql);
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	public void registerBind(Bind bind){
		try{
			if(bind.isPrepared()){
				String sql = "insert into Bind(Uuid1, Uuid2, QNum) values ('" + bind.getBond().get(0).getUuid()
						+ "', '" + bind.getBond().get(1).getUuid() + "')";
				statement.executeUpdate(sql);
			}			
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	public void unregisterBind(String uuid){
		try{
			String sql = "delete from Bind where Uuid1 = '" + uuid +"' or Uuid2 = '" + uuid + "'";
			statement.executeUpdate(sql);
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	public void registerObserv(Observ ob){
		try{
			if(ob.isPrepared()){
				String sql = "insert into Observ(Uuid1, Uuid2, TS_S, TS_D, MS, GT) values ('" 
						+ ob.getUuid1() + "', '" + ob.getUuid2() + "', " + String.valueOf(ob.getTS_S())
						+ ", " + String.valueOf(ob.getTS_D()) + ", '" + ob.getModalityS() 
						+ "', " + String.valueOf(ob.getGt()) + ")";
				statement.executeUpdate(sql);
			}
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	
}
