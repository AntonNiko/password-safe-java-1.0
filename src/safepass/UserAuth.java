package safepass;

import java.io.*;
import java.util.ArrayList;

public class UserAuth {
	public static final String publicKey = "hahaboggy69420";
	public static final String encryptedPassFile = "EncryptedPass";
	
	public static boolean login(String inputPass){
		AES aes = new AES();
		aes.encryptAuthPass(inputPass, publicKey);
		if(validPassword(aes.getEncryptedAuthPass())){
			return true;
		}
		return false;
	}
	
	public static void setPassword(String inputPass){
		AES aes = new AES();
		aes.encryptAuthPass(inputPass, publicKey);
		storeEncryptedPass(aes.getEncryptedAuthPass());
	}
	
	private static boolean validPassword(String encryptedPass){
		if(encryptedPass.equals(fetchEncryptedPass())){
			return true;
		}
		return false;
	}
	
	private static void storeEncryptedPass(String encryptedPass){
		try{
			PrintWriter pWriter = new PrintWriter(encryptedPassFile);
			pWriter.write(encryptedPass);
			pWriter.close();
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
		}
	}
	
	private static String fetchEncryptedPass(){
		String encryptedPass = null;
		String line;
		try{
			FileReader reader = new FileReader(new File(encryptedPassFile));
			BufferedReader buffReader = new BufferedReader(reader);
			
			while((line = buffReader.readLine()) != null){
				encryptedPass = line;
			}
			reader.close();
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
		}catch(IOException e2){
			e2.printStackTrace();
		}
		return encryptedPass;
	}
}
