import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <i>ImageServer</i> is class of server in a hybrid P2P structure.
 *  It is used as the source of images as well as that of the list 
 *  of peers available. It can used for choose which Image to show 
 *  and check whether client (other peers) is still active.
 * @version 1.0
 * @author
 */
public class ImageServer extends Thread {
	/** fixed port for server handle client connection use. */
    public static final int PORT=8000;
    
    private static int id = 1;
    
    public static int count = 5;
    
    /** peer instance in server */
    ImagePeer peer;
    
    /** upload server instance */
	UploadServer uploadServer;
	
	/** GUI instance */
    ServerClient client;
    
    /** server's IP */
    static String ip;
    
    /** constructor */
    public ImageServer() {
    	peer = new ImagePeer();
    	client = new ServerClient(this);
    	uploadServer = new UploadServer(peer);
        try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public int getPeerId() {
    	return ++id;
    }
    
    /**
     * @return current peer list's length
     */
    public int getPeerCount() {
    	return peer.data.peerList.size();
    }
    
    /**
     * init make some initialization for data stored in the server
     * @param bi image instance to be share
     * @return none
     * */
    public void init(BufferedImage bi) {
    	int[][] hasBlock = new int[20][20];
    	int[][][] pixels = new int[20][20][];
    	for (int i = 0; i < 20; i++) {
    		for (int j= 0; j < 20; j++) {
    			hasBlock[i][j] = 1;
    			BufferedImage sub = bi.getSubimage(30*i, 30*j, 30, 30);
    			pixels[i][j] = new int[30*30];
    			pixels[i][j] = sub.getRGB(0, 0, 30, 30, pixels[i][j], 0, 30);
    			peer.data.addBlocks(i, j, pixels[i][j]);
    		}
    	}
    	peer.data.setHasBlock(hasBlock);
    }
    
    /** show GUI and start upload server thread */
    public void startUploader() {
    	client.show();
    	uploadServer.start();
    }
    
    /** start server socket for handling server socket */
    public void startConnector() throws IOException {
        ServerSocket ss = new ServerSocket(PORT);
        System.out.println("ConnectServer@"+ ip +":"+ PORT +" start!");
        try{
            while(true){
                Socket s = ss.accept();// listen PORT;
                try{
                    new ConnHandler(s, this);
                } catch (IOException e) {
                	System.out.println("Error: " + e);
                    s.close();
                } 
            }
        }finally{
            ss.close();
            System.out.println("Server stop!");
        }
    }
    
    /** call startConnector in run method */
    public void run() {
    	try {
			startConnector();
		} catch (IOException e) {
			System.out.println("Error: " + e);
		}
    }
    
    /** set a time scheduled executor for checking whether other peer is active */
    public void sendActiveMsg() {
    	Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				for (String p : peer.data.getPeerList()) {
					if (p.equals(uploadServer.getAddress()))
						continue;
					String pIP = Data.parseIP(p);
					int pPort = Data.parsePort(p);
					ObjectInputStream in;
					ObjectOutputStream out;
					Socket s = null;
					try {
						System.out.println("send active message");
						System.out.println("IP:"+pIP);
						System.out.println("Port:"+pPort);
						s = new Socket(pIP, pPort);
						in = new ObjectInputStream(s.getInputStream());
						out = new ObjectOutputStream(s.getOutputStream());
						Message msg = new Message(ip + ":" + ImageServer.PORT, p, Message.ACTIVE_CHECK);
						out.reset();
						out.writeObject(msg);
						out.writeObject(null);
						Object obj = null;
			        	while((obj = in.readObject()) == null);
			        	Message reply = (Message) obj;
						if (!reply.getCommand().equals(Message.ACTIVE_CHECK_OK))
							peer.data.remove(p);
					} catch (ConnectException e) {
						// TODO Auto-generated catch block
						peer.data.remove(p);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ClassNotFoundException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					} finally {
						if (s != null) {
							try {
								s.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		};
		ScheduledExecutorService service = Executors  
                .newSingleThreadScheduledExecutor();  
        // set a time scheduled executor 
        service.scheduleAtFixedRate(runnable, 10, 60, TimeUnit.SECONDS);
    }
    
    public static void main(String[] args) {
		ImageServer imageServer = new ImageServer();
		if (args.length > 0) {
			ImageServer.count = Integer.parseInt(args[0].substring(1));
		}
		System.out.println(ImageServer.count);
		imageServer.start();
		imageServer.startUploader();
		imageServer.sendActiveMsg();
	}

    /** send update signal to every peer in peer list except self */
	public void sendUpdate() {
		// TODO Auto-generated method stub
		for (String p : peer.data.getPeerList()) {
			if (p.equals(uploadServer.getAddress()))
				continue;
			String pIP = Data.parseIP(p);
			int pPort = Data.parsePort(p);
			ObjectInputStream in;
			ObjectOutputStream out;
			Socket s = null;
			try {
				System.out.println("send update:");
				System.out.println("IP:"+pIP);
				System.out.println("Port:"+pPort);
				s = new Socket(pIP, pPort);
				in = new ObjectInputStream(s.getInputStream());
				out = new ObjectOutputStream(s.getOutputStream());
				Message msg = new Message(ip + ":" + ImageServer.PORT, p, Message.IMG_UPDATE);
				msg.setData_image_name(peer.imgName);
				out.reset();
				out.writeObject(msg);
				out.writeObject(null);
			} catch (ConnectException e) {
				// TODO Auto-generated catch block
				peer.data.remove(p);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/** 
	 * @return true (user name, password) exist, false user name or password wrong
	 */
	public boolean loginCheck(String sender, String pwd) {
		// TODO Auto-generated method stub
		if (sender.equals("admin") && pwd.equals("admin"))
			return true;
		return false;
	}
}

/**
 * The {@code ConnHandler} class enclosure handling connection from peers
 * method and data its need
 * @version 1.0
 * @author
 * */
class ConnHandler extends Thread {
	/** connection socket */
    private Socket s;
    
    /** registered ImageServer instance */
    private ImageServer server;
    
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    /**
     * @throws get object output stream may cause {@link IOException}
     *  */
    public ConnHandler(Socket s, ImageServer server) throws IOException {
        this.s = s;
        this.server = server;
        out = new ObjectOutputStream(s.getOutputStream());
        in = new ObjectInputStream(s.getInputStream());
        start();
    }
    
    public void run(){
    	try {
            while(true) {
            	Object obj = null;
            	while((obj = in.readObject()) == null);
            	Message msg = (Message) obj;
            	String sender = msg.getSender();
            	String command = msg.getCommand();
            	if (command.equals(Message.LOGIN)) {
            		String pwd = (String) msg.getData_content();
            		Message reply;
            		int peerNo = server.getPeerId();
            		if (server.loginCheck(sender, pwd) && peerNo <= ImageServer.count) {
            			reply = new Message(ImageServer.ip + ":" + ImageServer.PORT, sender, Message.LOGIN_OK);
            			reply.setData_image_name(server.peer.imgName);
            			reply.setPeer_list(server.peer.data.getPeerList());
            			reply.setData_content(peerNo);
            		} else {
            			reply = new Message(ImageServer.ip + ":" + ImageServer.PORT, sender, Message.LOGIN_FAILED);
            		}
            		out.reset();
            		out.writeObject(reply);
            		out.writeObject(null);
            	} else if (command.equals(Message.REGISTRATION)) {
            		server.peer.data.addPeerList(sender);
            		Message reply = new Message(ImageServer.ip + ":" + ImageServer.PORT, sender, Message.REGISTRATION_OK);
            		reply.setPeer_list(server.peer.data.getPeerList());
            		out.reset();
            		out.writeObject(reply);
            		out.writeObject(null);
            	}
            }
        } catch (EOFException e0){
        	
        } catch (Exception e){
        	System.out.println("Error: " + e);
        } finally {
            try{
                s.close();
            }catch(IOException e){
            	e.printStackTrace();
            }
		}
	}
}