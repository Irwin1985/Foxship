package v1;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BrowseWindow {

	public BrowseWindow() {}
	
	public BrowseWindow(ResultSet rs, String title) throws SQLException {
		
		JFrame browse = new JFrame();
		browse.setTitle(title);
		JTable table = new JTable(buildTableModel(rs));
		JScrollPane jScroll = new JScrollPane(table);
		
		browse.getContentPane().add(jScroll);		
		browse.pack();		
		//browse.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		browse.setLocationRelativeTo(null);
		browse.setVisible(true);

	}

	private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {

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

	    return new DefaultTableModel(data, columnNames) {
	    	@Override
	    	public boolean isCellEditable(int row, int column) {
	    		return false;
	    	}
	    };
	}	
}
