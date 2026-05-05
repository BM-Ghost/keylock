package com.bwire.keylock.domain.crypto

import java.math.BigInteger

/**
 * Base94 Engine
 * Handles Base94 encoding and decoding operations
 * 
 * Base94 uses a subset of printable ASCII characters (33-126):
 * ! " # $ % & ' ( ) * + , - . / 0-9 : ; < = > ? @ A-Z [ \ ] ^ _ ` a-z { | } ~
 * 
 * Supports:
 * - Encoding from ASCII text or Hexadecimal string
 * - Decoding to ASCII text
 */
object Base94Engine {
    
    // Base94 alphabet: 94 printable ASCII characters
    private val ALPHABET = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
    private val BASE = BigInteger.valueOf(94)
    
    init {
        require(ALPHABET.length == 94) { "Alphabet must have exactly 94 characters" }
    }
    
    /**
     * Encode data to Base94
     * 
     * @param data Input data (ASCII text or Hexadecimal string)
     * @param inputEncoding Input encoding type (ASCII or HEXADECIMAL)
     * @return Result containing Base94 encoded string
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
            
            val encoded = encodeBytes(bytes)
            Result.success(encoded)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Decode Base94 to ASCII text
     * 
     * @param data Base94 encoded string
     * @return Result containing decoded ASCII text
     */
    fun decode(data: String): Result<String> {
        return try {
            if (data.isBlank()) {
                throw IllegalArgumentException("Input data cannot be empty")
            }
            
            // Validate Base94 string
            if (!data.all { it in ALPHABET }) {
                throw IllegalArgumentException("Invalid Base94 input - contains characters not in Base94 alphabet")
            }
            
            // Decode Base94 to bytes
            val bytes = decodeBytes(data)
            
            // Convert bytes to UTF-8 string
            val decoded = bytes.toString(Charsets.UTF_8)
            
            Result.success(decoded)
        } catch (e: Exception) {
            Result.failure(Exception("Invalid Base94 input: ${e.message}"))
        }
    }
    
    /**
     * Encode byte array to Base94 string
     * Converts bytes to a base-94 representation using the alphabet
     */
    private fun encodeBytes(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        
        // Convert bytes to BigInteger (big-endian)
        val num = BigInteger(1, bytes)
        
        if (num == BigInteger.ZERO) return ALPHABET[0].toString()
        
        // Convert to base-94
        val result = mutableListOf<Char>()
        var current = num
        
        while (current > BigInteger.ZERO) {
            val remainder = current.mod(BASE).toInt()
            result.add(ALPHABET[remainder])
            current = current.divide(BASE)
        }
        
        // Add leading zeros (represented as first alphabet character)
        for (b in bytes) {
            if (b == 0.toByte()) {
                result.add(ALPHABET[0])
            } else {
                break
            }
        }
        
        // Reverse to get correct order
        return result.reversed().joinToString("")
    }
    
    /**
     * Decode Base94 string to byte array
     * Converts base-94 representation back to bytes
     */
    private fun decodeBytes(data: String): ByteArray {
        if (data.isEmpty()) return ByteArray(0)
        
        // Count leading zeros
        var leadingZeros = 0
        for (c in data) {
            if (c == ALPHABET[0]) {
                leadingZeros++
            } else {
                break
            }
        }
        
        // Convert from base-94 to BigInteger
        var num = BigInteger.ZERO
        for (i in leadingZeros until data.length) {
            val c = data[i]
            val index = ALPHABET.indexOf(c)
            if (index == -1) {
                throw IllegalArgumentException("Invalid character in Base94 string: $c")
            }
            num = num.multiply(BASE).add(BigInteger.valueOf(index.toLong()))
        }
        
        // Convert to byte array and prepend leading zeros
        val numBytes = if (num == BigInteger.ZERO) {
            ByteArray(0)
        } else {
            num.toByteArray().let { bytes ->
                // Remove leading sign byte if present
                if (bytes.isNotEmpty() && bytes[0] == 0.toByte() && bytes.size > 1) {
                    bytes.copyOfRange(1, bytes.size)
                } else {
                    bytes
                }
            }
        }
        
        return if (leadingZeros > 0) {
            ByteArray(leadingZeros) + numBytes
        } else {
            numBytes
        }
    }
}
