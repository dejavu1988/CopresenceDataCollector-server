package fi.helsinki.cs.server;

import java.io.BufferedReader;  
import java.io.BufferedWriter;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.io.OutputStreamWriter;  
import java.io.PrintWriter;  
import java.net.ServerSocket;  
import java.net.Socket;  
import java.util.ArrayList;  
import java.util.List;  
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  
  
  
public class Main {  
    private static final int PORT = 8887;  
    private List<Socket> mList = new ArrayList<Socket>();  
    private ServerSocket server = null;  
    private ExecutorService mExecutorService = null; //thread pool  
      
    public static void main(String[] args) {  
        new Main();  
    }  
    public Main() {  
        try {  
            server = new ServerSocket(PORT);  
            mExecutorService = Executors.newCachedThreadPool();  //create a thread pool  
            System.out.println("Server started...");  
            Socket client = null;  
            while(true) {  
                client = server.accept();  
                //Add client to client set  
                mList.add(client);  
                mExecutorService.execute(new Service(client)); //start a new thread to handle the connection  
            }  
        }catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    class Service implements Runnable {  
            private Socket socket;  
            private BufferedReader in = null;  
            private String msg = "";  
              
            public Service(Socket socket) {  
                this.socket = socket;  
                try {  
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
                    //Server sends msg to client upon connection establishment  
                    msg = "Connection to " + this.socket.getInetAddress() + " established: " +mList.size();  
                    this.sendmsg();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
                  
            }  
  
            @Override  
            public void run() {  
                try {  
                    while(true) {  
                        if((msg = in.readLine())!= null) {  
                            //When client's msg is "exit"，close connection  
                            if(msg.equals("exit")) {  
                                System.out.println("ssssssss");  
                                mList.remove(socket);  
                                in.close();  
                                msg = "user: " + socket.getInetAddress()  
                                    + "exit total: " + mList.size();  
                                socket.close();  
                                this.sendmsg();  
                                break;  
                               
                            } else {  
                            	// Server gets msg from client and sends back to client
                                msg = socket.getInetAddress() + ": " + msg;  
                                this.sendmsg();  
                            }  
                        }  
                    }  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
          /** 
           * Iterate client set，and send msgs to all clients 
           */  
           public void sendmsg() {  
               System.out.println(msg);  
               int num =mList.size();  
               for (int index = 0; index < num; index ++) {  
                   Socket mSocket = mList.get(index);  
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
        }      
}  