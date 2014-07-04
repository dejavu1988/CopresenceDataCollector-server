package fi.helsinki.cs.server;

import java.net.InetAddress;
import java.net.Socket;


public class Device {
	
	private String uuid;
	private String name;
	private InetAddress ipAddress;
	private int port;
	private Socket socket;
	//private int status;
	private long ts;
	private String ver;
	//private int gt;
	private boolean role;
	
	public Device(){
		this.uuid = "";
		this.name = "";
		this.ipAddress = null;
		this.port = -1;
		this.socket = null;
		//this.status = 0;
		this.ts = System.currentTimeMillis();
		this.ver = "";
		//this.gt = 0;
		//this.role = false;
	}
	
	public Device(Device device){
		this.uuid = device.getUuid();
		this.name = device.getName();
		this.ipAddress = device.getIpAddress();
		this.port = device.getPort();
		this.socket = device.getSocket();
		//this.status = device.getStatus();
		this.ts = device.getTS();
		this.ver = device.getVer();
		//this.gt = device.getGT();
		//this.role = device.getRole();
	}
	
	/*public int getGT(){
		return this.gt;
	}
	
	public void setGT(int gt){
		this.gt = gt;
	}*/
	public boolean getRole(){
		return this.role;
	}
	
	public void setRole(boolean role){
		this.role = role;
	}
	
	public InetAddress getIpAddress(){
		return this.ipAddress;
	}
	
	public void setIpAddress(InetAddress ipAddress){
		this.ipAddress = ipAddress;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	public String getVer(){
		return this.ver;
	}
	
	public void setVer(String ver){
		this.ver = ver;
	}
	
	public String getUuid(){
		return this.uuid;
	}
	
	public void setUuid(String uuid){
		this.uuid = uuid;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public Socket getSocket(){
		return this.socket;
	}
	
	public void setSocket(Socket socket){
		this.socket = socket;
	}
	
	public void resetSocket(){
		this.socket = null;
	}
	
	public long getTS(){
		return this.ts;
	}
	
	public void setTS(long ts){
		this.ts = ts;
	}
	
	/*public boolean isPrepared(){
		return (this.uuid.length() != 0 && this.name.length() != 0 && this.ipAddress != null && this.port >=0);
	}*/
	
	/*public boolean isDeviceActive(){
		return (this.socket != null);
	}*/
}
