import java.util.*;

/**
 * <i>Data</i> is entity class of peer stored data, which need be
 * synchronized when multithread read/write it.
 * */
public class Data {
	/** peer list stored */
	List<String> peerList;
	
	/** blocks data of received image */
	int[][][] blocks;
	
	/** flag refers whether the position has block data */
	int[][] hasBlock;
	
	public Data() {
		peerList = new ArrayList<String>();
		blocks= new int[20][20][];
	}
	
	/** initialize all position in hasBlock to 0 */
	public synchronized void init() {
		 hasBlock = new int[20][20]; // initialize
	}
	
	public int[][] getHasBlock() {
		return hasBlock;
	}

	/** setter method of hasBlock */
	public void setHasBlock(int[][] hasBlock) {
		this.hasBlock = hasBlock;
	}
	
	public synchronized List<String> getPeerList() {
		return peerList;
	}
	
	/** setter method of peerList */
	public synchronized void setPeerList(List<String> peerList) {
		// remove duplicate peer in the list
		this.peerList = new ArrayList<String>(new HashSet<String>(peerList));
	}
	
	/** add single peer to the peer list */
	public synchronized void addPeerList(String newPeer) {
		peerList.add(newPeer);
		this.peerList = new ArrayList<String>(new HashSet<String>(peerList));
	}
	
	/** parse IP from peer string */
	public static String parseIP(String peer) {
		return peer.split(":")[0];
	}
	/** parse port from peer string */
	public static int parsePort(String peer) {
		return Integer.parseInt(peer.split(":")[1]);
	}
	
	public synchronized int[][][] getBlocks() {
		return blocks;
	}
	
	/** 
	 * get block from the data instance
	 * @param x row number of the block
	 * @param y column number of the block
	 * @return pixels array if it has request block
	 * */
	public synchronized int[] getBlock(int x, int y) {
		if (hasBlock[x][y] != 1)
			return null;
		return blocks[x][y];
	}
	
	/** return one peer from the peer list randomly */
	public synchronized String getOnePeer() {
		Random r = new Random();
		return peerList.get(r.nextInt(peerList.size()));
	}
	
	/** add image block to data instance */
	public synchronized void addBlocks(int x, int y, int[] blocks) {
		this.blocks[x][y] = blocks;
		hasBlock[x][y] = 1;
	}

	public synchronized void remove(String p) {
		peerList.remove(p);
	}
}
