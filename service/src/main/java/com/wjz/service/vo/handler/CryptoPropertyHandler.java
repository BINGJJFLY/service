package com.wjz.service.vo.handler;

import org.apache.ibatis.type.TypeReference;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;

public abstract class CryptoPropertyHandler<T> extends TypeReference<T> implements PropertyHandler {
	
	private static final AesCipherService cipherService = new AesCipherService();
	
	protected String encrypt(byte[] plaintext) {
		ByteSource byteSource = cipherService.encrypt(plaintext, CIPHER_KEY);
		return byteSource.toBase64();
	}
	
	public static String decrypt(String ciphertext) {
		byte[] bytes = cipherService.decrypt(Base64.decode(ciphertext), PropertyHandler.CIPHER_KEY).getBytes();
		return new String(bytes);
	}
}