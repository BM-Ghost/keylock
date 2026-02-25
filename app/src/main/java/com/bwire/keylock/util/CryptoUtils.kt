package com.bwire.keylock.util

/**
 * Cryptographic utility functions
 * Hex encoding/decoding, data validation, formatting
 */

/**
 * Convert byte array to hexadecimal string
 */
fun ByteArray.toHexString(): String {
    return joinToString("") { "%02X".format(it) }
}

/**
 * Convert hexadecimal string to byte array
 */
fun String.hexToByteArray(): ByteArray {
    val cleanHex = this.replace("\\s+".toRegex(), "").uppercase()
    
    if (cleanHex.length % 2 != 0) {
        throw IllegalArgumentException("Hex string must have even length")
    }
    
    return cleanHex.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

/**
 * Validate hexadecimal string
 */
fun String.isValidHex(): Boolean {
    val cleanHex = this.replace("\\s+".toRegex(), "")
    return cleanHex.matches(Regex("^[0-9A-Fa-f]*$")) && cleanHex.length % 2 == 0
}

/**
 * Validate ASCII string (printable characters only)
 */
fun String.isValidAscii(): Boolean {
    return this.all { it.code in 32..126 }
}

/**
 * Convert ASCII string to byte array
 */
fun String.asciiToByteArray(): ByteArray {
    return this.toByteArray(Charsets.US_ASCII)
}

/**
 * Convert byte array to ASCII string (if possible)
 */
fun ByteArray.toAsciiString(): String {
    return String(this, Charsets.US_ASCII)
}

/**
 * XOR two byte arrays (for cryptographic operations)
 */
fun ByteArray.xor(other: ByteArray): ByteArray {
    require(this.size == other.size) { "Arrays must have same length for XOR" }
    return ByteArray(this.size) { i -> (this[i].toInt() xor other[i].toInt()).toByte() }
}

/**
 * Pad data to block size using PKCS#7
 */
fun ByteArray.padPKCS7(blockSize: Int): ByteArray {
    val paddingLength = blockSize - (this.size % blockSize)
    val paddingByte = paddingLength.toByte()
    return this + ByteArray(paddingLength) { paddingByte }
}

/**
 * Remove PKCS#7 padding
 */
fun ByteArray.unpadPKCS7(): ByteArray {
    if (this.isEmpty()) return this
    
    val paddingLength = this.last().toInt()
    if (paddingLength < 1 || paddingLength > this.size) {
        throw IllegalArgumentException("Invalid PKCS7 padding")
    }
    
    // Verify padding bytes
    for (i in (this.size - paddingLength) until this.size) {
        if (this[i] != paddingLength.toByte()) {
            throw IllegalArgumentException("Invalid PKCS7 padding")
        }
    }
    
    return this.copyOf(this.size - paddingLength)
}

/**
 * Pad data to block size using zero padding
 */
fun ByteArray.padZero(blockSize: Int): ByteArray {
    val paddingLength = blockSize - (this.size % blockSize)
    if (paddingLength == blockSize) return this
    return this + ByteArray(paddingLength) { 0 }
}

/**
 * Format hex string with spaces for readability
 */
fun String.formatHex(groupSize: Int = 4): String {
    val cleanHex = this.replace("\\s+".toRegex(), "")
    return cleanHex.chunked(groupSize).joinToString(" ")
}

/**
 * Mask sensitive data for display
 */
fun String.maskSensitive(visibleChars: Int = 4): String {
    if (this.length <= visibleChars) return "*".repeat(this.length)
    return this.take(visibleChars) + "*".repeat(this.length - visibleChars)
}

/**
 * BCD (Binary Coded Decimal) encoding
 */
fun String.toBCD(): ByteArray {
    val digits = this.filter { it.isDigit() }
    val padded = if (digits.length % 2 != 0) "0$digits" else digits
    
    return padded.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

/**
 * BCD decoding
 */
fun ByteArray.fromBCD(): String {
    return this.joinToString("") { 
        val high = (it.toInt() shr 4) and 0x0F
        val low = it.toInt() and 0x0F
        "$high$low"
    }
}

/**
 * Calculate Luhn check digit
 */
fun String.calculateLuhnCheckDigit(): Char {
    val digits = this.filter { it.isDigit() }
    var sum = 0
    var alternate = true
    
    for (i in digits.length - 1 downTo 0) {
        var digit = digits[i].digitToInt()
        if (alternate) {
            digit *= 2
            if (digit > 9) digit -= 9
        }
        sum += digit
        alternate = !alternate
    }
    
    val checkDigit = (10 - (sum % 10)) % 10
    return '0' + checkDigit
}

/**
 * Verify Luhn check digit
 * Validates the entire number by checking if sum % 10 == 0
 */
fun String.verifyLuhn(): Boolean {
    if (this.isEmpty() || !this.all { it.isDigit() }) return false
    
    var sum = 0
    var alternate = false
    
    for (i in this.length - 1 downTo 0) {
        var digit = this[i].digitToInt()
        if (alternate) {
            digit *= 2
            if (digit > 9) digit -= 9
        }
        sum += digit
        alternate = !alternate
    }
    
    return sum % 10 == 0
}

/**
 * Encode to Base64
 */
fun ByteArray.toBase64(): String {
    return android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
}

/**
 * Decode from Base64
 */
fun String.fromBase64(): ByteArray {
    return android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
}

/**
 * Secure memory zeroization
 */
fun ByteArray.zeroize() {
    this.fill(0)
}

/**
 * Generate random bytes
 */
fun generateRandomBytes(length: Int): ByteArray {
    val random = java.security.SecureRandom()
    return ByteArray(length).apply { random.nextBytes(this) }
}

/**
 * Generate UUID
 */
fun generateUUID(): String {
    return java.util.UUID.randomUUID().toString()
}

/**
 * Decimalize data (convert to digits 0-9)
 */
fun ByteArray.decimalize(): String {
    return this.joinToString("") {
        val digit = (it.toInt() and 0xFF) % 10
        digit.toString()
    }
}

/**
 * Bit shift operations
 */
fun ByteArray.shiftLeft(bits: Int): ByteArray {
    val result = ByteArray(this.size)
    var carry = 0
    
    for (i in this.size - 1 downTo 0) {
        val value = (this[i].toInt() and 0xFF)
        result[i] = ((value shl bits) or carry).toByte()
        carry = (value shr (8 - bits)) and 0xFF
    }
    
    return result
}

fun ByteArray.shiftRight(bits: Int): ByteArray {
    val result = ByteArray(this.size)
    var carry = 0
    
    for (i in 0 until this.size) {
        val value = (this[i].toInt() and 0xFF)
        result[i] = ((value shr bits) or carry).toByte()
        carry = (value shl (8 - bits)) and 0xFF
    }
    
    return result
}

/**
 * ISO 7816-4 padding
 */
fun ByteArray.padISO7816_4(blockSize: Int): ByteArray {
    val paddingLength = blockSize - (this.size % blockSize)
    val result = this.copyOf(this.size + paddingLength)
    result[this.size] = 0x80.toByte()
    return result
}

/**
 * Remove ISO 7816-4 padding
 */
fun ByteArray.unpadISO7816_4(): ByteArray {
    for (i in this.size - 1 downTo 0) {
        if (this[i] == 0x80.toByte()) {
            return this.copyOf(i)
        }
        if (this[i] != 0.toByte()) {
            throw IllegalArgumentException("Invalid ISO 7816-4 padding")
        }
    }
    throw IllegalArgumentException("Invalid ISO 7816-4 padding")
}
