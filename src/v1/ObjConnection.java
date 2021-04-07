package v1;

import java.sql.Connection;
import java.sql.DriverManager;

public class ObjConnection extends Obj {
	public String conId;
	public EngineType engine;
	public String server;
	public String port;
	public String dataBase;
	public String user;
	public String password;
	// Connection Handle
	Connection conn = null;
	
	public ObjConnection() {}
	
	public boolean createConnection() {
		if (engine == EngineType.MYSQL) {
			try {				
				Class.forName(parseDriver());
				this.conn = DriverManager.getConnection(parseUrl(), user, password);
			} catch(Exception e) {
				System.out.println("MySQL Error: " + e.getMessage());
				return false;
			}
		}
		return true;
	}
	
	private String parseDriver() {
		if (engine == EngineType.MYSQL) {
			return "com.mysql.cj.jdbc.Driver";
		}
		return "";
	}
	public String engineToString() {
		if (engine == EngineType.MYSQL) {
			return "MySQL";
		}
		return "";
	}
	
	private String parseUrl() {
		if (engine == EngineType.MYSQL) {
			if (server.isEmpty()) {
				server = "localhost";
			}
			String url = "jdbc:mysql://" + server;
			if (!port.isEmpty()) {				
				url += ":3306";
			}

			if (!dataBase.isEmpty()) {				
				url += "/" + dataBase;
			}
			return url;
		}
		return "";		
	}
	
	
	public ObjType type() {
		return ObjType.CONN_OBJ;
	}
	
	public String inspect() {
		return "true";
	}
	
}
