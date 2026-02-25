package com.bwire.keylock.domain.crypto

import com.bwire.keylock.util.toBase64
import com.bwire.keylock.util.fromBase64

/**
 * Base64 Engine
 * Handles Base64 encoding and decoding operations
 * 
 * Supports:
 * - Encoding from ASCII text or Hexadecimal string
 * - Decoding to ASCII text
 */
object Base64Engine {
    
    /**
     * Encode data to Base64
     * 
     * @param data Input data (ASCII text or Hexadecimal string)
     * @param inputEncoding Input encoding type (ASCII or HEXADECIMAL)
     * @return Result containing Base64 encoded string
     */
    fun encode(
        data: String,
        inputEncoding: DataEncoding
    ): Result<String> {
        return try {
            if (data.isBlank()) {
                throw IllegalArgumentException("Input data cannot be empty")
            }
            
            val bytes = when (inputEncoding) {
                DataEncoding.ASCII -> {
                    // Direct ASCII to bytes
                    data.toByteArray(Charsets.UTF_8)
                }
                DataEncoding.HEXADECIMAL -> {
                    // Convert hex string to bytes
                    val cleaned = data.replace("\\s".toRegex(), "")
                    
                    // Validate hex string
                    if (!cleaned.all { it.isDigit() || it.uppercaseChar() in 'A'..'F' }) {
                        throw IllegalArgumentException("Invalid hexadecimal input")
                    }
                    
                    if (cleaned.length % 2 != 0) {
                        throw IllegalArgumentException("Hexadecimal string must have even length")
                    }
                    
                    // Convert hex pairs to bytes
                    cleaned.chunked(2)
                        .map { it.toInt(16).toByte() }
                        .toByteArray()
                }
            }
            
            val encoded = bytes.toBase64()
            Result.success(encoded)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Decode Base64 to ASCII text
     * 
     * @param data Base64 encoded string
     * @return Result containing decoded ASCII text
     */
    fun decode(data: String): Result<String> {
        return try {
            if (data.isBlank()) {
                throw IllegalArgumentException("Input data cannot be empty")
            }
            
            // Remove whitespace
            val cleaned = data.replace("\\s".toRegex(), "")
            
            // Decode Base64 to bytes
            val bytes = cleaned.fromBase64()
            
            // Convert bytes to ASCII string
            val decoded = bytes.toString(Charsets.UTF_8)
            
            Result.success(decoded)
        } catch (e: Exception) {
            Result.failure(Exception("Invalid Base64 input: ${e.message}"))
        }
    }
}
