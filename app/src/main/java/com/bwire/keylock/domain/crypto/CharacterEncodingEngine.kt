package com.bwire.keylock.domain.crypto

/**
 * Character Encoding Engine
 * Handles conversions between different character encodings
 * 
 * Supported conversions:
 * - Binary <-> Hexadecimal
 * - ASCII <-> EBCDIC
 * - ASCII Text <-> Hexadecimal
 * - ATM ASCII Decimal <-> Hexadecimal
 */
object CharacterEncodingEngine {
    
    /**
     * Convert data based on encoding type
     */
    fun convert(
        encodingType: CharacterEncodingType,
        input: String
    ): Result<String> {
        return try {
            val result = when (encodingType) {
                CharacterEncodingType.BINARY_TO_HEX -> textToHex(input)  // "Binary" means text/ASCII
                CharacterEncodingType.HEX_TO_BINARY -> hexToText(input)  // "Binary" means text/ASCII
                CharacterEncodingType.ASCII_TO_EBCDIC -> asciiToEbcdic(input)
                CharacterEncodingType.EBCDIC_TO_ASCII -> ebcdicToAscii(input)
                CharacterEncodingType.ASCII_TO_HEX -> asciiToHex(input)
                CharacterEncodingType.ATM_ASCII_DEC_TO_HEX -> atmAsciiDecToHex(input)
                CharacterEncodingType.HEX_TO_ATM_ASCII_DEC -> hexToAtmAsciiDec(input)
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convert text to hexadecimal (for "Binary -> Hexadecimal")
     * "Binary" here means text/ASCII, not actual binary
     * Example: "57652C" -> "353736363532433..."
     */
    private fun textToHex(text: String): String {
        return text.toByteArray(Charsets.ISO_8859_1)
            .joinToString("") { "%02X".format(it) }
    }
    
    /**
     * Convert hexadecimal to text (for "Hexadecimal -> Binary")
     * "Binary" here means text/ASCII, not actual binary
     * Example: "48656C6C6F" -> "Hello"
     */
    private fun hexToText(hex: String): String {
        val cleaned = hex.replace("\\s".toRegex(), "").uppercase()
        if (!cleaned.all { it in '0'..'9' || it in 'A'..'F' }) {
            throw IllegalArgumentException("Invalid hexadecimal string")
        }
        
        if (cleaned.length % 2 != 0) {
            throw IllegalArgumentException("Hexadecimal string must have even length")
        }
        
        val bytes = cleaned.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return String(bytes, Charsets.ISO_8859_1)
    }
    
    /**
     * Convert ASCII to EBCDIC
     */
    private fun asciiToEbcdic(ascii: String): String {
        val bytes = ascii.toByteArray(Charsets.ISO_8859_1)
        val ebcdicBytes = bytes.map { asciiToEbcdicTable[it.toInt() and 0xFF] }
        return ebcdicBytes.joinToString("") { "%02X".format(it) }
    }
    
    /**
     * Convert EBCDIC to ASCII
     */
    private fun ebcdicToAscii(ebcdic: String): String {
        val cleaned = ebcdic.replace("\\s".toRegex(), "").uppercase()
        if (!cleaned.all { it in '0'..'9' || it in 'A'..'F' }) {
            throw IllegalArgumentException("Invalid hexadecimal EBCDIC string")
        }
        
        val ebcdicBytes = cleaned.chunked(2).map { it.toInt(16) }
        // Build reverse lookup: for each EBCDIC byte, find which ASCII byte maps to it
        val reverseTable = IntArray(256) { ebcdicCode ->
            // Find ASCII char that maps to this EBCDIC code
            asciiToEbcdicTable.indexOfFirst { it == ebcdicCode }.takeIf { it != -1 } ?: ebcdicCode
        }
        val asciiBytes = ebcdicBytes.map { reverseTable[it] }
        return String(asciiBytes.map { it.toByte() }.toByteArray(), Charsets.ISO_8859_1)
    }
    
    /**
     * Convert ASCII text to hexadecimal
     * Example: "Hello" -> "48656C6C6F"
     */
    private fun asciiToHex(ascii: String): String {
        return ascii.toByteArray(Charsets.ISO_8859_1)
            .joinToString("") { "%02X".format(it) }
    }
    
    /**
     * Convert ATM ASCII Decimal to hexadecimal
     * Example: "065066067" -> "414243" (ABC)
     * ATM format uses 3-digit decimal ASCII values concatenated
     */
    private fun atmAsciiDecToHex(atmDecimal: String): String {
        val cleaned = atmDecimal.replace("\\s".toRegex(), "")
        if (!cleaned.all { it.isDigit() }) {
            throw IllegalArgumentException("Invalid ATM ASCII Decimal - must contain only digits")
        }
        
        if (cleaned.length % 3 != 0) {
            throw IllegalArgumentException("ATM ASCII Decimal length must be multiple of 3")
        }
        
        return cleaned.chunked(3)
            .map { 
                val decimal = it.toInt()
                if (decimal > 255) {
                    throw IllegalArgumentException("Invalid ASCII value: $decimal (must be 0-255)")
                }
                "%02X".format(decimal)
            }
            .joinToString("")
    }
    
    /**
     * Convert hexadecimal to ATM ASCII Decimal
     * Example: "414243" -> "065066067"
     */
    private fun hexToAtmAsciiDec(hex: String): String {
        val cleaned = hex.replace("\\s".toRegex(), "").uppercase()
        if (!cleaned.all { it in '0'..'9' || it in 'A'..'F' }) {
            throw IllegalArgumentException("Invalid hexadecimal string")
        }
        
        if (cleaned.length % 2 != 0) {
            throw IllegalArgumentException("Hexadecimal string length must be even")
        }
        
        return cleaned.chunked(2)
            .map { it.toInt(16).toString().padStart(3, '0') }
            .joinToString("")
    }
    
    /**
     * ASCII to EBCDIC conversion table (Code Page 037)
     */
    private val asciiToEbcdicTable = intArrayOf(
        0x00, 0x01, 0x02, 0x03, 0x37, 0x2D, 0x2E, 0x2F, 0x16, 0x05, 0x25, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
        0x10, 0x11, 0x12, 0x13, 0x3C, 0x3D, 0x32, 0x26, 0x18, 0x19, 0x3F, 0x27, 0x1C, 0x1D, 0x1E, 0x1F,
        0x40, 0x5A, 0x7F, 0x7B, 0x5B, 0x6C, 0x50, 0x7D, 0x4D, 0x5D, 0x5C, 0x4E, 0x6B, 0x60, 0x4B, 0x61,
        0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0x7A, 0x5E, 0x4C, 0x7E, 0x6E, 0x6F,
        0x7C, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6,
        0xD7, 0xD8, 0xD9, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xAD, 0xE0, 0xBD, 0x5F, 0x6D,
        0x79, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96,
        0x97, 0x98, 0x99, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xC0, 0x4F, 0xD0, 0xA1, 0x07,
        0x20, 0x21, 0x22, 0x23, 0x24, 0x15, 0x06, 0x17, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x09, 0x0A, 0x1B,
        0x30, 0x31, 0x1A, 0x33, 0x34, 0x35, 0x36, 0x08, 0x38, 0x39, 0x3A, 0x3B, 0x04, 0x14, 0x3E, 0xFF,
        0x41, 0xAA, 0x4A, 0xB1, 0x9F, 0xB2, 0x6A, 0xB5, 0xBB, 0xB4, 0x9A, 0x8A, 0xB0, 0xCA, 0xAF, 0xBC,
        0x90, 0x8F, 0xEA, 0xFA, 0xBE, 0xA0, 0xB6, 0xB3, 0x9D, 0xDA, 0x9B, 0x8B, 0xB7, 0xB8, 0xB9, 0xAB,
        0x64, 0x65, 0x62, 0x66, 0x63, 0x67, 0x9E, 0x68, 0x74, 0x71, 0x72, 0x73, 0x78, 0x75, 0x76, 0x77,
        0xAC, 0x69, 0xED, 0xEE, 0xEB, 0xEF, 0xEC, 0xBF, 0x80, 0xFD, 0xFE, 0xFB, 0xFC, 0xBA, 0xAE, 0x59,
        0x44, 0x45, 0x42, 0x46, 0x43, 0x47, 0x9C, 0x48, 0x54, 0x51, 0x52, 0x53, 0x58, 0x55, 0x56, 0x57,
        0x8C, 0x49, 0xCD, 0xCE, 0xCB, 0xCF, 0xCC, 0xE1, 0x70, 0xDD, 0xDE, 0xDB, 0xDC, 0x8D, 0x8E, 0xDF
    )
}
