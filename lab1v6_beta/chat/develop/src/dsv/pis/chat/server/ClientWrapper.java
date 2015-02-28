package dsv.pis.chat.server;

public class ClientWrapper {
	private String username;
	private long connectionTime;
	
	public ClientWrapper(String username, long connectionTime) {
		super();
		this.username = username;
		this.connectionTime = connectionTime;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public long getConnectionTime() {
		return connectionTime;
	}

	public void setConnectionTime(long connectionTime) {
		this.connectionTime = connectionTime;
	}
}
