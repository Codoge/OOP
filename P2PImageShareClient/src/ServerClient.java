import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * <i>ServerClient</i> is a class to implement all GUI logic of
 * a image server instance.
 * */
public class ServerClient {
	private JFrame frame;
	private JButton btn;
	private MyCanvas canvas;
	
	/** buffered image instance to store image blocks */
	private BufferedImage bi;
	
	/** server registered */
	private ImageServer server;
	
	public ServerClient(ImageServer server) {
		this.server = server;
		init();
	}

	/**
	 * initialization of a server GUI, including initialize JFrame instance
	 * ,self-defined canvas and choose image button.
	 * */
	public void init() {
		frame = new JFrame("Image Server");
		btn = new JButton("Load another image");
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
		contentPane.add(btn, "South");
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = getFilePath();
				if (path == null)
					return;
				try {
					File file = new File(path);
					server.peer.imgName = file.getName();
					bi = ImageIO.read(file);
					bi = canvas.zoom(bi);
					canvas.setImage(bi);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				canvas.repaint();
				server.init(bi);
				server.sendUpdate();
			}
		});
	}
	
	/** show client */
	public void show() {
		String path = getFilePath();
		if (path == null)
			return;
		try {
			File file = new File(path);
			server.peer.imgName = file.getName();
			bi = ImageIO.read(file);
			bi = canvas.zoom(bi);
			canvas.setImage(bi);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		canvas.repaint();
		server.init(bi);
		frame.setVisible(true);
	}
	
	/**
	 * show image choose dialog in initialization
	 * @return image file path
	 * */
	public String getFilePath() {
		JFileChooser jfc=new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.showDialog(new JLabel(), "Open");
		File file=jfc.getSelectedFile();
		if (file != null)
			return file.getAbsolutePath();
		return null;
	}
	
	/** return the buffered image instance */
	public BufferedImage getImage() {
		return bi;
	}
	
}
