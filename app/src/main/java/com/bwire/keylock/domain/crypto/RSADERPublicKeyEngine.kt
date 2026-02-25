package com.bwire.keylock.domain.crypto

import android.util.Base64
import java.math.BigInteger

/**
 * RSA DER Public Key Engine
 * Handles encoding and decoding of RSA public keys in DER format
 * 
 * Supports:
 * - Encoding: Modulus + Exponent -> DER encoded public key
 * - Decoding: DER encoded public key -> Modulus + Exponent
 */
object RSADERPublicKeyEngine {
    
    /**
     * Encode RSA public key to DER format
     */
    fun encode(
        modulus: String,
        modulusEncoding: RSADataEncoding,
        exponent: String,
        exponentEncoding: RSADataEncoding,
        modulusNegative: Boolean,
        derEncoding: RSADEREncoding
    ): Result<String> {
        return try {
            if (modulus.isBlank() || exponent.isBlank()) {
                throw IllegalArgumentException("Modulus and Exponent cannot be empty")
            }
            
            // Decode modulus based on encoding format
            val modulusBytes = decodeData(modulus, modulusEncoding)
            
            // Decode exponent based on encoding format
            val exponentBytes = decodeData(exponent, exponentEncoding)
            
            // Apply modulus negative if needed
            val finalModulusBytes = if (modulusNegative) {
                negateBytes(modulusBytes)
            } else {
                modulusBytes
            }
            
            // Build DER structure
            val derBytes = buildDERPublicKey(finalModulusBytes, exponentBytes, derEncoding)
            
            // Return as hex string
            val result = derBytes.joinToString("") { "%02X".format(it) }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Decode DER formatted RSA public key
     */
    fun decode(
        data: String,
        dataEncoding: RSADataEncoding,
        derEncoding: RSADEREncoding
    ): Result<RSAPublicKeyComponents> {
        return try {
            if (data.isBlank()) {
                throw IllegalArgumentException("Data cannot be empty")
            }
            
            // Decode data based on encoding format
            val derBytes = decodeData(data, dataEncoding)
            
            // Parse DER structure
            val components = parseDERPublicKey(derBytes, derEncoding)
            
            Result.success(components)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Decode data based on encoding format
     */
    private fun decodeData(data: String, encoding: RSADataEncoding): ByteArray {
        return when (encoding) {
            RSADataEncoding.NONE -> data.toByteArray()
            RSADataEncoding.ASCII -> data.toByteArray(Charsets.US_ASCII)
            RSADataEncoding.UTF_8 -> data.toByteArray(Charsets.UTF_8)
            RSADataEncoding.ASCII_HEX -> {
                val cleaned = data.replace("\\s".toRegex(), "")
                if (cleaned.length % 2 != 0) {
                    throw IllegalArgumentException("Hex string must have even length")
                }
                cleaned.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            }
            RSADataEncoding.ASCII_BASE64 -> Base64.decode(data, Base64.NO_WRAP)
            RSADataEncoding.EBCDIC_HEX -> {
                // For now, treat as ASCII_HEX (proper EBCDIC conversion would be more complex)
                val cleaned = data.replace("\\s".toRegex(), "")
                cleaned.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            }
            else -> throw IllegalArgumentException("Encoding format not yet supported: ${encoding.displayName}")
        }
    }
    
    /**
     * Negate bytes (two's complement)
     */
    private fun negateBytes(bytes: ByteArray): ByteArray {
        val bigInt = BigInteger(bytes)
        return bigInt.negate().toByteArray()
    }
    
    /**
     * Build DER encoded public key
     * Structure: SEQUENCE { modulus INTEGER, exponent INTEGER }
     */
    private fun buildDERPublicKey(
        modulus: ByteArray,
        exponent: ByteArray,
        derEncoding: RSADEREncoding
    ): ByteArray {
        // Encode modulus as DER INTEGER
        val modulusInt = encodeDERInteger(modulus)
        
        // Encode exponent as DER INTEGER
        val exponentInt = encodeDERInteger(exponent)
        
        // Build SEQUENCE
        val sequenceContent = modulusInt + exponentInt
        val sequence = encodeDERSequence(sequenceContent)
        
        return sequence
    }
    
    /**
     * Encode integer as DER INTEGER
     */
    private fun encodeDERInteger(value: ByteArray): ByteArray {
        var intBytes = value
        
        // Add leading zero if high bit is set (to ensure positive number)
        if (intBytes.isNotEmpty() && (intBytes[0].toInt() and 0x80) != 0) {
            intBytes = byteArrayOf(0x00) + intBytes
        }
        
        // Tag: 0x02 (INTEGER)
        val tag = 0x02.toByte()
        
        // Length
        val length = encodeDERLength(intBytes.size)
        
        return byteArrayOf(tag) + length + intBytes
    }
    
    /**
     * Encode sequence as DER SEQUENCE
     */
    private fun encodeDERSequence(content: ByteArray): ByteArray {
        // Tag: 0x30 (SEQUENCE)
        val tag = 0x30.toByte()
        
        // Length
        val length = encodeDERLength(content.size)
        
        return byteArrayOf(tag) + length + content
    }
    
    /**
     * Encode DER length
     */
    private fun encodeDERLength(length: Int): ByteArray {
        return if (length < 128) {
            byteArrayOf(length.toByte())
        } else {
            // Long form
            val lengthBytes = mutableListOf<Byte>()
            var len = length
            while (len > 0) {
                lengthBytes.add(0, (len and 0xFF).toByte())
                len = len shr 8
            }
            byteArrayOf((0x80 or lengthBytes.size).toByte()) + lengthBytes.toByteArray()
        }
    }
    
    /**
     * Parse DER encoded public key
     */
    private fun parseDERPublicKey(
        derBytes: ByteArray,
        derEncoding: RSADEREncoding
    ): RSAPublicKeyComponents {
        var offset = 0
        
        // Parse SEQUENCE tag
        if (derBytes[offset] != 0x30.toByte()) {
            throw IllegalArgumentException("Invalid DER: Expected SEQUENCE tag")
        }
        offset++
        
        // Parse SEQUENCE length
        val (seqLength, seqLenBytes) = parseDERLength(derBytes, offset)
        offset += seqLenBytes
        
        // Parse modulus
        val (modulus, modulusBytes) = parseDERInteger(derBytes, offset)
        offset += modulusBytes
        
        // Parse exponent
        val (exponent, _) = parseDERInteger(derBytes, offset)
        
        // Check if modulus is negative (starts with 0x00 padding)
        val modulusNegative = modulus.size > 1 && modulus[0] == 0x00.toByte()
        
        // Format modulus and exponent as hex
        val modulusHex = modulus.joinToString("") { "%02X".format(it) }
        val exponentHex = exponent.joinToString("") { "%02X".format(it) }
        
        return RSAPublicKeyComponents(
            modulus = modulusHex,
            exponent = exponentHex,
            modulusNegative = modulusNegative
        )
    }
    
    /**
     * Parse DER length
     * Returns (length, bytesConsumed)
     */
    private fun parseDERLength(data: ByteArray, offset: Int): Pair<Int, Int> {
        val firstByte = data[offset].toInt() and 0xFF
        
        return if (firstByte < 128) {
            // Short form
            Pair(firstByte, 1)
        } else {
            // Long form
            val numLengthBytes = firstByte and 0x7F
            var length = 0
            for (i in 1..numLengthBytes) {
                length = (length shl 8) or (data[offset + i].toInt() and 0xFF)
            }
            Pair(length, 1 + numLengthBytes)
        }
    }
    
    /**
     * Parse DER INTEGER
     * Returns (value, bytesConsumed)
     */
    private fun parseDERInteger(data: ByteArray, offset: Int): Pair<ByteArray, Int> {
        if (data[offset] != 0x02.toByte()) {
            throw IllegalArgumentException("Invalid DER: Expected INTEGER tag")
        }
        
        val (length, lengthBytes) = parseDERLength(data, offset + 1)
        val valueOffset = offset + 1 + lengthBytes
        val value = data.copyOfRange(valueOffset, valueOffset + length)
        
        return Pair(value, 1 + lengthBytes + length)
    }
}

/**
 * RSA Public Key Components
 */
data class RSAPublicKeyComponents(
    val modulus: String,
    val exponent: String,
    val modulusNegative: Boolean
)
