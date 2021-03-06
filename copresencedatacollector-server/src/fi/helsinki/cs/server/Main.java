package fi.helsinki.cs.server;
/**
 * Server (tcp connection) for handling copresencedatacollector connections.
 * @author xzgao
 *
 */
import java.io.BufferedReader;  
import java.io.BufferedWriter;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.io.OutputStreamWriter;  
import java.io.PrintWriter;  
import java.lang.reflect.Type;
import java.net.ServerSocket;  
import java.net.Socket;  
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

import fi.helsinki.cs.audio.XCorrAndDistFromWav;

  
public class Main {  
	
	// Data structures to record current active connections
    //private List<Device> dList = new ArrayList<Device>();  
    private static HashMap<String,Device> dMap = new HashMap<String,Device>();
    private static HashMap<String,Socket> sMap = new HashMap<String,Socket>();
    private static HashMap<String,Wave> wMap = new HashMap<String,Wave>();
    //private static HashMap<String,String> aMap = new HashMap<String,String>();
    
    private ServerSocket serverSocket = null;  
    private ExecutorService mExecutorService = null; //thread pool  
    private Gson gson = new Gson();
    DBHelper db = null;
    public static void main(String[] args) {  
        new Main();  
    }  
    public Main() {  
        try {  
        	db = new DBHelper();
        	db.prepare();
        	//db.startToMem();	//restore in-mem sqlite3 database
        	serverSocket = new ServerSocket(Constants.SERVER_PORT);  //server socket
            mExecutorService = Executors.newCachedThreadPool();  //create a thread pool  
            System.out.println("Server " + Constants.SERVER_INET + " started at port " + Constants.SERVER_PORT + ".");  
            //Socket clientSocket = null;  
            Device device = new Device();
            while(true) {  
            	Socket clientSocket = serverSocket.accept();  //waits for incoming request
            	clientSocket.setKeepAlive(true);
                System.out.println("**\nClient "+ clientSocket.getInetAddress() + ":" + String.valueOf(clientSocket.getPort()) + " connected.\n**");
                	//initiate Device Object
                device.setIpAddress(clientSocket.getInetAddress());
                device.setPort(clientSocket.getPort());
                device.setSocket(clientSocket);
                device.setTS(System.currentTimeMillis());
                //Add client to client set  
                //dList.add(device); 
                
                mExecutorService.execute(new ThreadService(new Device(device))); //start a new thread to handle the connection  
            }  
        }catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    class ThreadService implements Runnable { 
    	private Device client;
        private Socket socket;
        private BufferedReader in;  
        private String msg;  
        private HashMap<String,String> msgObj;
        private Type mapType;
        //private String fp;
        
        public ThreadService(Device device) {  
        	this.client = device;
            this.socket = client.getSocket(); 
            client.setSocket(socket);
            this.in = null;
            this.msg = "";
            //this.fp = "";
            this.mapType = new TypeToken<HashMap<String,String>>(){}.getType();
            this.msgObj = new HashMap<String,String>();
            try {  
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
                //Server sends msg to client upon connection establishment  
                //msg = "Connection to " + this.socket.getInetAddress() + " established: " + String.valueOf(dList.size());  
                
                msgObj.clear();
            	//msgObj = new HashMap<String,String>();
            	msgObj.put("id", "REQ_UUID");
            	msgObj.put("ver", Constants.CURRENT_VERSION);
            	String msg = gson.toJson(msgObj);
            	System.out.println("REQ_UUID sent.");
                this.sendBackMsg(msg);  //request UUID before decision
                
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
              
        }  

        @Override  
        public void run() {  
            try {  
                while(true) {  
                    if((msg = in.readLine())!= null) {  
                        
                    	
                    	msgObj = gson.fromJson(msg, mapType);
                    	String token = msgObj.get("id");
                    	
                    	client.setTS(System.currentTimeMillis());
                    	
                    	//db = new DBHelper();
                    	//db.prepare();
                    	
                    	
                    	if(token.contains("ACK_UUID")){	//ACK with UUID
                    		System.out.println("ACK_UUID received: "+msg);
                    		String uuid = msgObj.get("uuid");
                    		String ver = msgObj.get("ver");
                    		//int status = Integer.parseInt(msgObj.get("sta"));
                    		
                    		client.setUuid(uuid);
                    		client.setVer(ver);
                    		
                			//client.setStatus(Constants.STATUS_BIND_OFF);
                    		if(!db.checkDeviceReg(uuid)){	//new device                    			
                    			db.registerDevice(client);
                    			System.out.println("ACK_UUID: new device");
                    		}else{
                    			db.updateDeviceAddr(client);
                    			db.updateVersion(client);
                    		}
                    		
                    		//db.updateStatus(uuid, status);
                    		dMap.put(uuid, client);
                    		sMap.put(uuid, socket);
                    		
                    		System.out.println("ACK_UUID: "+client.getUuid()+", "+client.getIpAddress().getHostAddress());
                    		
                    		
                    		boolean test = db.isBind(uuid);
                    		System.out.println("Bindtest: "+test);
                    		String aUuid = db.getAUuid(uuid);
                    		if(test || aUuid != ""){
                    			//String aUuid = db.getAUuid(uuid);
                    			test = true;
                        		String aName = db.getName(aUuid);
                        		boolean sta = false;
                        		if(dMap.containsKey(aUuid) && sMap.containsKey(aUuid)){
                        			if(((System.currentTimeMillis()-dMap.get(aUuid).getTS()) < 120*1000)){
                        				sta = true;
                        			}
                        		}
                        		msgObj.clear();   			
                            	msgObj.put("id", "UP_BIND");
                            	msgObj.put("bind", "true");
                            	msgObj.put("bindId", aUuid);
                            	msgObj.put("bindName", aName);
                            	msgObj.put("peerOn", String.valueOf(sta));
                            	//boolean peerOn = (sMap.get(aUuid)==null)?false:sMap.get(aUuid).isClosed();
                            	//msgObj.put("peer", String.valueOf(peerOn));
                            	String msg = gson.toJson(msgObj);
                            	System.out.println("UP_BIND Bind test: "+uuid+" "+test);
                            	//System.out.println(aUuid + aName);
                            	this.sendBackMsg(msg);
                            	if(sta){
                            		msgObj.clear();   			
                                	msgObj.put("id", "ACK_ALIVE");
                                	msgObj.put("bind", "true");
                                	msgObj.put("bindId", uuid);
                                	msgObj.put("bindName", db.getName(uuid));
                                	msgObj.put("peerOn", String.valueOf(true));
                                	msgObj.put("flag", "0");
                                	String msg2 = gson.toJson(msgObj);
                                	//System.out.println("ACK_ALIVE Bind test: "+uuid+" "+test);
                                	sendMsg(msg2, aUuid);
                            	}
                    		}else{
                    			msgObj.clear();   			
                            	msgObj.put("id", "UP_BIND");
                            	msgObj.put("bind", "false");
                            	String msg = gson.toJson(msgObj);
                            	System.out.println("UP_BIND Bind test: "+uuid+" "+test);                    		
                            	this.sendBackMsg(msg);
                    		}                    		
                    		
                        	
                    	}else if(token.contains("REQ_GETQ")){	//initiate binding
                    		System.out.println("REQ_GETQ received: "+msg);
                    		String uuid = msgObj.get("uuid");
                    		db.cleanBind();
                    		boolean test = db.isBind(uuid);
                    		String aUuid = db.getAUuid(uuid);
                    		if(!test && aUuid == ""){
                    			String name = msgObj.get("name");
                    			client.setName(name);
                    			db.updateDeviceName(client);
                    			int qnum = (int)(Math.random()*8847);
                    			
                    			db.registerBind(client, qnum);
                    		
                    			//System.out.println("REQ_GETQ: "+client.getUuid()+" "+client.getName());
                    		
                    			msgObj.clear();                    			
                    			msgObj.put("id", "ACK_GETQ");
                    			msgObj.put("qnum", String.valueOf(qnum));
                    			String msg = gson.toJson(msgObj);
                    			System.out.println("ACK_GETQ sent.");
                    			this.sendBackMsg(msg);	//send back to active device
                    		}else{
                    			msgObj.clear();                    			
                    			msgObj.put("id", "ACK_GETQ");
                    			msgObj.put("deny", "Already bound.");
                    			String msg = gson.toJson(msgObj);
                    			System.out.println("ACK_GETQ sent.");
                    			this.sendBackMsg(msg);	//send back to active device
                    		}
                        	
                    	}else if(token.contains("REQ_VALQ")){	//"This is my 2nd device"
                    		System.out.println("REQ_VALQ received: "+msg);
                    		String uuid = msgObj.get("uuid");
                    		String qnums = msgObj.get("qnum");
                    		int qnum = Integer.parseInt(qnums);
                    		String name = msgObj.get("name");
                    		client.setName(name);
                    		db.updateDeviceName(client);
                    		//System.out.println("REQ_VALQ: "+client.getUuid()+client.getName()+qnum);
                    		
                    		if(db.isQnumFit(qnum)){	//found half empty entry
                    			System.out.println("REQ_VALQ:"+" qnum fit.");
                    			db.updateBind(uuid, qnum);
                    			String aUuid = db.getAUuid(uuid);
                    			String aName = db.getName(aUuid);
                    			//Socket so = getSocket(aUuid);
                    			//System.out.println("REQ_VALQ:(o) "+aUuid+aName);
                    			//db.updateDeviceStatus(Constants.STATUS_PEER_OFF, aUuid);
                    			//db.updateDeviceStatus(Constants.STATUS_PEER_OFF, uuid);
                    			//client.setStatus(Constants.STATUS_PEER_OFF);
                    			dMap.put(uuid, client);
                    			                    			
                    			msgObj.clear();                    			
                            	msgObj.put("id", "ACK_VALQ");
                            	msgObj.put("uuid", aUuid);
                            	msgObj.put("name", aName);
                            	String msg1 = gson.toJson(msgObj);
                            	System.out.println("ACK_VALQ sent to "+uuid);
                            	this.sendBackMsg(msg1);	// send to device B UUID & Name of Device A
                            	msgObj.clear();                    			
                            	msgObj.put("id", "ACK_VALQ");
                            	msgObj.put("uuid", uuid);
                            	msgObj.put("name", name);
                            	String msg2 = gson.toJson(msgObj);
                            	System.out.println("ACK_VALQ sent to "+aUuid);
                            	this.sendMsg(msg2,aUuid);
                    		}
                    		
                    	}else if(token.contains("REQ_ALIVE")){	// request heartbeat
                    		System.out.println("REQ_ALIVE received: " + msg);
                    		String uuid = msgObj.get("uuid");
                    		//int status = Integer.parseInt(msgObj.get("sta"));
                    		
                    		if(client.getUuid() == ""){
                    			// similar to ACK_UUID
                    			
                    			client.setUuid(uuid);
                    			if(!db.checkDeviceReg(uuid)){	//new device                    			
                        			db.registerDevice(client);
                        			System.out.println("REQ_ALIVE: new connection");
                        		}else{
                        			db.updateDeviceAddr(client);
                        		}
                    			
                        		dMap.put(uuid, client);
                        		sMap.put(uuid, socket);
                    		}
                    		//db.updateStatus(uuid, status);
                    		
                    		boolean test = db.isBind(uuid);
                    		String aUuid = db.getAUuid(uuid);
                    		if(test || aUuid != ""){
                    			//String aUuid = db.getAUuid(uuid);
                        		String aName = db.getName(aUuid);
                        		boolean sta = false;
                        		if(dMap.containsKey(aUuid) && sMap.containsKey(aUuid)){
                        			if(((System.currentTimeMillis()-dMap.get(aUuid).getTS()) < 120*1000)){
                        				sta = true;
                        			}
                        		}
                        		if(sta){
                        			msgObj.clear();   			
                                	msgObj.put("id", "ACK_ALIVE");
                                	msgObj.put("bind", "true");
                                	msgObj.put("bindId", aUuid);
                                	msgObj.put("bindName", aName);
                                	msgObj.put("peerOn", String.valueOf(sta));
                                	msgObj.put("flag", "1");
                                	String msg = gson.toJson(msgObj);
                                	System.out.println("ACK_ALIVE Bind test: "+uuid+" "+test);
                                	sendBackMsg(msg);
                                	msgObj.clear();   			
                                	msgObj.put("id", "ACK_ALIVE");
                                	msgObj.put("bind", "true");
                                	msgObj.put("bindId", uuid);
                                	msgObj.put("bindName", db.getName(uuid));
                                	msgObj.put("peerOn", String.valueOf(true));
                                	msgObj.put("flag", "0");
                                	String msg2 = gson.toJson(msgObj);
                                	//System.out.println("ACK_ALIVE Bind test: "+uuid+" "+test);
                                	sendMsg(msg2, aUuid);
                        		}else{
                        			msgObj.clear();   			
                                	msgObj.put("id", "ACK_ALIVE");
                                	msgObj.put("bind", "true");
                                	msgObj.put("bindId", aUuid);
                                	msgObj.put("bindName", aName);
                                	msgObj.put("peerOn", String.valueOf(sta));
                                	msgObj.put("flag", "1");
                                	String msg = gson.toJson(msgObj);
                                	System.out.println("ACK_ALIVE Bind test: "+uuid+" "+test);
                                	sendBackMsg(msg);
                        		}
                        		
                    		}else{
                    			msgObj.clear();   			
                            	msgObj.put("id", "ACK_ALIVE");
                            	msgObj.put("bind", "false");
                            	String msg = gson.toJson(msgObj);
                            	System.out.println("ACK_ALIVE Bind test: "+uuid+" "+test);                   		
                            	this.sendBackMsg(msg);
                    		}
                    		
                    	}else if(token.contains("REQ_TASK")){	//start tasks after 5 seconds
                    		System.out.println("REQ_TASK received: " + msg);
                    		String uuid = msgObj.get("uuid");
                    		String gt = msgObj.get("gt");
                    		String mt = msgObj.get("mt");
                    		//String mod = msgObj.get("mod");
                    		String aUuid = db.getAUuid(uuid);
                    		//Socket so = getSocket(aUuid);
                    		
                    		/*client.setRole(true);
                    		if(dMap.containsKey(aUuid)){
                    			dMap.get(aUuid).setRole(false);
                    		}*/
                    		
                    		System.out.println("Peer client found: "+dMap.containsKey(aUuid));
                    		System.out.println("Peer socket found: "+sMap.containsKey(aUuid));
                    		boolean sta = false;
                    		if(dMap.containsKey(aUuid) && sMap.containsKey(aUuid)){
                    			if(((System.currentTimeMillis()-dMap.get(aUuid).getTS()) < 120*1000)){
                    				sta = true;
                    			}
                    		}
                    		if(sta){
                    			msgObj.clear();                    			
                            	msgObj.put("id", "REQ_AGREE");
                            	//msgObj.put("dt", "0");
                            	msgObj.put("gt", gt);
                            	msgObj.put("mt", mt);
                            	//msgObj.put("mod", mod);
                            	String msg = gson.toJson(msgObj);
                            	System.out.println("REQ_AGREE sent to "+aUuid);
                            	sendMsg(msg, aUuid);
                    		}else{
                    			msgObj.clear();                    			
                            	msgObj.put("id", "ACK_TASK");
                            	msgObj.put("gt", "0");
                            	//msgObj.put("mod", mod);
                            	String msg = gson.toJson(msgObj); 
                            	System.out.println("ACK_TASK FIAL sent to "+uuid);
                            	sendBackMsg(msg);
                    		}
                    		
                    	}else if(token.contains("ACK_AGREE")){	//start tasks after 5 seconds
                    		System.out.println("ACK_AGREE received: " + msg);
                    		String uuid = msgObj.get("uuid");
                    		String gt = msgObj.get("gt");
                    		String mt = msgObj.get("mt");
                    		//String mod = msgObj.get("mod");
                    		String aUuid = db.getAUuid(uuid);
                    		//Socket so = getSocket(aUuid);
                    		Observ obs = new Observ();
                    		obs.setUuid(uuid, aUuid);
                    		obs.setTS_S();
                    		obs.setGt(Integer.parseInt(gt));
                    		db.registerObserv(obs);	//add observ to database
                    		int obNum = db.getObservNumber(obs);
                    		System.out.println("Peer client found: "+dMap.containsKey(aUuid));
                    		System.out.println("Peer socket found: "+sMap.containsKey(aUuid));
                    		boolean sta = false;
                    		if(dMap.containsKey(aUuid) && sMap.containsKey(aUuid)){
                    			if(((System.currentTimeMillis()-dMap.get(aUuid).getTS()) < 120*1000)){
                    				sta = true;
                    			}
                    		}
                    		if(sta){
                    			client.setRole(false);
                        		if(dMap.containsKey(aUuid)){
                        			dMap.get(aUuid).setRole(true);
                        		}
                        		
                    			msgObj.clear();                    			
                            	msgObj.put("id", "ACK_TASK");
                            	//msgObj.put("dt", "0");
                            	msgObj.put("ob", String.valueOf(obNum));
                            	msgObj.put("gt", gt);
                            	msgObj.put("mt", mt);
                            	msgObj.put("ar", String.valueOf(false));
                            	//msgObj.put("mod", mod);
                            	String msg = gson.toJson(msgObj); 
                            	System.out.println("ACK_TASK sent to "+uuid);
                            	sendBackMsg(msg);
                            	//this.sendBackMsg(msg);
                            	msgObj.clear();                    			
                            	msgObj.put("id", "ACK_TASK");
                            	//msgObj.put("dt", "0");
                            	msgObj.put("ob", String.valueOf(obNum));
                            	msgObj.put("gt", gt);
                            	msgObj.put("mt", mt);
                            	msgObj.put("ar", String.valueOf(true));
                            	//msgObj.put("mod", mod);
                            	String msg2 = gson.toJson(msgObj);
                            	System.out.println("ACK_TASK sent to "+aUuid);
                            	sendMsg(msg2, aUuid);
                    		}/*else{
                    			msgObj.clear();                    			
                            	msgObj.put("id", "ACK_TASK");
                            	msgObj.put("gt", "0");
                            	//msgObj.put("mod", mod);
                            	String msg = gson.toJson(msgObj); 
                            	System.out.println("ACK_TASK FIAL sent to "+uuid);
                            	sendBackMsg(msg);
                    		}*/
                    		
                    	}else if(token.contains("REQ_UNBIND")){
                    		System.out.println("REQ_UNBIND reveiced: " + msg);
                    		String uuid = msgObj.get("uuid");
                    		boolean test = db.isBind(uuid);
                    		String aUuid = db.getAUuid(uuid);
                    		if(test || aUuid != ""){
                    			//String aUuid = db.getAUuid(uuid);
                    			//Socket so = getSocket(aUuid);
                    			db.unregisterBind(uuid);
                    			msgObj.clear();                    			
                    			msgObj.put("id", "ACK_UNBIND");
                    			String msg = gson.toJson(msgObj);
                    			System.out.println("ACK_UNBIND sent to "+uuid);
                    			this.sendBackMsg(msg);
                    			System.out.println(dMap.containsKey(aUuid));
                    			if(sMap.containsKey(aUuid)){
                    				msgObj.clear();                    			
                    				msgObj.put("id", "ACK_UNBIND");
                    				String msg2 = gson.toJson(msgObj);
                    				System.out.println("ACK_UNBIND sent to "+aUuid);
                    					this.sendMsg(msg2,aUuid);
                    			}
                    		}else{
                    			msgObj.clear();                    			
                    			msgObj.put("id", "ACK_UNBIND");
                    			msgObj.put("deny", "Not yet bind.");
                    			String msg = gson.toJson(msgObj);
                    			this.sendBackMsg(msg);
                    		}
                    	}else if(token.contains("SEND")){	//forward msg to peer
                    		//System.out.println("SEND received: " + msg);
                    		
                    		String uuid = msgObj.get("uuid");
                    		String waveHeaderJson = msgObj.get("header");
                    		String waveData = msgObj.get("data");
                    		String aUuid = db.getAUuid(uuid);
                    		
                    		Gson gson = new Gson();
                      		WaveHeader waveHeaderRemote = gson.fromJson(waveHeaderJson, WaveHeader.class);
                      		byte[] dataRemote = null;
                      		try {
                      			dataRemote = Hex.decodeHex(waveData.toCharArray());
                    		} catch (DecoderException e) {
                    			// TODO Auto-generated catch block
                    			e.printStackTrace();
                    		}
                      		Wave waveLocal = new Wave(waveHeaderRemote, dataRemote);
                      		
                      		if(aUuid != ""){
                      			if(wMap.containsKey(aUuid)){       
                      				//String nUuid = (client.getRole())?aUuid:uuid;
                      				new Thread((new AudioTask(waveLocal, wMap.get(aUuid), uuid, aUuid))).start();
                      				/*boolean flag = false;
                      				while(flag){
                      					Thread.sleep(1000);
                      					if(aMap.containsKey(nUuid)){
                      						if(aMap.get(nUuid) != ""){
                      							flag = true;
                      							msgObj.clear();                    			
                      				        	msgObj.put("id", "SEND");
                      				        	msgObj.put("wavefp", fp);
                      				        	String msg = gson.toJson(msgObj);
                      				        	if(!client.getRole()){
                      				        		sendBackMsg(msg);
                      				        	}else{
                      				        		sendMsg(msg,aUuid);
                      				        	}
                      				        	aMap.remove(nUuid);
                      						}
                      					}
                      				}*/
                          		}else{
                          			wMap.put(uuid, waveLocal);
                          		}
                      		}
                        	
                    	}else if(token.contains("EXIT")){
                    		// client leaves
                    		System.out.println("Connection shutdown."); 
                    		//System.out.println(msg);
                            //dList.remove(client);  
                            client.resetSocket();
                            in.close();  
                            //msg = "Client " + socket.getInetAddress() + " exit, total: " + String.valueOf(dList.size());  
                            socket.close(); 
                            //this.sendmsg(msg);
                    		break;
                    	/*}else{
                    		// Server gets msg from client and sends back to client
                            msg = socket.getInetAddress() + ": " + msg;  
                            this.sendmsg(msg);  
                    		break; */
                    	}  
                    	
                    	//db.terminate();
                    }  
                }  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        
        //send back msg
        public void sendBackMsg(String msg) {  
            System.out.println(msg);  
            Socket mSocket = socket;  
            PrintWriter pout = null;  
            try {  
                pout = new PrintWriter(new BufferedWriter(  
                        new OutputStreamWriter(mSocket.getOutputStream())),true);  
                pout.println(msg);  
                pout.flush();
            }catch (IOException e) {  
                e.printStackTrace();  
            }             
        }
        
        /*public boolean commitUpdateOnList(Device device){
        	int num =dList.size();  
            for (int index = 0; index < num; index ++) {  
         	   if(dList.get(index).getIpAddress() == device.getIpAddress() && dList.get(index).getPort() == device.getPort()){
         		  dList.get(index).setUuid(device.getUuid());
         		  System.out.println("registered.");
         		  return true;
         	   }
            }
            return false;
        }*/
        
        
        
        /*public InetAddress getInet(String uuid){
        	int num =dList.size();  
            for (int index = 0; index < num; index ++) {  
         	   if(dList.get(index).getUuid() == uuid){
         		   return dList.get(index).getIpAddress();
         	   }
            }
            return null;            
        }*/
        
        
        //send msg to socket
        public void sendMsg(String msg, String uuid) {  
            System.out.println(msg);             
            //Socket mSocket = dMap.get(uuid).getSocket();
            Socket mSocket = sMap.get(uuid);
            PrintWriter pout = null;  
            try {  
            	
                pout = new PrintWriter(new BufferedWriter(  
                        new OutputStreamWriter(mSocket.getOutputStream())),true);  
                pout.println(msg);  
                pout.flush();
            }catch (IOException e) {  
                e.printStackTrace();  
            }             
        }
        
        //send msg to socket
        public void sendNewMsg(String msg, String uuid) {  
            System.out.println(msg);             
            
            PrintWriter pout = null;  
            try {  
            	Socket mSocket = new Socket(dMap.get(uuid).getIpAddress(), dMap.get(uuid).getPort());
                pout = new PrintWriter(new BufferedWriter(  
                        new OutputStreamWriter(mSocket.getOutputStream())),true);  
                pout.println(msg);  
                pout.flush();
            }catch (IOException e) {  
                e.printStackTrace();  
            }             
        }
        
        /**
         * To relay raw audio wav to its peer for further feature extraction.
         * @author xzgao
         *
         */
        public class AudioTask implements Runnable{
        	private Wave waveRemote, waveLocal;
        	private String uuid, aUuid;
        	private String fp;
        	final DecimalFormat df1 = new DecimalFormat("#.######");
        	final DecimalFormat df2 = new DecimalFormat("#.####");
        	final float[] trimSeconds = {10, 5};
        	
        	public AudioTask(Wave wave1, Wave wave2, String uuid, String aUuid){
        		super();
        		this.waveLocal = wave1;
        		this.waveRemote = wave2;
        		this.uuid = uuid;
        		this.aUuid = aUuid;
        		this.fp = "";
        	}
        	
    		@Override
    		public void run() {
    			// TODO Auto-generated method stub
    			if(waveRemote.length() > 0 && waveLocal.length() > 0){
      	  			for(int i = 0; i < 2; i++){
      	  				if(waveRemote.length() > trimSeconds[i]){
      	  					waveRemote.rightTrim(waveRemote.length() - trimSeconds[i]);
      	  				}
      	  				if(waveLocal.length() > trimSeconds[i]){
      	  					waveLocal.rightTrim(waveLocal.length()  - trimSeconds[i]);
      	  				}
      	  				
      	  				XCorrAndDistFromWav xCorrAndDistFromWav = new XCorrAndDistFromWav(waveRemote, waveLocal);
      	  				fp += df1.format(xCorrAndDistFromWav.getMaxCorr())+"#"+df2.format(xCorrAndDistFromWav.getDist())+"#";
      	  				
      	  			}
      	  		}
    			wMap.remove(uuid);
      			wMap.remove(aUuid);
      			msgObj.clear();                    			
    	        	msgObj.put("id", "SEND");
    	        	msgObj.put("wavefp", fp);
    	        	String msg = gson.toJson(msgObj);
    	        	if(!client.getRole()){
    	        		sendBackMsg(msg);
    	        	}else{
    	        		sendMsg(msg,aUuid);
    	        	}
    		}
        	
        }
        
      /** 
       * Iterate client set，and send msgs to all clients 
       */  
      /* public void broadcastMsg(String msg) {  
           System.out.println(msg);  
           int num =dList.size();  
           for (int index = 0; index < num; index ++) {  
        	   if(dList.get(index).getStatus() == DeviceStatus.STATUS_BIND_OFF && dList.get(index).getUuid() != client.getUuid()){
               Socket mSocket = dList.get(index).getSocket();  
               PrintWriter pout = null;  
               try {  
                   pout = new PrintWriter(new BufferedWriter(  
                           new OutputStreamWriter(mSocket.getOutputStream())),true);  
                   pout.println(msg);  
               }catch (IOException e) {  
                   e.printStackTrace();  
               }  
           } 
        } 
       }  */
    }      
    
    
    
    	
}  