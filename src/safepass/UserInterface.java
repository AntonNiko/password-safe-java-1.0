package safepass;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class UserInterface extends JFrame {
	private static DefaultTableModel passwordTableModel = null;
	private static JTable passwordTable = null;
	private static final String[] passwordTableColNames = {"ID", "Name", "Password"};
	
	public UserInterface(){
		super("User Interface");
		prepareUI();
	}
	
	public static void main(String[] args){	
		/*
		AES e = new AES();
		DB d = new DB();
		try{
			e.encryptAppPass("forfeet", 1);
			d.insertRecord(1, "forfeet", e.getEncryptedAppPass());
			
			e.encryptAppPass("bully", 2);
			d.insertRecord(2, "bully", e.getEncryptedAppPass());
			
			e.encryptAppPass("twenty", 3);
			d.insertRecord(3, "twenty", e.getEncryptedAppPass());
		}catch(Exception e1){
			
		}
		*/
		if(UserAuth.login(loginUserDialog())){
			UserInterface ui = new UserInterface();			
		}else{
			failedLoginDialog();
		}
	}
		
	private void prepareUI(){
		setSize(400,400);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// Set up main table
		String[][] passwords = getPasswords(true);
		passwordTableModel = new DefaultTableModel(passwords, passwordTableColNames);
		passwordTable = new JTable(passwordTableModel);
		JScrollPane passwordTablePane  = new JScrollPane(passwordTable);
		passwordTable.setFillsViewportHeight(true);
		
		// Header Pane (top section)
		add(getHeader(), BorderLayout.PAGE_START);
		// Left Padding Space
		add(new JPanel(), BorderLayout.LINE_START);
		// Password Table Pane
		add(passwordTablePane, BorderLayout.CENTER);
		// Right Padding Space
		add(new JPanel(), BorderLayout.LINE_END);		
		// Footer Pane (bottom section)
		add(new JPanel(), BorderLayout.PAGE_END);
		setVisible(true);
	}
	
	private static JPanel getHeader(){
		JPanel header = new JPanel();
		header.setLayout(new BorderLayout());
		
		// Left Header
		JPanel leftHeader = new JPanel();
		leftHeader.setLayout(new BorderLayout());
		JButton addPasswordButton = new JButton("Add");
		addPasswordButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				// TODO 
				String passwordName = addPasswordNameDialog();
				String passwordValue = addPasswordValueDialog();
				try{
					addPassword(passwordName, passwordValue);
				}catch(Exception e1){
					e1.printStackTrace();
				}
				
				// Repaint JTable
				DefaultTableModel passwordTableModel = new DefaultTableModel(getPasswords(true), passwordTableColNames);
				passwordTable.setModel(passwordTableModel);
			}
		});
		leftHeader.add(addPasswordButton, BorderLayout.LINE_START);
		
		JButton editPasswordButton = new JButton("Edit");
		editPasswordButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String passwordName = editPasswordNameDialog();
				String newPasswordValue = editPasswordValueDialog();
				try{
					editPassword(passwordName, newPasswordValue);
				}catch(Exception e1){
					e1.printStackTrace();
				}
				
				// Repaint JTable
				DefaultTableModel passwordTableModel = new DefaultTableModel(getPasswords(true), passwordTableColNames);
				passwordTable.setModel(passwordTableModel);
			}
		});
		leftHeader.add(editPasswordButton, BorderLayout.CENTER);
		
		JButton deletePasswordButton = new JButton("Delete");
		deletePasswordButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String passwordName = deletePasswordDialog();
				try{
					deletePassword(passwordName);
				}catch(Exception e1){
					e1.printStackTrace();
				}
				
				// Repaint JTable
				DefaultTableModel passwordTableModel = new DefaultTableModel(getPasswords(true), passwordTableColNames);
				passwordTable.setModel(passwordTableModel);
			}
		});
		leftHeader.add(deletePasswordButton, BorderLayout.LINE_END);
		header.add(leftHeader, BorderLayout.LINE_START);
		
		// Right Header
		JPanel rightHeader = new JPanel();
		rightHeader.setLayout(new BorderLayout());
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				// Repaint JTable
				DefaultTableModel passwordTableModel = new DefaultTableModel(getPasswords(true), passwordTableColNames);
				passwordTable.setModel(passwordTableModel);
			}
		});
		rightHeader.add(refreshButton, BorderLayout.LINE_END);
		header.add(rightHeader, BorderLayout.LINE_END);
		
		header.setBorder(new EmptyBorder(10, 10, 10, 10));
		return header;
	}
	
	private static String[][] getPasswords(boolean decrypted){
		DB db = new DB();
		AES e = new AES();
		// Fetch all records
		String[][] passwordArray = db.fetchAllRecords();
		// Decrypt all entries in the record and replace entries
		if(decrypted){
			for(int i = 0; i<passwordArray.length; i++){
				e.decryptAppPass(passwordArray[i][2], (i+1));
				passwordArray[i][2] = e.getDecryptedAppPass();
			}			
		}
		return passwordArray;
	}
	
	public static void logEvent(){
		
	}
	
	public static void authenticate(){	
		
	}
	
	public static void addPassword(String name, String password) throws Exception{
		// TODO check args not empty
		if(name.length()<1 || password.length()<1){
			throw new Exception("Empty Argument Exception");
		}
		
		AES e = new AES();
		DB db = new DB();
		System.out.println("Next available: "+db.nextAvailableId());
		e.encryptAppPass(password, db.nextAvailableId());
		String encryptedPassword = e.getEncryptedAppPass();
		try{
			db.insertRecord(db.nextAvailableId(), name, encryptedPassword);
		}catch(Exception e1){
			e1.printStackTrace();
		}
	}
	
	public static void editPassword(String name, String password) throws Exception{
		if(name.length()<1 || password.length()<1){
			throw new Exception("Empty Argument Exception");
		}
	
		AES e = new AES();
		DB db = new DB();
		
		e.encryptAppPass(password, db.recordId(name));
		String encryptedPassword = e.getEncryptedAppPass();
		try{
			db.editRecord(name, encryptedPassword);
		}catch(Exception e1){
			e1.printStackTrace();
		}
	}
	
	public static void deletePassword(String name) throws Exception{
		if(name.length()<1){
			throw new Exception("Empty Argument Exception");
		}
		AES e = new AES();
		DB db = new DB();
		
		try{
			e.deleteSecretDecodeKey(db.recordId(name));
			db.deleteRecord(name);
		}catch(Exception e1){
	     	e1.printStackTrace();
		}
	}
	
	private static String addPasswordNameDialog(){
		String passwordName = JOptionPane.showInputDialog(null, 
				"New password name:", 
				"New Password Name", 
				JOptionPane.PLAIN_MESSAGE);
		return passwordName;
	}
	
	private static String addPasswordValueDialog(){
		String passwordValue = JOptionPane.showInputDialog(null,
				"New password value:",
				"New Password Value",
				JOptionPane.PLAIN_MESSAGE);
		return passwordValue;
	}
	
	private static String editPasswordNameDialog(){
		String passwordName = JOptionPane.showInputDialog(null,
				"Password name:",
				"Password Name",
				JOptionPane.PLAIN_MESSAGE);
		return passwordName;		
	}
	
	private static String editPasswordValueDialog(){
		String newPasswordValue = JOptionPane.showInputDialog(null,
				"New password value:",
				"Password Value",
				JOptionPane.PLAIN_MESSAGE);
		return newPasswordValue;		
	}
	
	private static String deletePasswordDialog(){
		String passwordName = JOptionPane.showInputDialog(null,
				"Password name:",
				"Delete Password Name",
				JOptionPane.PLAIN_MESSAGE);
		return passwordName;
	}
	
	private static String loginUserDialog(){
		String userPassword = JOptionPane.showInputDialog(null,
				"Login:",
				"User Authentication",
				JOptionPane.PLAIN_MESSAGE);
		return userPassword;		
	}
	
	private static void failedLoginDialog(){
		JOptionPane.showMessageDialog(null,
				"Wrong password.",
				"Authentication Fail", 
				JOptionPane.ERROR_MESSAGE);		
	}
}
