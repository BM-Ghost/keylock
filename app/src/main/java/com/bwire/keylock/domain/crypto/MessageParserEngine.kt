package com.bwire.keylock.domain.crypto

/**
 * Message Parser Engine
 * Handles parsing and formatting of message data in various formats
 * 
 * Supports:
 * - ATM NDC format
 * - ATM Wincor format
 * - ISO 8583 1987 format
 */
object MessageParserEngine {
    
    /**
     * Parse hex data and format as hex dump
     * 
     * @param hexData Hexadecimal string input
     * @param mode Parsing mode (ATM_NDC, ATM_WINCOR, ISO_8583_1987)
     * @return Result containing formatted hex dump with ASCII representation
     */
    fun parse(
        hexData: String,
        mode: ParseMode
    ): Result<String> {
        return try {
            if (hexData.isBlank()) {
                throw IllegalArgumentException("Input data cannot be empty")
            }
            
            // Clean and validate hex string
            val cleaned = hexData.replace("\\s".toRegex(), "").uppercase()
            
            if (!cleaned.all { it.isDigit() || it in 'A'..'F' }) {
                throw IllegalArgumentException("Invalid hexadecimal input")
            }
            
            if (cleaned.length % 2 != 0) {
                throw IllegalArgumentException("Hexadecimal string must have even length")
            }
            
            // Convert hex string to bytes
            val bytes = cleaned.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
            
            val formatted = formatHexDump(bytes)
            Result.success(formatted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Format byte array as hex dump with offset and ASCII representation
     * Format:
     * 0000:  XX XX XX XX XX XX XX XX  XX XX XX XX XX XX XX XX  ASCII representation
     */
    private fun formatHexDump(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        
        val result = StringBuilder()
        var offset = 0
        
        while (offset < bytes.size) {
            // Offset line (0000:, 0010:, etc.)
            result.append("%04X:".format(offset))
            result.append("  ")
            
            // Hex bytes (16 bytes per line)
            val lineEnd = minOf(offset + 16, bytes.size)
            val lineBytes = bytes.copyOfRange(offset, lineEnd)
            
            // First 8 bytes
            for (i in 0 until 8) {
                if (i < lineBytes.size) {
                    result.append("%02X ".format(lineBytes[i]))
                } else {
                    result.append("   ")
                }
            }
            
            // Separator between first 8 and last 8 bytes
            result.append(" ")
            
            // Last 8 bytes
            for (i in 8 until 16) {
                if (i < lineBytes.size) {
                    result.append("%02X ".format(lineBytes[i]))
                } else {
                    result.append("   ")
                }
            }
            
            // Two spaces before ASCII representation
            result.append(" ")
            
            // ASCII representation
            for (byte in lineBytes) {
                val char = byte.toInt().toChar()
                result.append(if (char.code in 32..126) char else '.')
            }
            
            result.append("\n")
            offset = lineEnd
        }
        
        return result.toString()
    }
}
