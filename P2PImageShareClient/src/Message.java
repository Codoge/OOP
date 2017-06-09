import java.io.Serializable;
import java.util.List;

/**
 * data structure of communication between peer to peer(server)
 */
public class Message implements Serializable{
	private static final long serialVersionUID = -1298221469160590748L;
	
	/** command of message */
	public static String LOGIN = "login";
	public static String LOGIN_OK = "login_ok";
	public static String LOGIN_FAILED = "login_failed";
	public static String GET_IMG_BLOCK = "get_img_block";
	public static String SEND_IMG_BLOCK = "send_img_block";
	public static String BLOCK_NOT_AVAILABLE = "block_not_available";
	public static String NAME_MISMATCH = "name_mismatch";
	public static String IMG_UPDATE = "img_update";
	public static String IMG_UPDATE_OK = "img_update_ok";
	public static String ACTIVE_CHECK = "active_check";
	public static String ACTIVE_CHECK_OK = "active_check_ok";
	public static String REGISTRATION = "registration";
	public static String REGISTRATION_OK = "registration_ok";
	
	/** message sender */
	private String sender;
	
	/** message receiver */
	private String receiver;
	
	/** command in the message */
	private String command;
	
	/** request image name */
	private String data_image_name;
	
	/** request block number */
	private int data_block_num;
	
	/** request data */
	private Object data_content;
	
	private List<String> peer_list;
	
	public Message(String sender, String receiver, String command) {
		this.sender = sender;
		this.receiver = receiver;
		this.command = command;
	}
	
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getData_image_name() {
		return data_image_name;
	}
	public void setData_content(Object data_content) {
		this.data_content = data_content;
	}
	public Object getData_content() {
		return data_content;
	}
	public void setData_image_name(String data_image_name) {
		this.data_image_name = data_image_name;
	}
	public int getData_block_num() {
		return data_block_num;
	}
	public void setData_block_num(int data_block_num) {
		this.data_block_num = data_block_num;
	}
	public List<String> getPeer_list() {
		return peer_list;
	}
	public void setPeer_list(List<String> peer_list) {
		this.peer_list = peer_list;
	}
}
