package v1;
import java.sql.Connection;

public class GlobalEnv {
	public Connection currentConnection = null; // current connection
	public String currentAlias = ""; // current alias
	public String currentConnectionName = "";
	public GlobalEnv() {
		
	}
}
