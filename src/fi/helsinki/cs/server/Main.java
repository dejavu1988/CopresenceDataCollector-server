package fi.helsinki.cs.server;

import java.io.BufferedReader;  
import java.io.BufferedWriter;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.io.OutputStreamWriter;  
import java.io.PrintWriter;  
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;  
import java.net.Socket;  
import java.util.ArrayList;  
import java.util.HashMap;
import java.util.List;  
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
  
  
public class Main {  
	
    private List<Device> dList = new ArrayList<Device>();  
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
        	db.startToMem();	//restore in-mem sqlite3 database
        	serverSocket = new ServerSocket(Constants.PORT);  //server socket
            mExecutorService = Executors.newCachedThreadPool();  //create a thread pool  
            System.out.println("Server " + Constants.SERVER_INET + " started at port " + Constants.PORT + ".");  
            Socket clientSocket = null;  
            while(true) {  
            	clientSocket = serverSocket.accept();  //waits for incoming request
            	clientSocket.setKeepAlive(true);
                System.out.println("Client "+ clientSocket.getInetAddress() + ":" + String.valueOf(clientSocket.getPort()) + " connected.");
                Device device = new Device();	//initiate Device Object
                device.setIpAddress(clientSocket.getInetAddress());
                device.setPort(clientSocket.getPort());
                device.setSocket(clientSocket);
                //Add client to client set  
                dList.add(device); 
                mExecutorService.execute(new ThreadService(device)); //start a new thread to handle the connection  
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
        
        public ThreadService(Device device) {  
        	this.client = device;
            this.socket = device.getSocket(); 
            this.in = null;
            this.msg = "";
            this.msgObj = new HashMap<String,String>();
            try {  
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
                //Server sends msg to client upon connection establishment  
                //msg = "Connection to " + this.socket.getInetAddress() + " established: " + String.valueOf(dList.size());  
                
                msgObj.clear();
            	//msgObj = new HashMap<String,String>();
            	msgObj.put("id", "REQ_UUID");
            	String msg = gson.toJson(msgObj);
            	
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
                        
                    	Type mapType = new TypeToken<HashMap<String,String>>(){}.getType();
                    	HashMap<String,String> msgObj = gson.fromJson(msg, mapType);
                    	String token = msgObj.get("id");
                    	if(token == "ACK_UUID"){	//ACK with UUID
                    		String uuid = msgObj.get("uuid");
                    		String uuid2 = null;
                    		int tmpStatus = 0;
                    		if(-1 == (tmpStatus = db.checkDeviceReg(uuid))){	//new device
                    			client.setUuid(uuid);
                    			client.setStatus(Constants.STATUS_BIND_OFF);
                    			db.registerDevice(client);
                    		}
                    		
                    	}else if(token == "REQ_GETQ"){	//initiate binding
                    		String uuid = msgObj.get("uuid");
                    		String name = msgObj.get("name");
                    		int qnum = (int)(Math.random()*8847);
                    		db.registerBind(client, qnum);
                    		client.setName(name);
                    		db.updateDeviceName(client);
                    		
                    		msgObj.clear();                    			
                        	msgObj.put("id", "ACK_GETQ");
                        	msgObj.put("qnum", String.valueOf(qnum));
                        	String msg = gson.toJson(msgObj);
                        	this.sendBackMsg(msg);	//send back to active device
                        	
                    	}else if(token == "REQ_VALQ"){	//"This is my 2nd device"
                    		
                    		String uuid = msgObj.get("uuid");
                    		String qnum = msgObj.get("qnum");
                    		String name = msgObj.get("name");
                    		client.setName(name);
                    		db.updateDeviceName(client);
                    		
                    		if(db.isQnumFit(uuid, qnum)){	//found half empty entry
                    			db.updateBind(uuid, qnum);
                    			String aUuid = db.getAUuid(uuid);
                    			String aName = db.getName(aUuid);
                    			Socket so = getSocket(aUuid);
                    			db.updateDeviceStatus(Constants.STATUS_PEER_OFF, aUuid);
                    			db.updateDeviceStatus(Constants.STATUS_PEER_OFF, uuid);
                    			client.setStatus(Constants.STATUS_PEER_OFF);
                    			msgObj.clear();                    			
                            	msgObj.put("id", "ACK_VALQ");
                            	msgObj.put("uuid", aUuid);
                            	msgObj.put("name", aName);
                            	String msg1 = gson.toJson(msgObj);
                            	this.sendBackMsg(msg);	// send to device B UUID & Name of Device A
                            	msgObj.clear();                    			
                            	msgObj.put("id", "ACK_VALQ");
                            	msgObj.put("uuid", uuid);
                            	msgObj.put("name", name);
                            	String msg2 = gson.toJson(msgObj);
                            	this.sendMsg(msg2,so);
                    		}
                    		
                    	}else if(token == "REQ_REACH"){	// request recheability of peer
                    		String uuid = msgObj.get("uuid");
                    		String aUuid = db.getAUuid(uuid);
                    		InetAddress ip = getInet(aUuid);
                    		msgObj.clear();                    			
                        	msgObj.put("id", "ACK_VALQ");
                    		if(ip.isReachable(1000)){
                            	msgObj.put("r", "true"); 
                            	db.updateDeviceStatus(Constants.STATUS_PEER_ON, aUuid);
                    			db.updateDeviceStatus(Constants.STATUS_PEER_ON, uuid);
                    			client.setStatus(Constants.STATUS_PEER_ON);
                    		}else{
                    			msgObj.put("r", "false"); 
                    		}
                    		String msg = gson.toJson(msgObj);
                    		this.sendBackMsg(msg);
                    		
                    	}else if(token == "REQ_TASK"){	//start tasks after 5 seconds
                    		String uuid = msgObj.get("uuid");
                    		String gt = msgObj.get("gt");
                    		//String mod = msgObj.get("mod");
                    		String aUuid = db.getAUuid(uuid);
                    		Socket so = getSocket(aUuid);
                    		msgObj.clear();                    			
                        	msgObj.put("id", "ACK_TASK");
                        	msgObj.put("dt", "5000");
                        	msgObj.put("gt", gt);
                        	//msgObj.put("mod", mod);
                        	String msg1 = gson.toJson(msgObj);                        	
                        	msgObj.clear();                    			
                        	msgObj.put("id", "ACK_TASK");
                        	msgObj.put("dt", "5000");
                        	//msgObj.put("mod", mod);
                        	String msg2 = gson.toJson(msgObj);
                        	this.sendMsg(msg1,so);
                        	this.sendBackMsg(msg2);
                    		
                    	}else if(token == "UNBIND"){
                    		String uuid = msgObj.get("uuid");
                    		String aUuid = db.getAUuid(uuid);
                    		Socket so = getSocket(aUuid);
                    		db.unregisterBind(uuid);
                    		msgObj.clear();                    			
                        	msgObj.put("id", "UNBIND");
                        	String msg = gson.toJson(msgObj);
                        	this.sendMsg(msg,so);
                        	
                    	}else if(token == "EXIT"){
                    		// client leaves
                    		System.out.println("Connection shutdown.");  
                            dList.remove(client);  
                            client.resetSocket();
                            in.close();  
                            msg = "Client " + socket.getInetAddress() + " exit, total: " + String.valueOf(dList.size());  
                            socket.close(); 
                            //this.sendmsg(msg);
                    		break;
                    	/*}else{
                    		// Server gets msg from client and sends back to client
                            msg = socket.getInetAddress() + ": " + msg;  
                            this.sendmsg(msg);  
                    		break; */
                    	}  
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
            }catch (IOException e) {  
                e.printStackTrace();  
            }             
        }
        
        public Socket getSocket(String uuid){
        	int num =dList.size();  
            for (int index = 0; index < num; index ++) {  
         	   if(dList.get(index).getUuid() == uuid){
         		   return dList.get(index).getSocket();
         	   }
            }
            return null;            
        }
        
        public InetAddress getInet(String uuid){
        	int num =dList.size();  
            for (int index = 0; index < num; index ++) {  
         	   if(dList.get(index).getUuid() == uuid){
         		   return dList.get(index).getIpAddress();
         	   }
            }
            return null;            
        }
        
        
        //send msg to socket
        public void sendMsg(String msg, Socket so) {  
            System.out.println(msg);  
            Socket mSocket = so;  
            PrintWriter pout = null;  
            try {  
                pout = new PrintWriter(new BufferedWriter(  
                        new OutputStreamWriter(mSocket.getOutputStream())),true);  
                pout.println(msg);  
            }catch (IOException e) {  
                e.printStackTrace();  
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