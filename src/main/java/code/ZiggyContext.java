package code;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.bind.DatatypeConverter;

public class ZiggyContext {
	
	public ZiggyContext(String targetHash, String text) throws Exception {
		this.targetHash = targetHash;
		this.text = text;
		
		targetCipher = DatatypeConverter.parseBase64Binary(targetHash);
		iv = new IvParameterSpec(new byte[8]);
		cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		plainTextBytes = text.getBytes("utf-8");
	}
	
	String targetHash, text;
	
	public final byte[] targetCipher;
    public final IvParameterSpec iv;
    public final Cipher cipher;
    public final byte[] plainTextBytes;

}
