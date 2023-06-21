package com.hotelbooking.api.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auth {
    // Admin credentials
    private String username;
    private String password;
    private String token;
}