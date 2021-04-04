package v1;
import java.sql.*;

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
			Statement st = conn.createStatement();
			
			// execute the query, and get a java resultset
			ResultSet rs = st.executeQuery(query);
			
			// iterate through the java resultset
			while (rs.next()) {
				int id = rs.getInt("id");
				String firstName = rs.getString("first_name");
				System.out.println(id + ": " + firstName);
			}
			rs.close();
		} catch(Exception e) {
			System.out.println("MySQL Error: " + e);
		}
	}
}
