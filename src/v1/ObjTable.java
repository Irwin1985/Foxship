package v1;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Statement;

public class ObjTable extends Obj {
	public String name;
	public String alias;
	public boolean noData;
	public boolean noUpdate;
	public String filter;
	public ResultSet cursor;
	
	public ObjTable() {
		// nothing
	}
	public boolean goRecno(int recno) {
		if (cursor != null) {
			try {				
				cursor.absolute(recno);
				return true;
			} catch(Exception e) {
				System.out.println("SQL Error: " + e.getMessage());
				return false;
			}
		}
		return false;
	}
	public boolean openTable(Connection activeConn) {
		String query = "SELECT * FROM " + name;
		if (noData) {
			query += " WHERE 1 = 2 ";
		}
		else if (!filter.isEmpty()) {
			query += " WHERE " + filter;
		}
		try {
			Statement st = activeConn.createStatement();
			cursor = st.executeQuery(query);
			return true;
		} catch (Exception e) {
			System.out.println("SQL Error: " + e.getMessage());
		}
		return false;
	}
	
	public ObjType type() {
		return ObjType.TABLE_OBJ;
	}
	public String inspect() {
		return "ok";
	}
}
