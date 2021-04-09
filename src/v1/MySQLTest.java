package v1;
import java.sql.*;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class MySQLTest {
	public static void main(String[] args) {
		try {
			// Create the connection
			String myDriver = "com.mysql.cj.jdbc.Driver";
			String myUrl = "jdbc:mysql://localhost:3306/test";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(myUrl, "root", "1234");
			// Query
			String query = "SELECT * FROM users";
			
			// create the java statement
			PreparedStatement prepareStmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			
			// execute the query, and get a java resultset
			ResultSet rs = prepareStmt.executeQuery(query);
			String action = "update";
			if (action.equals("insert")) {				
				// SAVE RECNO AND APPEND BLANK
				rs.moveToInsertRow();
				// REPLACE
				rs.updateString("first_name", "Pepe");
				rs.updateString("last_name", "Aguilar");
				// TABLEUPDATE
				rs.insertRow();
				
				// GO RECNO
				rs.moveToCurrentRow();
			}
			else if (action.equals("update")) {
				// move to row 6
				rs.absolute(6); 
				// REPLACE ...
				//rs.updateString("first_name", "Juliana");
				rs.updateObject("first_name", "Peter");
				rs.updateRow();
				
				rs.absolute(7);
				//rs.updateString("first_name", "Mariana");
				rs.updateObject("first_name", "Jacinta");
				
				// TABLE UPDATE
				rs.updateRow();
			}
			else if (action.equals("delete")) {
				// delete the last row
				rs.last();
				// detele
				rs.deleteRow();
			}
			System.out.println("policia listo.");
			//JTable table = new JTable(buildTableModel(rs));
			//JTable table = new ResultSetTable(rs);
			
			//BrowseWindow browse = new BrowseWindow();
			//browse.show(rs, "policia browse");
			
			// iterate through the java resultset
			/*
			while (rs.next()) {
				int id = rs.getInt("id");
				String firstName = rs.getString("first_name");
				System.out.println(id + ": " + firstName);
			}
			*/
			//JOptionPane.showMessageDialog(null, new JScrollPane(table));
			
			//rs.close();
		} catch(Exception e) {
			System.out.println("MySQL Error: " + e);
		}
	}
	public static DefaultTableModel buildTableModel(ResultSet rs)
	        throws SQLException {

	    ResultSetMetaData metaData = rs.getMetaData();

	    // names of columns
	    Vector<String> columnNames = new Vector<String>();
	    int columnCount = metaData.getColumnCount();
	    for (int column = 1; column <= columnCount; column++) {
	        columnNames.add(metaData.getColumnName(column));
	    }

	    // data of the table
	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    while (rs.next()) {
	        Vector<Object> vector = new Vector<Object>();
	        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
	            vector.add(rs.getObject(columnIndex));
	        }
	        data.add(vector);
	    }

	    return new DefaultTableModel(data, columnNames);

	}
}
