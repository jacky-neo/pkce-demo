package com.ghy.vo.authentication.config.pkce;

import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public enum CodeChallengeMethod {
    S256 {
        @Override
        public String transform(String codeVerifier) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
                //return Base64.getUrlEncoder().encodeToString(Hex.encode(hash));
                String encode =  Base64.getUrlEncoder().encodeToString(hash);
                //System.out.println(encode);
                encode = encode.replaceAll("\\=","").replaceAll("\\+","-").replaceAll("\\/","_");
                return encode;
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
    },
    PLAIN {
        @Override
        public String transform(String codeVerifier) {
            return codeVerifier;
        }
    },
    NONE {
        @Override
        public String transform(String codeVerifier) {
            throw new UnsupportedOperationException();
        }
    };

    public abstract String transform(String codeVerifier);

    public static void main(String[] args) throws Exception{
        CodeChallengeMethod s256 = CodeChallengeMethod.S256;
        String codeVerifier = "mWVA5rTJxv0fkLhjyeqlgbIPUOsScVHg";
        String codeChallenge = s256.transform(codeVerifier);
        System.out.printf("%s\n%s\n",codeVerifier,codeChallenge);

        String base64Encode = codeChallenge;
        String base64Decode = Hex.toHexString(Base64.getUrlDecoder().decode(base64Encode));
        System.out.println(base64Decode);
    }
}
