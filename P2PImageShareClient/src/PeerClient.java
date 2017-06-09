import java.awt.Canvas;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * <i>PeerClient</i> is a class to implement all GUI logic of
 * a image peer instance.
 * */
public class PeerClient {
	JFrame frame;
	MyCanvas canvas;
	BufferedImage bi;
	
	public PeerClient() {
		init();
	}
	
	/**
	 * initialization of peer GUI, including initialize JFrame instance
	 * and self-defined canvas.
	 * */
	public void init() {
		frame = new JFrame("Image Peer #");
		frame.setLocationRelativeTo(null);
		canvas = new MyCanvas();
		canvas.setSize(600, 600);
		Container contentPane = frame.getContentPane();
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.add(canvas);
		contentPane.add(panel, "Center");
		contentPane.add(new JPanel(), "North");
		contentPane.add(new JPanel(), "West");
		contentPane.add(new JPanel(), "East");
		contentPane.add(new JPanel(), "South");
		bi = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
		canvas.setImage(bi);
		canvas.repaint();
		frame.add(canvas);
		frame.pack();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * do some update in image when received a new block
	 * */
	void updateImage(int blockNums, int[] pixels) {
		canvas.modifyImage(blockNums, pixels);
		canvas.setImage(bi);
		canvas.repaint();
	}
    
	/** ask for server address dialog */
    String getServerAddress() {
    	return JOptionPane.showInputDialog( // GUI for input IP
    			null, "Server IP address", "Input", JOptionPane.QUESTION_MESSAGE);
    }
    
    /** ask for user name dialog */
    String getName() {
    	return JOptionPane.showInputDialog( // GUI for input username
    			null, "Username", "Input", JOptionPane.QUESTION_MESSAGE);
    } 
    
    /** ask for user password dialog */
    String getPwd() {
    	return JOptionPane.showInputDialog( // GUI for input password
    			null, "Password", "Input", JOptionPane.QUESTION_MESSAGE);
    }
    
    /** show login failed hint dialog */
    void showLoginFailed() {
    	JOptionPane.showMessageDialog( // GUI for login failed
    			null, "Login Failed!", "Message", JOptionPane.ERROR_MESSAGE);
    }
    
    /** show hint dialog */
    void showExceedMaxCount() {
    	JOptionPane.showMessageDialog( // GUI for login failed
    			null, "Peer Nums Exceed Max Numbers!", "Message", JOptionPane.ERROR_MESSAGE);
    }
    
    /** ask user if accept update image dialog */
    int showUpdate() {
    	return JOptionPane.showConfirmDialog( // GUI for input password
    			null, "Update Graph?", "Select an option", JOptionPane.YES_NO_OPTION);
    }
    
    public static void main(String[] args) {
		PeerClient client = new PeerClient();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.setVisible(true);
		System.out.println("test for multithreads");
		
	}
}

/**
 * MyCanvas is a class of self-defined Canvas, which can scale 
 * a {@code BufferedImage} to fixed size. and it can update
 * image immediately.
 * */
class MyCanvas extends Canvas{
	/** buffered image instance to store image */
    private BufferedImage bi;
    /** fixed width and height */
    private int image_width = 600;
    private int image_height = 600;
    
    public void setImage(BufferedImage bi){
        this.bi = bi;
    }
    
    /**
     * update image immediately
     * @param blockNums block number of block received
     * @param pixels pixels array received
     * */
    public synchronized void modifyImage(int blockNums, int[] pixels) {
    	int x = blockNums/20;
    	int y = blockNums%20;
    	bi.setRGB(30*x, 30*y, 30, 30, pixels, 0, 30);
    }
    
    /** paint method */
    public void paint(Graphics g){
        g.drawImage(bi,0,0,this.getWidth(),this.getHeight(),this);
    }
    
    /**
     * zoom buffered image to given size
     * @param bi origin buffered image
     * @return buffered image after scaling
     * */
    public BufferedImage zoom(BufferedImage bi){
    	Image im = bi.getScaledInstance(image_width,image_height,Image.SCALE_SMOOTH);
        BufferedImage newBi = new BufferedImage(image_width, image_height, BufferedImage.TYPE_INT_RGB);
        Graphics g = newBi.getGraphics();
        g.drawImage(im, 0, 0, null);
        g.dispose();
        return newBi;
    }
}
