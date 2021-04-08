package v1;
import java.sql.Connection;
import java.util.HashMap;

public class GlobalEnv {
	public Connection currentConnection = null; // current connection
	public String currentAlias = ""; // current alias
	public String currentConnectionName = "";
	public HashMap<String, ObjTable> workArea;

	public GlobalEnv() {
		// cursor list
		workArea = new HashMap<String, ObjTable>();
	}
}
