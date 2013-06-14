package fi.helsinki.cs.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DBHelper {
	
	private Connection connection;
	private Statement statement;
	
	public DBHelper() throws ClassNotFoundException{
		Class.forName("org.sqlite.JDBC");
		this.connection = null;
	}
	
	public void prepareMem() throws SQLException{
		connection = DriverManager.getConnection(Constants.DB_MEM);
		statement = connection.createStatement();
	}
	
	public void prepare() throws SQLException{
		connection = DriverManager.getConnection(Constants.DB_LOCAL);
		statement = connection.createStatement();
	}
	
	// Start on-memory database
	public void startToMem(){
		try{			
			//statement.setQueryTimeout(15);
			statement.executeUpdate("restore from index.db");
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}		
	}
	
	// Dump to disk
	public void backupFromMem(){
		try{
			statement.executeUpdate("backup to index.db");
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	public void terminateMem(){
		try{
			backupFromMem();
			if(connection != null)
				connection.close();
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	public void terminate(){
		try{
			
			if(connection != null)
				connection.close();
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	// Check if device registered or not
	public boolean checkDeviceReg(String uuid){
		try{
			String sql = "select * from Device where Uuid='" + uuid + "'";
			ResultSet rs = statement.executeQuery(sql);
			if(rs.next()){
				return true;
			}
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
		return false;
	}
	
	// Register device on first connection
	public void registerDevice(Device device){
		try{
			String sql = "insert into Device(Uuid, Addr, Port) values('" + device.getUuid() + "', '" + device.getIpAddress().getHostAddress()
					+ "', " + String.valueOf(device.getPort()) + ")";
			System.out.println(sql);
			statement.executeUpdate(sql);
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	public void updateDeviceAddr(Device device){
		try{
			String sql = "update Device set Addr='" + device.getIpAddress().getHostAddress() + "' and Port=" + device.getPort() + " where Uuid='" + device.getUuid() + "'";
			statement.executeUpdate(sql);
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	public void updateDeviceName(Device device){
		try{
			String sql = "update Device set Name='" + device.getName() + "' where Uuid='" + device.getUuid() + "'";
			statement.executeUpdate(sql);
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	// Update device status
	/*public void updateDeviceStatus(int sta, String uuid){
		try{
			String sql = "update Device set Status=" + String.valueOf(sta) + " where Uuid='" + uuid + "'";
			statement.executeUpdate(sql);
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}*/
	
	public Boolean isBind(String uuid){
		try{
			String sql = "select * from Bind where (Uuid1='" + uuid + "' and Uuid2!='') or Uuid2='" + uuid + "'";
			ResultSet rs = statement.executeQuery(sql);
			if(rs.next()){
				return true;
			}
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
		return false;
	}
	
	public void cleanBind(){
		try{
				String sql = "delete from Bind where Uuid2=''";
				statement.executeUpdate(sql);
				
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	// First add one device to bind
	public void registerBind(Device device, int qnum){
		try{
				String sql = "insert into Bind(Uuid1, QNum) values ('" + device.getUuid()
						+ "', " + String.valueOf(qnum) + ")";
				statement.executeUpdate(sql);
				
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	// check QNum ok
	public boolean isQnumFit(int qnum){
		try{
			String sql = "select * from Bind where Uuid1!='' and Uuid2='' and QNum=" + String.valueOf(qnum);
			//System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);
			if(rs.next()){
				return true;
			}
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
		return false;
	}
	
	// registry bind
	public void updateBind(String uuid, int qnum){
		try{
			String sql = "update Bind set Uuid2='" + uuid + "' where Uuid2='' and QNum=" + qnum;
			statement.executeUpdate(sql);
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	// get the other uuid of bind device
	public String getAUuid(String uuid){
		try{
			String sql = "select * from Bind where Uuid1='" + uuid + "' or Uuid2='" + uuid + "'" ;
			ResultSet rs = statement.executeQuery(sql);
			if(rs.next()){
				return (rs.getString("Uuid1").contains(uuid))?rs.getString("Uuid2"):rs.getString("Uuid1");
			}
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
		return null;
	}
	
	// get the name of device
	public String getName(String uuid){
		try{
			String sql = "select * from Device where Uuid='" + uuid + "'";
			ResultSet rs = statement.executeQuery(sql);
			if(rs.next()){
				return rs.getString("Name");
			}
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
		return null;
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
			String sql = "insert into Observ(Uuid1, Uuid2, TS_S, MS, GT) values('" 
					+ ob.getUuid1() + "', '" + ob.getUuid2() + "', " + String.valueOf(ob.getTS_S())
					+ ", '" + ob.getModalityS() + "', " + String.valueOf(ob.getGt()) + ")";
			statement.executeUpdate(sql);
		}catch(SQLException e){
			System.err.println(e.getMessage());
		}
	}
	
	
}
