import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <i>UploadServer</i> is a class of server for sharing resource
 * to other peer. And it can handle with update request and active
 * check request.
 * @version 1.0
 * @author
 * */
public class UploadServer extends Thread {
	/** start port of upload server is 8001 */
	public static final int START_PORT=8001;
	
	/** image peer it belongs to */
	private ImagePeer peer;
	
	/** port to listen */
    private int port;
    
    /** IP address */
    private String ip;
    
    /**
     * constructor
     * @param peer which peer to registered
     *   */
    public UploadServer(ImagePeer peer) {
    	this.peer = peer;
    }
	
    /** get the upload server's IP and port */
    public String getAddress() {
    	if (ip != null)
    		return ip + ":" + port;
    	return null;
    }
    
    /** run method */
    public void run() {
        ServerSocket ss = null;
        port = START_PORT;
        while (ss == null && port < 65536) {
        	try {
        		ss = new ServerSocket(port);
        	} catch (IOException e) {
				port++;
			}
        }
        
        try{
        	ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("UploadServer@"+ ip +":"+ port +" start!");
            peer.data.addPeerList(ip +":"+ port);
            peer.registrate(ip +":"+ port);
            while(true){
                Socket s = ss.accept();// listen PORT
                try{
                    new UploadHandler(s, peer);
                } catch (IOException e) {
                	System.out.println("Error: " + e);
                    s.close();
                } 
            }
        } catch(IOException e1) {
        	e1.printStackTrace();
        }finally{
            try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
            System.out.println("Server stop!");
        }
    }
}

/**
 * <i>UploadHandler</i> is a class of handling each concrete request from
 * other peer.
 * @version 1.0
 * @author
 * */
class UploadHandler extends Thread {
	/** socket instance accept in upload server */
    private Socket s;
    
    /** object input/output stream */
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    /** image peer it belongs to */
    private ImagePeer peer;
    
    /**
     * registered socket and peer
     * @param s socket
     * @param peer image peer
     * */
    public UploadHandler(Socket s, ImagePeer peer) throws IOException {
        this.s = s;
        this.peer = peer;
        out = new ObjectOutputStream(s.getOutputStream());
        in = new ObjectInputStream(s.getInputStream());
        start();
    }
    
    /**
     * run method
     * */
    public void run(){
        try {
            while(true) {
            	Object obj = null;
            	while((obj = in.readObject()) == null);
            	Message msg = (Message) obj;
            	String sender = msg.getSender();
            	String command = msg.getCommand();
            	if (command.equals(Message.GET_IMG_BLOCK)) {
            		String file_name = msg.getData_image_name();
            		int block_num = msg.getData_block_num();
            		Message reply = peer.makeSendMsg(sender, file_name, block_num);
            		out.reset();
            		out.writeObject(reply);
            		out.writeObject(null);
            	} else if(command.equals(Message.IMG_UPDATE)) {
            		peer.checkUpdate(msg);
            	} else if(command.equals(Message.ACTIVE_CHECK)) {
            		Message reply = new Message(msg.getReceiver(), sender, Message.ACTIVE_CHECK_OK);
            		out.reset();
            		out.writeObject(reply);
            		out.writeObject(null);
            	}
            }
        } catch (EOFException e0){
        	
        } catch (Exception e){
        	e.printStackTrace();
        } finally {
            try{
                s.close();
            }catch(IOException e){
            	e.printStackTrace();
            }
        }
    }
}

/**
 * <i>Downloader</i> is a class of downloading all image blocks 
 * in peer initialization.
 * @version 1.0
 * @author 
 * */

class Downloader extends Thread {
	/** socket */
	private Socket s;
	private ObjectInputStream in;
    private ObjectOutputStream out;
    private ImagePeer peer;
    
    /** Message it ready to send */
    private Message msg;
    
    /**
     * constructor
     * @param msg message ready to send
     * @throws get object input/output stream may cause {@code IOException}
     * */
    public Downloader(Socket s, ImagePeer peer, Message msg) throws IOException {
    	
    	this.peer = peer;
    	this.s = s;
    	this.msg = msg;
    	in = new ObjectInputStream(s.getInputStream());
        out = new ObjectOutputStream(s.getOutputStream());
    }
    
    /** run method */
    public void run(){
        try {
        	out.reset();
        	out.writeObject(msg);
        	out.writeObject(null);
        	Object obj = null;
        	while((obj = in.readObject()) == null);
        	Message reply = (Message) obj;
        	if (reply.getCommand().equals(Message.SEND_IMG_BLOCK)) {
        		int blockNums = reply.getData_block_num();
        		int[] pixels = (int[]) reply.getData_content();
        		peer.setBlock(blockNums/20, blockNums%20, pixels);
        		peer.updatePeerList(reply.getPeer_list());
        	} else if (reply.getCommand().equals(Message.BLOCK_NOT_AVAILABLE) || reply.getCommand().equals(Message.NAME_MISMATCH)) {
        		peer.updatePeerList(reply.getPeer_list());
        		System.out.println("info: " + reply.getCommand());
        		String peerStr = peer.getOnePeer();
        		String ip = Data.parseIP(peerStr);
        		int port = Data.parsePort(peerStr);
        		Socket newSocket = null;
        		try {
        			System.out.println(peerStr);
        			newSocket = new Socket(ip, port);
        			Message newMsg = new Message(reply.getReceiver(), ip+":"+port, Message.GET_IMG_BLOCK);
        			newMsg.setData_block_num(msg.getData_block_num());
        			newMsg.setData_image_name(msg.getData_image_name());
        			Downloader d = new Downloader(newSocket, peer, newMsg);
        			d.start();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
        } catch (EOFException e0){
        	
        } catch (Exception e){
        	e.printStackTrace();
        } finally {
            try{
                s.close();
            }catch(IOException e){
//            	System.out.println("Error: " + e);
            	e.printStackTrace();
            }
        }
    }
}
