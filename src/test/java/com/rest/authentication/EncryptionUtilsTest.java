package com.rest.authentication;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

public class EncryptionUtilsTest {
    @Autowired
    private RestTemplate restTemplate;
    
    @Test public void createsSaltsAsBase64UrlEncoded() throws NoSuchAlgorithmException {
    	String salt = EncryptionUtils.getSalt();
    	assertTrue(Base64.isBase64(salt));
    }
    
    @Test public void creates32ByteSalts() throws NoSuchAlgorithmException {
    	String salt = EncryptionUtils.getSalt();
    	assertEquals(Base64.decodeBase64(salt).length, 32);
    }
}

