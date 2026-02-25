package com.bwire.keylock.domain.crypto

/**
 * AES Algorithm variants
 */
enum class AESAlgorithm(
    val displayName: String,
    val keyBits: Int
) {
    AES_128("AES-128", 128),
    AES_192("AES-192", 192),
    AES_256("AES-256", 256);
    
    val keyBytes: Int get() = keyBits / 8
}

/**
 * Cipher modes of operation
 */
enum class CipherMode(
    val displayName: String,
    val requiresIV: Boolean = false
) {
    ECB("ECB", requiresIV = false),
    CBC("CBC", requiresIV = true),
    CFB("CFB", requiresIV = true),
    OFB("OFB", requiresIV = true),
    KCV("KCV", requiresIV = false); // Key Check Value
}

/**
 * Data encoding formats
 */
enum class DataEncoding(
    val displayName: String
) {
    ASCII("ASCII"),
    HEXADECIMAL("Hexadecimal")
}

/**
 * DES Algorithm variants
 */
enum class DESAlgorithm(
    val displayName: String,
    val keyBits: Int
) {
    DES("DES", 56),
    TDES_2KEY("TDES (2-Key)", 112),
    TDES_3KEY("TDES (3-Key)", 168);
    
    val keyBytes: Int get() = keyBits / 8
}

/**
 * Hash algorithms
 */
enum class HashAlgorithm(
    val displayName: String
) {
    MD4("MD4"),
    MD5("MD5"),
    SHA1("SHA-1"),
    SHA224("SHA-224"),
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512"),
    SHA3_224("SHA3-224"),
    SHA3_256("SHA3-256"),
    SHA3_384("SHA3-384"),
    SHA3_512("SHA3-512"),
    RIPEMD160("RIPEMD-160"),
    WHIRLPOOL("WHIRLPOOL"),
    TIGER_192("TIGER-192"),
    CRC32("CRC32"),
    CRC32_RFC1510("CRC32-RFC1510"),
    CRC24_RFC2440("CRC24-RFC2440"),
    MDC2("MDC-2")
}

/**
 * MAC (Message Authentication Code) algorithms
 */
enum class MACAlgorithm(
    val displayName: String
) {
    ISO_9797_1_MAC1("ISO/IEC 9797-1 Algorithm 1 (MAC1)"),
    ISO_9797_1_MAC2("ISO/IEC 9797-1 Algorithm 2 (MAC2)"),
    ISO_9797_1_MAC3("ISO/IEC 9797-1 Algorithm 3 (MAC3)"),
    ANSI_X9_9("ANSI X9.9"),
    ANSI_X9_19("ANSI X9.19"),
    AS2805_4_1("AS2805.4.1"),
    TDES_CBC_MAC("TDES CBC-MAC"),
    HMAC_SHA1("HMAC-SHA1"),
    HMAC_SHA256("HMAC-SHA256"),
    HMAC_SHA512("HMAC-SHA512"),
    CMAC_AES("CMAC-AES"),
    CMAC_TDES("CMAC-TDES"),
    RETAIL_MAC("Retail MAC")
}

/**
 * PIN block formats
 */
enum class PINBlockFormat(
    val displayName: String,
    val format: String
) {
    ISO_0("ISO Format 0", "ISO-0"),
    ISO_1("ISO Format 1", "ISO-1"),
    ISO_2("ISO Format 2", "ISO-2"),
    ISO_3("ISO Format 3", "ISO-3"),
    ISO_4("ISO Format 4 (AES)", "ISO-4"),
    ECI_1("ECI Format 1", "ECI-1"),
    ECI_2("ECI Format 2", "ECI-2"),
    ECI_3("ECI Format 3", "ECI-3"),
    ECI_4("ECI Format 4", "ECI-4")
}

/**
 * RSA key sizes
 */
enum class RSAKeySize(
    val displayName: String,
    val bits: Int
) {
    RSA_1024("RSA-1024", 1024),
    RSA_2048("RSA-2048", 2048),
    RSA_3072("RSA-3072", 3072),
    RSA_4096("RSA-4096", 4096)
}

/**
 * ECC curves
 */
enum class ECCCurve(
    val displayName: String,
    val curveName: String
) {
    SECP256R1("secp256r1 (P-256)", "secp256r1"),
    SECP384R1("secp384r1 (P-384)", "secp384r1"),
    SECP521R1("secp521r1 (P-521)", "secp521r1"),
    SECP256K1("secp256k1", "secp256k1")
}

/**
 * Padding schemes
 */
enum class PaddingScheme(
    val displayName: String
) {
    NONE("None"),
    PKCS5("PKCS#5"),
    PKCS7("PKCS#7"),
    ISO_IEC_7816_4("ISO/IEC 7816-4"),
    ANSI_X9_23("ANSI X9.23"),
    ZERO_PADDING("Zero Padding")
}

/**
 * Character encoding conversion types
 */
enum class CharacterEncodingType(
    val displayName: String
) {
    BINARY_TO_HEX("Binary -> Hexadecimal"),
    HEX_TO_BINARY("Hexadecimal -> Binary"),
    ASCII_TO_EBCDIC("ASCII -> EBCDIC"),
    EBCDIC_TO_ASCII("EBCDIC -> ASCII"),
    ASCII_TO_HEX("ASCII Text -> Hexadecimal"),
    ATM_ASCII_DEC_TO_HEX("ATM ASCII Decimal -> Hexadecimal"),
    HEX_TO_ATM_ASCII_DEC("Hexadecimal -> ATM ASCII Decimal")
}

/**
 * BCD (Binary Coded Decimal) data format
 */
enum class BCDFormat(
    val displayName: String
) {
    BINARY("Binary"),
    HEXADECIMAL("Hexadecimal")
}

/**
 * Check digit calculation methods
 */
enum class CheckDigitMethod(
    val displayName: String
) {
    LUHN("Luhn's number (MOD 10)"),
    AMEX_SE("Amex SE Number (MOD 9)")
}

/**
 * Message parsing modes
 */
enum class ParseMode(
    val displayName: String
) {
    ATM_NDC("ATM NDC"),
    ATM_WINCOR("ATM Wincor"),
    ISO_8583_1987("ISO 8583 1987")
}

/**
 * RSA data encoding formats
 */
enum class RSADataEncoding(
    val displayName: String
) {
    NONE("None"),
    ASCII("ASCII"),
    EBCDIC("EBCDIC"),
    BCD("BCD"),
    BCD_LEFT_F("BCD_left_F"),
    UTF_8("UTF_8"),
    ASCII_HEX("ASCII_HEX"),
    ASCII_BASE64("ASCII_BASE64"),
    EBCDIC_HEX("EBCDIC_HEX"),
    ASCII_ZERO_PADDED("ASCII_zero_padded"),
    BCD_SIGNED("BCD_Signed")
}

/**
 * RSA DER encoding types
 */
enum class RSADEREncoding(
    val displayName: String
) {
    UNKNOWN("UNKNOWN"),
    ENCODING_01_DER_ASN1_PUBLIC_KEY_UNSIGNED("ENCODING_01_DER_ASN1_PUBLIC_KEY_UNSIGNED"),
    ENCODING_02_DER_ASN1_PUBLIC_KEY_2S_COMPLIMENT("ENCODING_02_DER_ASN1_PUBLIC_KEY_2S_COMPLIMENT")
}
