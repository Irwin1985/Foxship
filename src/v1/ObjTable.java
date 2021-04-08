package v1;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
	public Object findColumn(String columnName) {
		try {
			if (cursor != null) {
				return cursor.getString(columnName);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	public int reccount() {
		if (cursor != null) {
			try {
				int curRecno = cursor.getRow(); // save current row
				int cursorSize = 0;
				cursor.first();
				while (cursor.next()) {
					cursorSize++;
				}
				cursor.absolute(curRecno); // restore current row
				return cursorSize;
			} catch(Exception e) {
				System.out.println("SQL Error: " + e.getMessage());
				return -1;				
			}
		}
		return -1;
	}
	public boolean goRecno(int recno) {
		if (cursor != null) {
			try {
				int total = reccount();
				if (total < 0) {
					System.out.println("Invalid cursor: reccount < 0");
					return false;
				}
				if (recno <= 0 || recno > reccount()) {
					System.out.println("Record is out of range.");
					return false;
				}
				cursor.absolute(recno);
				return true;
			} catch(Exception e) {
				System.out.println("SQL Error: " + e.getMessage());
				return false;
			}
		}
		return false;
	}
	public boolean requery(Connection activeConn) {
		String query = "SELECT * FROM " + name;
		if (noData) {
			query += " WHERE 1 = 2 ";
		}
		else if (filter != null && !filter.isEmpty()) {
			query += " WHERE " + filter;
		}
		try {
			PreparedStatement prepareStmt = activeConn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			cursor = prepareStmt.executeQuery(query);
			cursor.next();
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
		return "true";
	}
}
