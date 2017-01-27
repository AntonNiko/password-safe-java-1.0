package safepass;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class AES {
	private static SecretKeySpec secretAppKey;
	private static SecretKeySpec secretEncodeKey;
	
	private static String decryptedAppPass;
	private static String encryptedAppPass;
	private static String encryptedAuthPass;
	private static final String appKeyFile = "AppKey";
	private static final String appDecodeFile = "AppDecode";
	private static final String cipherTransform = "AES/ECB/PKCS5PADDING";
	
	public void setAuthKey(String publicKey){
		// Method to be used only for encrypting Passwords
		MessageDigest sha = null;
		try {
			byte[] keyAuth = publicKey.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			keyAuth = sha.digest(keyAuth);
			keyAuth = Arrays.copyOf(keyAuth, 16);
			secretAppKey = new SecretKeySpec(keyAuth, "AES");
			storeSecretAppKey(secretAppKey);
		}catch(NoSuchAlgorithmException e1){
			e1.printStackTrace();
		}catch(UnsupportedEncodingException e2){
			e2.printStackTrace();
		}
	}
	
	private void setAppKey(String secretAuthKey, int id){
		// Method to be used only for encrypting Passwords
		MessageDigest sha = null;
		try {
			byte[] keyAuth = secretAuthKey.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			keyAuth = sha.digest(keyAuth);
			keyAuth = Arrays.copyOf(keyAuth, 16);
			secretEncodeKey = new SecretKeySpec(keyAuth, "AES");
			// TODO Store in file
			storeSecretDecodeKey(secretEncodeKey, id);
		}catch(NoSuchAlgorithmException e1){
			e1.printStackTrace();
		}catch(UnsupportedEncodingException e2){
			e2.printStackTrace();
		}	
	}
	
	private String fetchSecretAppKey(){
		String result = null;
		StringBuilder fileData = new StringBuilder(1000);
		try{
			FileReader reader = new FileReader(new File(appKeyFile));
			//BufferedReader buffReader = new BufferedReader(reader);
			char[] buffer = new char[1000];
			int numRead = 0;
			while((numRead = reader.read(buffer)) != -1){
				String readData = String.valueOf(buffer, 0, numRead);
				fileData.append(readData);
				buffer = new char[1024];
			}
			reader.close();
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
		}catch(IOException e2){
			e2.printStackTrace();
		}
		result = fileData.toString();
		byte[] encoded = null;
		try{
			encoded = Hex.decodeHex(result.toCharArray());
		}catch(DecoderException e){
			e.printStackTrace();
		}
		return encoded.toString();	
	}
	
	private void storeSecretAppKey(SecretKeySpec key){
		FileWriter writer = null;
		BufferedWriter buffWriter = null;
		char[] hex = Hex.encodeHex(key.getEncoded());
		try {
			writer = new FileWriter(new File(appKeyFile), false);
			buffWriter = new BufferedWriter(writer);
			buffWriter.write(hex);
		}catch (IOException e1){
			e1.printStackTrace();
		}finally {
			try{
				if(buffWriter != null){
					buffWriter.close();
				}
				if(writer != null){
					writer.close();
				}
				}catch(IOException e1){
					e1.printStackTrace();
			}
		}
	}
	
	private void storeSecretDecodeKey(SecretKeySpec key, int id){
		// Read file
		ArrayList<ArrayList<ArrayList<Character>>> listDecodeKeys = new ArrayList<>();
		try{
			FileReader reader = new FileReader(new File(appDecodeFile));
			BufferedReader buffReader = new BufferedReader(reader);
			String line;
			while((line = buffReader.readLine()) != null){
				// Split between tab, store in ArrayList
			    String[] splitLine = line.split("\t");
			    
			    ArrayList<ArrayList<Character>> row = new ArrayList<>();
			    
			    ArrayList<Character> index = new ArrayList<Character>(){{
			    	add(splitLine[0].charAt(0));  // TODO Fix Second Line presence
			    }};
			    ArrayList<Character> values = new ArrayList<Character>(){{
				    for(int i = 0; i<splitLine[1].length(); i++){
				    	add(splitLine[1].charAt(i));
				    }			    	
			    }};
			    row.add(index);
			    row.add(values);
			    listDecodeKeys.add(row);
			}
			reader.close();
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
		}catch(IOException e2){
			e2.printStackTrace();
		}
		
		// Check for similar IDs, and overwrite file
		FileWriter writer = null;
		BufferedWriter buffWriter = null;
		char[] hex = Hex.encodeHex(key.getEncoded());
		// Convert hex into writable string
		String writeHex = "";
		for(char ch: hex){
			writeHex+=String.valueOf(ch);
		}
		
		boolean indexExists = false;
		try {
			writer = new FileWriter(new File(appDecodeFile), false);
			buffWriter = new BufferedWriter(writer);
			for(int i = 0; i<listDecodeKeys.size(); i++){
				ArrayList<ArrayList<Character>> row = listDecodeKeys.get(i);
				if(Character.getNumericValue(row.get(0).get(0)) == id){
					indexExists = true;
					buffWriter.write(id+"\t"+writeHex+"\n");
				}else{
					// ArrayList to String
					String value = "";
					for(int j = 0; j<row.get(1).size(); j++){
						value+=row.get(1).get(j).toString();
					}
					System.out.println("2: "+row.get(0).get(0)+"\t"+value+"\n");
					buffWriter.write(row.get(0).get(0)+"\t"+value+"\n");					
				}
			}
		}catch(IOException e1){
			e1.printStackTrace();
		}catch(IndexOutOfBoundsException e2){
			e2.printStackTrace();
		}finally {
			try{
				if(!indexExists){
					System.out.println("3: "+id+"\t"+writeHex+"\n");
					buffWriter.write(id+"\t"+writeHex+"\n");
				}
				if(buffWriter != null){
					buffWriter.close();
				}
				if(writer != null){
					writer.close();
				}
				}catch(IOException e1){
					e1.printStackTrace();
			}
		}		
	}

	private String fetchSecretDecodeKey(int id){
		try{
			FileReader reader = new FileReader(new File(appDecodeFile));
			BufferedReader buffReader = new BufferedReader(reader);
			String line;
			while((line = buffReader.readLine()) != null){
				// Split between tab, if id in line, return
			    String[] splitLine = line.split("\t");
			    if(Integer.parseInt(splitLine[0]) == id){
			    	return splitLine[1];
			    }
			}
			buffReader.close();
			reader.close();
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
		}catch(IOException e2){
			e2.printStackTrace();
		}
		return null;
	}
	
	public void deleteSecretDecodeKey(int id){
		// Read file
		ArrayList<ArrayList<ArrayList<Character>>> listDecodeKeys = new ArrayList<>();
		try{
			FileReader reader = new FileReader(new File(appDecodeFile));
			BufferedReader buffReader = new BufferedReader(reader);
			String line;
			while((line = buffReader.readLine()) != null){
				// Split between tab, store in ArrayList
			    String[] splitLine = line.split("\t");
			    if(Character.getNumericValue(splitLine[0].charAt(0)) != id){
				    ArrayList<ArrayList<Character>> row = new ArrayList<>();
				    ArrayList<Character> index = new ArrayList<Character>(){{
				    	add(splitLine[0].charAt(0)); // TODO Fix Second Line presence
				    }};
				    ArrayList<Character> values = new ArrayList<>();
				    for(int i = 0; i<splitLine[1].length(); i++){
				    	values.add(splitLine[1].charAt(i));
				    }
				    row.add(index);
				    row.add(values);
				    listDecodeKeys.add(row);
			    }
			}
			reader.close();
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
		}catch(IOException e2){
			e2.printStackTrace();
		}
		
		try{
			PrintWriter pWriter = new PrintWriter(appDecodeFile);
			for(int i = 0; i<listDecodeKeys.size(); i++){
				ArrayList<ArrayList<Character>> row = listDecodeKeys.get(i);
				// ArrayList of encrypted passwords to String
				String value = "";
				for(int j = 0; j<row.get(1).size(); j++){
					value+=row.get(1).get(j).toString();
				}
				System.out.println(row.get(0).get(0)+"\t"+value+"\n");
				pWriter.write(row.get(0).get(0)+"\t"+value+"\n");					
			}
			pWriter.close();
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
		}
	}
	
	public String getEncryptedAuthPass(){
		return encryptedAuthPass;
	}
	
	public void setEncryptedAuthPass(String encAuthPass){
		encryptedAuthPass = encAuthPass; 
	}
	
	public String getEncryptedAppPass(){
		return encryptedAppPass;
	}
	
	public void setEncryptedAppPass(String encryptedString){
		encryptedAppPass = encryptedString;
	}
	
	public String getDecryptedAppPass(){
		return decryptedAppPass;
	}
	
	public void setDecryptedAppPass(String decryptedPass){
		AES.decryptedAppPass = decryptedPass;
	}
	
	public String getSecretEncodeKeyRepresenation(){
		return secretEncodeKey.toString();
	}
	
	public void encryptAppPass(String unencryptedPass, int id){
		setAppKey(fetchSecretAppKey(), id);
		try{
			Cipher cipher = Cipher.getInstance(cipherTransform);
			cipher.init(Cipher.ENCRYPT_MODE, secretEncodeKey);
		    setEncryptedAppPass(Base64.encodeBase64String(cipher.doFinal(unencryptedPass.getBytes("UTF-8"))));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void encryptAuthPass(String inputPassword, String publicKey){
		setAuthKey(publicKey);
		try{
			Cipher cipher = Cipher.getInstance(cipherTransform);
			cipher.init(Cipher.ENCRYPT_MODE, secretAppKey);
		    setEncryptedAuthPass(Base64.encodeBase64String(cipher.doFinal(inputPassword.getBytes("UTF-8"))));
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	public void decryptAppPass(String encryptedPass, int id){
		try{
			byte[] encoded = Hex.decodeHex(fetchSecretDecodeKey(id).toCharArray());
			SecretKeySpec key = new SecretKeySpec(encoded, 0, encoded.length, "AES");
			Cipher cipher = Cipher.getInstance(cipherTransform);
			cipher.init(Cipher.DECRYPT_MODE, key);
		    setDecryptedAppPass(new String(cipher.doFinal(Base64.decodeBase64(encryptedPass))));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
