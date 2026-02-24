package com.bwire.keylock.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes and structure for KeyLock Pro
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    // Main screens
    object CryptoCalculator : Screen(
        route = "crypto_calculator",
        title = "Crypto Calculator",
        icon = Icons.Default.Lock
    )
    
    object HSMCommander : Screen(
        route = "hsm_commander",
        title = "HSM Commander",
        icon = Icons.Default.Computer
    )
    
    object KeyVault : Screen(
        route = "key_vault",
        title = "Key Vault",
        icon = Icons.Default.VpnKey
    )
    
    object AuditLogs : Screen(
        route = "audit_logs",
        title = "Audit Logs",
        icon = Icons.Default.Description
    )
    
    object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )
    
    // Crypto Calculator submenus
    object CryptoMain : Screen("crypto/main", "Main")
    object CryptoGeneric : Screen("crypto/generic", "Generic")
    object CryptoCipher : Screen("crypto/cipher", "Cipher")
    object CryptoKeys : Screen("crypto/keys", "Keys")
    object CryptoPayments : Screen("crypto/payments", "Payments")
    object CryptoEMV : Screen("crypto/emv", "EMV")
    object CryptoDevelopment : Screen("crypto/development", "Development")
    
    // HSM Commander screens
    object HSMAtalla : Screen("hsm/atalla", "Atalla")
    object HSMSafeNet : Screen("hsm/safenet", "SafeNet")
    object HSMThales : Screen("hsm/thales", "Thales")
    
    // Security
    object LockScreen : Screen("lock_screen", "Lock")
}

/**
 * Crypto Calculator menu categories
 */
enum class CryptoMenu(
    val displayName: String,
    val route: String
) {
    GENERIC("Generic", "crypto/generic"),
    CIPHER("Cipher", "crypto/cipher"),
    KEYS("Keys", "crypto/keys"),
    PAYMENTS("Payments", "crypto/payments"),
    EMV("EMV", "crypto/emv"),
    DEVELOPMENT("Development", "crypto/development")
}

/**
 * Generic menu tools
 */
enum class GenericTool(val displayName: String) {
    HASHES("Hashes"),
    CHARACTER_ENCODING("Character Encoding"),
    BCD("BCD"),
    CHECK_DIGITS("Check Digits"),
    BASE64("Base64"),
    BASE94("Base94"),
    MESSAGE_PARSER("Message Parser"),
    RSA_DER_PUBLIC_KEY("RSA DER Public Key"),
    UUID_GENERATOR("UUID Generator")
}

/**
 * Cipher menu tools
 */
enum class CipherTool(val displayName: String) {
    AES("AES"),
    DES("DES"),
    RSA("RSA"),
    THALES_RSA("Thales RSA"),
    ECC("ECC"),
    ECDSA("ECDSA"),
    FPE("FPE (Format Preserving Encryption)")
}

/**
 * Keys menu tools
 */
enum class KeysTool(val displayName: String) {
    DES_TDES_AES_KEYS("DES / TDES / AES Keys"),
    KEYSHARE_GENERATOR("Keyshare Generator"),
    HSM_KEYS_FUTUREX("HSM Keys - Futurex"),
    HSM_KEYS_ATALLA("HSM Keys - Atalla"),
    HSM_KEYS_SAFENET("HSM Keys - SafeNet"),
    HSM_KEYS_THALES("HSM Keys - Thales"),
    THALES_KEY_BLOCK("Thales Key Block"),
    TR31_KEY_BLOCK("TR-31 Key Block"),
    SSL_CERTIFICATES("SSL Certificates")
}

/**
 * Payments menu tools
 */
enum class PaymentsTool(val displayName: String) {
    AS2805("AS2805"),
    BITMAP("Bitmap"),
    CVV("Card Validation - CVV"),
    AMEX_CSC("Card Validation - AMEX CSC"),
    MASTERCARD_DYNAMIC_CVC3("Card Validation - MasterCard Dynamic CVC3"),
    DUKPT_ISO9797("DUKPT - ISO 9797"),
    DUKPT_AES("DUKPT - AES"),
    MAC_ISO_9797_1("MAC - ISO/IEC 9797-1"),
    MAC_ANSI_X9_9("MAC - ANSI X9.9 / X9.19"),
    MAC_AS2805_4_1("MAC - AS2805.4.1"),
    MAC_TDES_CBC("MAC - TDES CBC-MAC"),
    MAC_HMAC("MAC - HMAC"),
    MAC_CMAC("MAC - CMAC"),
    MAC_RETAIL("MAC - Retail MAC"),
    MDC_ALGORITHMS("MDC Algorithms"),
    PIN_BLOCKS_GENERAL("PIN Blocks - General"),
    PIN_BLOCKS_AES("PIN Blocks - AES"),
    PIN_OFFSET("PIN Offset"),
    PIN_PVV("PIN PVV"),
    VISA_CERTIFICATES("Visa Certificates"),
    ZKA("ZKA")
}

/**
 * EMV menu tools
 */
enum class EMVTool(val displayName: String) {
    APPLICATION_CRYPTOGRAMS("Application Cryptograms"),
    SDA("SDA (Static Data Authentication)"),
    DDA("DDA (Dynamic Data Authentication)"),
    ICC_DYNAMIC_NUMBER("ICC Dynamic Number (MasterCard EMV 3.1.1)"),
    DATA_STORAGE_PARTIAL_KEY("Data Storage Partial Key (MasterCard)"),
    SECURE_MESSAGING_MASTERCARD("Secure Messaging - MasterCard"),
    SECURE_MESSAGING_VISA("Secure Messaging - Visa"),
    HCE_VISA("HCE (Visa)"),
    CAP_TOKEN("CAP Token Computation"),
    ATR_PARSER("ATR Parser"),
    EMV_DATA_PARSER("EMV Data Parser"),
    EMV_TAG_DICTIONARY("EMV Tag Dictionary"),
    APDU_RESPONSE_QUERY("APDU Response Query")
}

/**
 * Development menu tools
 */
enum class DevelopmentTool(val displayName: String) {
    SECURE_PADDING("Secure Padding"),
    STRING_BUILDER("String Builder"),
    TRACE_PARSER("Trace Parser"),
    DECIMALIZE_DATA("Decimalize Data"),
    BIT_SHIFT("Bit Shift")
}

/**
 * HSM vendor types
 */
enum class HSMVendor(val displayName: String) {
    ATALLA("Atalla"),
    SAFENET("SafeNet"),
    THALES("Thales")
}
