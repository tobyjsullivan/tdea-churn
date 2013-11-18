package main.java;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import javax.xml.bind.DatatypeConverter;


class ZiggyTest {
		private static String text = "123456";
		private static String targetHash = "EQ7fIpT7i/Q=";

		private static final String RANGE = "abcdefghijklmnopqrstuvwxyz";
		private static final int RANGE_SIZE_EXPECTED = 26;
		private static final int MIN_COMBO_LENGTH = 1;
		private static final int MAX_COMBO_LENGTH = 8;
		
		private static final long MIN_TEST_VALUE = 61303500040L;
		private static final long MAX_TEST_VALUE = 281474976710656L;
		
		private static int comboLength = 0;

        public static void main(String[] args) throws Exception{  
//            if(RANGE.length() != RANGE_SIZE_EXPECTED) {
//            	throw new Exception("Bad range length");
//            }
//        	
//        	
//            for(comboLength = MIN_COMBO_LENGTH; comboLength <= MAX_COMBO_LENGTH; comboLength++) {
//                long total = calcNumCombos();
//	        	for(long j = 0; j < total; j++) {
//	        		String combo = getCombo(j);
//	
//	                if(testPassword(combo)) {
//	                	System.out.println("SUCCESS: "+combo);
//	                	System.exit(0);
//	                }
//	        	}
//            }
        	
        	long stamp = System.currentTimeMillis();
        	long lastI = 0;
        	float rate = 0;
        	
        	long nextStat = MIN_TEST_VALUE + 10;
        	
        	byte[] targetCipher = DatatypeConverter.parseBase64Binary(targetHash);

            IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            byte[] plainTextBytes = ZiggyTest.text.getBytes("utf-8");
            
        	for(long i = MIN_TEST_VALUE; i <= MAX_TEST_VALUE; i++) {
        		byte[] keyBytes = getBytes(i);
        		
//        		byte[] codedtext = ZiggyTest.encryptWithByteKey(ZiggyTest.text, key);
        		
        		SecretKey key = new SecretKeySpec(keyBytes, "DESede");
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);

                byte[] codedtext = cipher.doFinal(plainTextBytes);
        		

//                System.out.println("Password: " + i + "; "+ codedtext + "; Rate: " + rate);
                
                if(byteArraysEqual(codedtext, targetCipher)) {

                	String printable = DatatypeConverter.printBase64Binary(codedtext);
                	System.out.println("Password: " + i + "; "+ printable + "; Rate: " + rate);
                	System.out.println("SUCCESS: " + i);
                	System.exit(0);
                }
                
                if(i == nextStat) {
                	
                	long now = System.currentTimeMillis();
                	float timeDiff = (float)(now - stamp)/1000; // Convert to seconds
                	if(timeDiff == 0) {
                		continue;
                	}
                	long iDiff = i - lastI;
                	rate = (iDiff / timeDiff);
                	
                	// Reset
                	stamp = now;
                	lastI = i;
                	
                	String printable = DatatypeConverter.printBase64Binary(codedtext);
                	
                	System.out.println("Password: " + i + "; "+ printable + "; Rate: " + rate + "; ETA: " + estimatedTimeRemaining(MAX_TEST_VALUE - i, rate));

                	nextStat += (long)Math.min(rate, 1000000);
                }
        	}
            
            System.out.println("FAILURE");
        }
        
        private static String estimatedTimeRemaining(long values, float rate) {
        	long remaining = (long)(values / rate);
        	
        	int seconds = (int)(remaining % 60);
        	remaining /= 60;
        	int minutes = (int)(remaining % 60);
        	remaining /= 60;
        	int hours = (int)(remaining % 24);
        	remaining /= 24;
        	int days = (int)(remaining % 365);
        	int years = (int)(remaining / 365);
        	
        	return String.format("%d years, %d days, %d hours, %d minutes and %d seconds", years, days, hours, minutes, seconds);
        }
        
        private static Boolean byteArraysEqual(byte[] one, byte[] two) {
        	if(one.length != two.length) {
        		return false;
        	}
        	
        	for(int i = 0; i < one.length; i++) {
        		if(one[i] != two[i]) {
        			return false;
        		}
        	}
        	
        	return true;
        }
        
        private static long calcNumCombos() {
        	long total = 1;
        	for(int i = 0; i < comboLength; i++) {
        		total *= RANGE.length();
        	}
        	return total;
        }
        
        private static String getCombo(long index) {
        	String output = "";
        	
        	int rangeSize = RANGE.length();
        	while(index > 0) {
        		int idx = (int)(index % rangeSize);
        		output = RANGE.charAt(idx) + output;
        		index /= rangeSize;
        	}
        	
        	// Fill remainder
        	while(output.length() < comboLength) {
        		output = RANGE.charAt(0) + output;
        	}
        	
        	return output;
        }
        
        private static byte getByte(int in) {
        	if(in < 0 || in >= 256) {
        		throw new IllegalArgumentException("Bad byte value");
        	}
        	
        	in -= 128;
        	
        	return (byte)in;
        }
        
        private static byte[] getBytes(long input) {
        	if(input > MAX_TEST_VALUE) {
        		throw new IllegalArgumentException("Input too big");
        	}
        	
        	byte[] output = new byte[24];
        	
        	int i = 0;
        	while(input > 0) {
        		output[i++] = getByte((int)(input % 256));
        		input /= 256;
        	}

            for (int j = 0, k = 16; j < 8;) {
            	output[k++] = output[j++];
            }
        	
        	return output;
        }
        
        private static Boolean testPassword(String password) throws Exception{
            String codedtext = ZiggyTest.encrypt(ZiggyTest.text, password);

            System.out.print("Password: " + password + "; ");
            System.out.println(codedtext); // this is a byte array, you'll just see a reference to an array
            
            if(codedtext.equals(targetHash)) {
            	return true;
            }
            
            return false;
        }

        public static String encrypt(String message, String password) throws Exception {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] digestOfPassword = md.digest(password.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            for (int j = 0, k = 16; j < 8;) {
                    keyBytes[k++] = keyBytes[j++];
            }

            return DatatypeConverter.printBase64Binary(encryptWithByteKey(message, keyBytes));
        }
        
        private static byte[] encryptWithByteKey(String message, byte[] keyBytes) throws Exception{
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            byte[] plainTextBytes = message.getBytes("utf-8");
            byte[] cipherText = cipher.doFinal(plainTextBytes);
            
            return cipherText;
        }

        public static String decrypt(String message, String password) throws Exception {
        	byte[] messageBytes = DatatypeConverter.parseBase64Binary(message);
        	
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] digestOfPassword = md.digest(password.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            for (int j = 0, k = 16; j < 8;) {
                    keyBytes[k++] = keyBytes[j++];
            }

            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            decipher.init(Cipher.DECRYPT_MODE, key, iv);

            byte[] plainText = decipher.doFinal(messageBytes);

            return new String(plainText, "UTF-8");
        }
    }