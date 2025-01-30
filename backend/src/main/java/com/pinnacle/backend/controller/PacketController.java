package com.pinnacle.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.pinnacle.backend.util.DecryptionUtil;

@RestController
@RequestMapping("/packet")
@Slf4j
public class PacketController {

    @PostMapping("/save")
    public ResponseEntity<String> savePacket(@RequestHeader("API-Key") String encryptedAPIKey, @RequestBody String encryptedPayload) {
        System.out.println("Received API-Key: "+ encryptedAPIKey);
        System.out.println("Received Encrypted Payload: " + encryptedPayload);
        log.info("Encrypted Payload: "+encryptedPayload);
        try {

            // Decrypt the encrypted payload
            String decryptedPacket = DecryptionUtil.decrypt(encryptedPayload);
            String decryptedAPIKey = DecryptionUtil.decryptAPIKey(encryptedAPIKey);

            System.out.println("Decrypted Packet: " + decryptedPacket);
            System.out.println("Decrypted API Key: " + decryptedAPIKey);
            log.info("Decrypted Packet: "+decryptedPacket);

            

            // You can process the decrypted data as needed (e.g., save to DB)
            return ResponseEntity.ok("Packet saved successfully: " + decryptedPacket);
            

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error decrypting packet");
        }
    }
}
