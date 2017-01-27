package safepass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DB {
	private static final String url = "jdbc:sqlite:users.db";
	private Connection conn;
	private static final int queryTimeout = 10;
	
	public void insertRecord(int id, String name, String value) throws Exception {
		// Check that name and value not empty
		if(name.length()<1 || value.length()<1){
			throw new Exception("Empty Arguments Exception");
		}
		// NOTE: value is encrypted AES 128 salt
		String query = "INSERT INTO collection (id, name, password) "
				+ "VALUES ('"+ id +"', '" + name +"','"+ value +"')";
		connect(url);
		try{
			Statement st = conn.createStatement();
		    st.setQueryTimeout(queryTimeout);
		    st.executeUpdate(query);
		}catch(SQLException e1){
			e1.printStackTrace();
		}
		disconnect();
	}
	
	public void editRecord(String name, String value) throws Exception {
		// NOTE: Method does not indicate if name not present in DB
		// Check that name and value not empty
		if(name.length()<1 || value.length()<1){
			throw new Exception("Empty Arguments Exception");
		}
		
		String query = "UPDATE collection SET (name, password)"
				+ "= ('" + name +"','" + value +"') "
				+ "WHERE name = '"+name+"' ";
		connect(url);
		try{
			Statement st = this.conn.createStatement();
			st.setQueryTimeout(queryTimeout);
			st.executeUpdate(query);
		}catch(SQLException e1){
			e1.printStackTrace();
		}
		disconnect();
	}
	
	public void deleteRecord(String name) throws Exception { 
		// NOTE: Method doesn't indicate if name not present in DB
		// Check name not empty
		if(name.length()<1){
			throw new Exception("Empty Arguments Exception");
		}

		String query =  "DELETE FROM collection "
				+ "WHERE name='" + name +"'";
		connect(url);
		try{
			Statement st = this.conn.createStatement();
			st.setQueryTimeout(queryTimeout);
			st.executeUpdate(query);
		}catch(SQLException e1){
			e1.printStackTrace();
		}
		disconnect();
	}
	
	public String fetchRecord(String name) throws Exception { 
		// Check name not empty
		if(name.length()<1){
			throw new Exception("Empty Arguments Exception");
		}
		
		String result = null; 
		String query = "SELECT password FROM collection "
				+ "WHERE name='" + name +"'";
		connect(url);
		try{
			Statement st = this.conn.createStatement();
			st.setQueryTimeout(queryTimeout);
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				result = rs.getString("password");
			}	
		}catch(SQLException e1){
			e1.printStackTrace();
		}
		disconnect();
		return result;
	}
	
	public String[][] fetchAllRecords(){
		String query = "SELECT id,name,password  "
				+ "FROM collection";
		connect(url);
		ArrayList<ArrayList<String>> listResult = new ArrayList<>();
		try{
			Statement st = this.conn.createStatement();
			st.setQueryTimeout(queryTimeout);
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				listResult.add(new ArrayList<String>(){{
					add(rs.getString("id"));
					add(rs.getString("name"));
					add(rs.getString("password"));
				}});
			}	
		}catch(SQLException e1){
			e1.printStackTrace();
		}	
		
		disconnect();
		// Place ArrayList elements into String[] array
		String[][] finalResult = new String[listResult.size()][3];
		for(int i = 0; i<listResult.size(); i++){
			ArrayList<String> row = listResult.get(i);
			finalResult[i] = row.toArray(new String[3]);
		}
		return finalResult;	
	}
	
	public int nextAvailableId(){
		String query = "SELECT id FROM collection";
		int id = 0;
		connect(url);
		try{
			Statement st = this.conn.createStatement();
			st.setQueryTimeout(queryTimeout);
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				++id;
			}	
		}catch(SQLException e1){
			e1.printStackTrace();
		}	
		disconnect();
		id+=1; // id represents highest id, so returns the increment
		return id;
	}
	
	public int recordId(String name) throws Exception{
		// Check name not empty
		if(name.length()<1){
			throw new Exception("Empty Arguments Exception");
		}
		// Returns 0 if not exists
		String query = "SELECT id FROM collection "
				+ "WHERE name='" + name + "'";
		int id = 0;
		connect(url);
		try{
			Statement st = this.conn.createStatement();
			st.setQueryTimeout(queryTimeout);
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				id = Integer.parseInt(rs.getString("id"));
			}	
		}catch(SQLException e1){
			e1.printStackTrace();
		}	
		disconnect();
		return id;
	}
	
	
	private void connect(String url)  {
		try{
			Class.forName("org.sqlite.JDBC");
			this.conn = DriverManager.getConnection(url);
		}catch(ClassNotFoundException e1){
			e1.printStackTrace();
		}catch(SQLException e1){
			e1.printStackTrace();
		}
	}
	
	private void disconnect(){
		try{
			this.conn.close();
		}catch(SQLException e1){
			e1.printStackTrace();
		}
	}
	
}
