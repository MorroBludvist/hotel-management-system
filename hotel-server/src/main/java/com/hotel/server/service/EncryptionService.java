//package com.hotel.server.service;
//
//import org.springframework.stereotype.Service;
//import java.util.Base64;
//
///**
// * TODO: реализовать шифровку данных
// */
////@Service
//public class EncryptionService {
//
//    public String encrypt(String data) {
//        return Base64.getEncoder().encodeToString(data.getBytes());
//    }
//
//    public String decrypt(String encryptedData) {
//        byte[] decoded = Base64.getDecoder().decode(encryptedData);
//        return new String(decoded);
//    }
//}