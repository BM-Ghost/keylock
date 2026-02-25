package com.bwire.keylock.domain.crypto

/**
 * Binary Coded Decimal (BCD) Engine
 * Handles BCD encoding and decoding operations
 * 
 * BCD represents decimal digits in binary form:
 * - Each decimal digit (0-9) is encoded as 4 bits (0000-1001)
 * - Packed BCD stores two decimal digits per byte
 */
object BCDEngine {
    
    /**
     * Encode decimal number to BCD
     */
    fun encode(
        decimal: String,
        format: BCDFormat
    ): Result<String> {
        return try {
            // Validate input is numeric
            val cleaned = decimal.replace("\\s".toRegex(), "")
            if (!cleaned.all { it.isDigit() }) {
                throw IllegalArgumentException("Input must contain only digits")
            }
            
            val result = when (format) {
                BCDFormat.BINARY -> encodeToBinary(cleaned)
                BCDFormat.HEXADECIMAL -> encodeToHex(cleaned)
            }
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Decode BCD to decimal number
     */
    fun decode(
        data: String,
        format: BCDFormat
    ): Result<String> {
        return try {
            val result = when (format) {
                BCDFormat.BINARY -> decodeFromBinary(data)
                BCDFormat.HEXADECIMAL -> decodeFromHex(data)
            }
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Encode decimal to binary BCD
     * Example: "25" -> "0010 0101"
     */
    private fun encodeToBinary(decimal: String): String {
        return decimal.map { digit ->
            val value = digit.digitToInt()
            value.toString(2).padStart(4, '0')
        }.joinToString(" ")
    }
    
    /**
     * Encode decimal to hexadecimal BCD
     * Example: "25" -> "25"
     */
    private fun encodeToHex(decimal: String): String {
        // Each decimal digit is already its hex representation in BCD
        return decimal
    }
    
    /**
     * Decode binary BCD to decimal
     * Example: "0010 0101" -> "25"
     */
    private fun decodeFromBinary(binary: String): String {
        val cleaned = binary.replace("\\s".toRegex(), "")
        
        // Validate binary string
        if (!cleaned.all { it == '0' || it == '1' }) {
            throw IllegalArgumentException("Invalid binary BCD - must contain only 0 and 1")
        }
        
        if (cleaned.length % 4 != 0) {
            throw IllegalArgumentException("Binary BCD length must be multiple of 4")
        }
        
        // Convert each 4-bit nibble to decimal digit
        return cleaned.chunked(4).map { nibble ->
            val value = nibble.toInt(2)
            if (value > 9) {
                throw IllegalArgumentException("Invalid BCD nibble: $nibble (value $value > 9)")
            }
            value.toString()
        }.joinToString("")
    }
    
    /**
     * Decode hexadecimal BCD to decimal
     * Example: "00100101" -> "00100101"
     * Each hex digit represents a decimal digit
     */
    private fun decodeFromHex(hex: String): String {
        val cleaned = hex.replace("\\s".toRegex(), "").uppercase()
        
        // Validate hex string
        if (!cleaned.all { it in '0'..'9' || it in 'A'..'F' }) {
            throw IllegalArgumentException("Invalid hexadecimal BCD")
        }
        
        // In packed BCD, each hex digit represents a decimal digit (0-9)
        // Validate all digits are 0-9 (not A-F)
        cleaned.forEach { digit ->
            if (digit !in '0'..'9') {
                throw IllegalArgumentException("Invalid BCD hex digit: $digit (must be 0-9)")
            }
        }
        
        return cleaned
    }
}
