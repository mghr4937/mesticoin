package com.example.mestichain.utils;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SignatureUtils {

    private static final String ALGORITHM_DSA = "DSA";
    private static final String PROVIDER = "SUN";

    //SHA1PRNG: Algoritmo para la generación de números pseudoaleatorios con SUN.
    private static final String ALGORITHM_SHA1 = "SHA1PRNG";

    //SHA1withDSA: Algoritmo de firma DSA (Digital Signature Algorithm) con SHA-1 (Secure Hash Algorithm 1) para crear y verificar firmas digitales.
    private static final String ALGORITHM_SHA_DSA = "SHA1withDSA";

    private static final KeyFactory keyFactory;

    static {
        try {
            keyFactory = KeyFactory.getInstance(ALGORITHM_DSA, PROVIDER);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generar un par de claves publica-privada
     *
     * @return KeyPair par de claves
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_DSA, PROVIDER);
        SecureRandom random = SecureRandom.getInstance(ALGORITHM_SHA1, PROVIDER);
        keyPairGenerator.initialize(1024, random);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Validar una firma para unos datos y clave publica dados
     *
     * @param info         datos firmados y a ser verificados
     * @param signature    a ser verificada
     * @param publicKey    clave publica asociada a la clave privada con la que
     *                     fueron firmados los datos
     * @return true si la firma es valida para los datos y clave publica dados
     */
    public static boolean validateSignature(byte[] info, byte[] signature, byte[] publicKey)
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        // crear un objeto PublicKey con la clave publica dada
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        PublicKey publicKeyObj = keyFactory.generatePublic(keySpec);

        // validar firma
        Signature sig = getSignatureInstance();
        sig.initVerify(publicKeyObj);
        sig.update(info);
        return sig.verify(signature);
    }

    /**
     * Firmar unos datos con una clave privada dada
     *
     * @param info         datos a ser firmados
     * @param privateKey   para firmar los datos
     * @return firma de los datos y que puede ser verificada con los datos y la
     *         clave pública
     */
    public static byte[] sign(byte[] info, byte[] privateKey)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        // crear un objeto PrivateKey con la clave privada dada
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
        PrivateKey privateKeyObj = keyFactory.generatePrivate(keySpec);

        // firmar datos
        Signature sig = getSignatureInstance();
        sig.initSign(privateKeyObj);
        sig.update(info);
        return sig.sign();
    }

    private static Signature getSignatureInstance() throws NoSuchAlgorithmException, NoSuchProviderException {
        return Signature.getInstance(ALGORITHM_SHA_DSA, PROVIDER);
    }


}
