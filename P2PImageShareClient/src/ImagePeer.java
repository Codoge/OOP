import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * <i>ImagePeer</i> is class of peer in a hybrid P2P structure.
 * It gets resource from other peer and share resource it has to
 * other peer.
 * @version 1.0
 * @author
 * */
public class ImagePeer extends Thread{
	/** the id of peer client */
	int id = 1; // at least one (the server)
	
	/** user name */
	String name;
	
	/** data received in this peer, including image blocks and peer list */
	Data data;
	
	/** current image's name */
	String imgName;
	
	/** peer client's GUI instance */
	PeerClient client;
	
	/** upload server instance */
	UploadServer uploadServer;
	
	/**
	 * constructor: initialize data, GUI client and upload server
	 * */
	public ImagePeer() {
		data = new Data();
		data.init();
		client = new PeerClient();
		uploadServer = new UploadServer(this);
	}
	
	/**
	 * set block received and update the image
	 * @param x row number of block
	 * @param y column number of block
	 * @param pixels pixels array of block
	 * */
	public void setBlock(int x, int y, int[] pixels) {
		data.addBlocks(x, y, pixels);
		client.updateImage(20*x+y, pixels);
	}
	
	/** get a peer from peer list randomly */
	public String getOnePeer() {
		return data.getOnePeer();
	}
	
	public void run() {
		String user;
		String pwd;
		if ((user = client.getName()) == null)
			return;
		if ((pwd = client.getPwd()) == null)
			return;
		while (!loginCheck(user, pwd)) { // verify user and password
			client.showLoginFailed();
			if ((user = client.getName()) == null)
				return;
			if ((pwd = client.getPwd()) == null)
				return;
		}
		name = user;
		String serverIP = client.getServerAddress();
		if(!login(serverIP, ImageServer.PORT, pwd)) {
			if (id > ImageServer.count) client.showExceedMaxCount();
			return;
		}
		client.frame.setVisible(true);
		uploadServer.start();
		download();
	}

	/** download all blocks from arbitrary peers from peerlist */
	private void download() {
		// TODO Auto-generated method stub
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
				try {
					String peer = data.getOnePeer();
					while (peer.equals(uploadServer.getAddress())) {
						peer = data.getOnePeer();
					}
					String ip = Data.parseIP(peer);
					int port = Data.parsePort(peer);
					Socket s = new Socket(ip, port);
					Message msg = new Message(name, ip+":"+port, Message.GET_IMG_BLOCK);
					msg.setData_block_num(20*i+j);
					msg.setData_image_name(imgName);
					Downloader d = new Downloader(s, this, msg);
					d.start();
				} catch (EOFException e0) {
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	/** send a login request to the server and return its response
	 * @param serverIP IP address of server
	 * @param pwd password user typed in
	 * @return true login success, false login failed
	 * */
	private boolean login(String serverIP, int port, String pwd) {
		Socket s = null;
		boolean ret = false;
		try {
			s = new Socket(serverIP, port);
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			Message msg = new Message(name, serverIP + port, Message.LOGIN);
			msg.setData_content(pwd);
			out.reset();
			out.writeObject(msg);
			out.writeObject(null);
			Object obj = null;
        	while((obj = in.readObject()) == null);
        	Message reply = (Message) obj;
			String replyCommand = reply.getCommand();
			if (replyCommand.equals(Message.LOGIN_OK)) {
				imgName = reply.getData_image_name();
				List<String> peerList = (List<String>) reply.getPeer_list();
				data.setPeerList(peerList);
				id = (Integer) reply.getData_content(); 
				client.frame.setTitle("Peer #" + id);
				ret = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			try{
				if (s != null)
					s.close();
            }catch(IOException e2){
                System.out.println("Error in Client\n");
            }
		}
		return ret;
	}

	/**
	 * loginCheck verify user and password locally
	 * @param user user name typed
	 * @param pwd password typed
	 * @return true verify success, false verify failed
	 * */
	private boolean loginCheck(String user, String pwd) {
		// TODO Auto-generated method stub
		if (user.equals("admin") && pwd.equals("admin"))
			return true;
		return false;
	}

	/**
	 * deal with get image block request according to the data received
	 * @param sender message sender
	 * @param file_name image name of the request
	 * @param block_num block number of the request block
	 * @return response message
	 * */
	public Message makeSendMsg(String sender, String file_name, int block_num) {
		// TODO Auto-generated method stub
		if (!file_name.equals(imgName)) {
			Message reply = new Message(name, sender, Message.NAME_MISMATCH);
			reply.setPeer_list(data.getPeerList());
			return reply;
		}
		int x = block_num / 20;
		int y = block_num % 20;
		int[] pixels = data.getBlock(x, y);
		if (pixels != null) {
			Message reply = new Message(name, sender, Message.SEND_IMG_BLOCK);
			reply.setData_block_num(20*x+y);
			reply.setData_content(pixels);
			reply.setData_image_name(file_name);
			reply.setPeer_list(data.getPeerList());
			return reply;
		} else {
			Message reply = new Message(name, sender, Message.BLOCK_NOT_AVAILABLE);
			reply.setPeer_list(data.getPeerList());
			return reply;
		}
	}

	/** 
	 * show an update dialog, do update if user accept
	 * maintain origin image if user declined
	 * @param msg the update request received
	 * @return none
	 * */
	public void checkUpdate(Message msg) {
		// TODO Auto-generated method stub
		int ret = client.showUpdate();
		System.out.println("debug check update: " + ret);
		if (ret == 0) {
			data.init();
			imgName = msg.getData_image_name();
			download();
		}
	}

	/** 
	 * update peer list method
	 * @param peer_list new peer list received
	 * @return none
	 * */
	public void updatePeerList(List<String> peer_list) {
		String addr = uploadServer.getAddress();
		if (addr != null)
			peer_list.add(addr);
		data.setPeerList(peer_list);
	}

	public static void main(String[] args) {
		ImagePeer peer = new ImagePeer();
		peer.start();
	}

	/**
	 * make a registration when peer's upload server start
	 * @param peerStr peer's IP port string
	 * @return none
	 * */
	public void registrate(String peerStr) {
		// TODO Auto-generated method stub
		Socket s = null;
		try {
			s = new Socket(ImageServer.ip, ImageServer.PORT);
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			String receiver = ImageServer.ip +":"+ImageServer.PORT;
			Message msg = new Message(peerStr, receiver, Message.REGISTRATION);
			out.reset();
			out.writeObject(msg);
			out.writeObject(null);
			Object obj = null;
        	while((obj = in.readObject()) == null);
        	Message reply = (Message) obj;
			String replyCommand = reply.getCommand();
			if (replyCommand.equals(Message.REGISTRATION_OK)) {
				List<String> peerList = (List<String>) reply.getPeer_list();
				data.setPeerList(peerList);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			try{
				if (s != null)
					s.close();
            }catch(IOException e2){
                System.out.println("Error in registrate, socket close error\n");
            }
		}
	}
}
