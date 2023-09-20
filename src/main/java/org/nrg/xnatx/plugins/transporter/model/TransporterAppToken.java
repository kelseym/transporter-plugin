package org.nrg.xnatx.plugins.transporter.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


// POJO object using Lombok and Java that can be serialized into JSON and used to represent an authorization Token.

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransporterAppToken {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String scope;
}
