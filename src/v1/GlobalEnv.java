package v1;
import java.sql.Connection;

public class GlobalEnv {
	
	public Connection currentConn = null; // current connection
	public String currentAlias = ""; // current alias
	
	public GlobalEnv() {
		
	}
}