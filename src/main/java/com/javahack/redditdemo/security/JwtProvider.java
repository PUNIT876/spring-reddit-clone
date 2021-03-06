package com.javahack.redditdemo.security;


import com.javahack.redditdemo.exception.SpringRedditException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Service
public class JwtProvider {

    private KeyStore keyStore;

    @PostConstruct
    public void init(){
        try{
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/springblog.jks");
            keyStore.load(resourceAsStream , "springblog".toCharArray());

        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new SpringRedditException("Exception occurred while loading keystore");
        }
    }
    public String generateToken(Authentication authentication){
        User principal = (User) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(principal.getUsername())
                .signWith(getPrivateKey())
                .compact();


    }

    private Key getPrivateKey() {
        try{
            return keyStore.getKey("springblog" , "springblog".toCharArray());
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new SpringRedditException("Exception occured while retrieving public key from keystore");
        }
    }

    public boolean validateToken(String jwt){
        Jwts.parser().setSigningKey(getPublicKey()).parseClaimsJws(jwt);
        return true;
    }

    private PublicKey getPublicKey() {
        try{
            return keyStore.getCertificate("springblog").getPublicKey();
        } catch (KeyStoreException e) {
            throw new SpringRedditException("Exception occured while " +
                    "returning public key from Keystore");
        }
    }

    public String getUsernameFromJwt(String token){

        Claims claims = Jwts.parser()
                .setSigningKey(getPublicKey())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}
