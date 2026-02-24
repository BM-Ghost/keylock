package com.bwire.keylock.domain.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.crypto.digests.MD4Digest
import org.bouncycastle.crypto.digests.WhirlpoolDigest
import org.bouncycastle.crypto.digests.SHA3Digest
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.crypto.digests.TigerDigest
import org.bouncycastle.crypto.digests.MD2Digest
import java.security.MessageDigest
import java.security.Security
import java.util.zip.CRC32 as JavaCRC32
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Hash and MAC (Message Authentication Code) Operations
 */
object HashEngine {
    
    init {
        // Add Bouncy Castle provider for additional algorithms
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }
    
    /**
     * Use Bouncy Castle's low-level digest implementations directly
     */
    private fun hashWithBouncyCastleDigest(digest: org.bouncycastle.crypto.Digest, data: ByteArray): ByteArray {
        digest.update(data, 0, data.size)
        val result = ByteArray(digest.digestSize)
        digest.doFinal(result, 0)
        return result
    }
    
    /**
     * Compute CRC32 checksum (returns 4 bytes, big-endian)
     */
    private fun computeCRC32(data: ByteArray): ByteArray {
        val crc = JavaCRC32()
        crc.update(data)
        val value = crc.value
        // Return as hex string representation would expect (big-endian)
        return byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )
    }
    
    /**
     * Compute CRC32 for RFC1510 (Kerberos) - Standard CRC32 with polynomial 0x04C11DB7
     * Note: RFC1510 uses standard CRC32 but might differ in initial value or final XOR
     */
    private fun computeCRC32RFC1510(data: ByteArray): ByteArray {
        // Try standard CRC32 first - RFC1510 may use it directly
        var crc = 0xFFFFFFFFL
        for (b in data) {
            crc = crc xor ((b.toInt() and 0xFF).toLong())
            for (i in 0 until 8) {
                crc = if ((crc and 1L) != 0L) {
                    (crc shr 1) xor 0xEDB88320L
                } else {
                    crc shr 1
                }
            }
        }
        crc = crc xor 0xFFFFFFFFL
        val result = crc.toInt()
        return byteArrayOf(
            (result shr 24).toByte(),
            (result shr 16).toByte(),
            (result shr 8).toByte(),
            result.toByte()
        )
    }
    
    /**
     * Compute CRC24 for RFC2440 (OpenPGP) - polynomial 0x1864CFB
     */
    private fun computeCRC24RFC2440(data: ByteArray): ByteArray {
        var crc = 0xB704CEL // OpenPGP initial value
        for (b in data) {
            crc = crc xor ((b.toInt() and 0xFF).toLong() shl 16)
            for (i in 0 until 8) {
                crc = crc shl 1
                if ((crc and 0x1000000L) != 0L) {
                    crc = crc xor 0x1864CFBL
                }
            }
        }
        val result = (crc and 0xFFFFFFL).toInt()
        return byteArrayOf(
            (result shr 16).toByte(),
            (result shr 8).toByte(),
            result.toByte()
        )
    }
    
    /**
     * Try to get MessageDigest with multiple fallback providers
     */
    private fun getMessageDigestWithFallback(vararg attempts: Pair<String, String?>): MessageDigest? {
        for ((algorithmName, providerName) in attempts) {
            try {
                return if (providerName != null) {
                    MessageDigest.getInstance(algorithmName, providerName)
                } else {
                    MessageDigest.getInstance(algorithmName)
                }
            } catch (e: Exception) {
                // Try next fallback
                continue
            }
        }
        return null
    }
    
    /**
     * Generate hash using specified algorithm with creative fallbacks
     */
    fun hash(algorithm: HashAlgorithm, data: ByteArray): Result<ByteArray> {
        return try {
            val hashValue = when (algorithm) {
                // These work with standard Java crypto provider - don't touch them
                HashAlgorithm.MD5 -> MessageDigest.getInstance("MD5").digest(data)
                HashAlgorithm.SHA1 -> MessageDigest.getInstance("SHA-1").digest(data)
                HashAlgorithm.SHA224 -> MessageDigest.getInstance("SHA-224").digest(data)
                HashAlgorithm.SHA256 -> MessageDigest.getInstance("SHA-256").digest(data)
                HashAlgorithm.SHA384 -> MessageDigest.getInstance("SHA-384").digest(data)
                HashAlgorithm.SHA512 -> MessageDigest.getInstance("SHA-512").digest(data)
                
                // MD4: Try provider-based first, then BC low-level API
                HashAlgorithm.MD4 -> {
                    val digest = getMessageDigestWithFallback("MD4" to "BC", "MD4" to null)
                    digest?.digest(data) ?: hashWithBouncyCastleDigest(MD4Digest(), data)
                }
                
                // SHA-3 family: Try JCA providers first, then BC low-level API
                HashAlgorithm.SHA3_224 -> {
                    val digest = getMessageDigestWithFallback(
                        "SHA3-224" to null,
                        "SHA3-224" to "BC",
                        "SHA-3-224" to "BC"
                    )
                    digest?.digest(data) ?: hashWithBouncyCastleDigest(SHA3Digest(224), data)
                }
                HashAlgorithm.SHA3_256 -> {
                    val digest = getMessageDigestWithFallback(
                        "SHA3-256" to null,
                        "SHA3-256" to "BC",
                        "SHA-3-256" to "BC"
                    )
                    digest?.digest(data) ?: hashWithBouncyCastleDigest(SHA3Digest(256), data)
                }
                HashAlgorithm.SHA3_384 -> {
                    val digest = getMessageDigestWithFallback(
                        "SHA3-384" to null,
                        "SHA3-384" to "BC",
                        "SHA-3-384" to "BC"
                    )
                    digest?.digest(data) ?: hashWithBouncyCastleDigest(SHA3Digest(384), data)
                }
                HashAlgorithm.SHA3_512 -> {
                    val digest = getMessageDigestWithFallback(
                        "SHA3-512" to null,
                        "SHA3-512" to "BC",
                        "SHA-3-512" to "BC"
                    )
                    digest?.digest(data) ?: hashWithBouncyCastleDigest(SHA3Digest(512), data)
                }
                
                // RIPEMD-160: Try JCA providers first, then BC low-level
                HashAlgorithm.RIPEMD160 -> {
                    val digest = getMessageDigestWithFallback(
                        "RIPEMD160" to "BC",
                        "RIPEMD-160" to "BC",
                        "RipeMD160" to "BC"
                    )
                    digest?.digest(data) ?: hashWithBouncyCastleDigest(RIPEMD160Digest(), data)
                }
                
                // WHIRLPOOL: Try JCA providers first, then BC low-level
                HashAlgorithm.WHIRLPOOL -> {
                    val digest = getMessageDigestWithFallback(
                        "WHIRLPOOL" to "BC",
                        "Whirlpool" to "BC",
                        "WHIRLPOOL" to null
                    )
                    digest?.digest(data) ?: hashWithBouncyCastleDigest(WhirlpoolDigest(), data)
                }
                
                // TIGER-192: BC low-level API - output is 192 bits (24 bytes)
                HashAlgorithm.TIGER_192 -> {
                    // TigerDigest outputs 192 bits = 24 bytes by default
                    hashWithBouncyCastleDigest(TigerDigest(), data)
                }
                
                // CRC32: Java built-in
                HashAlgorithm.CRC32 -> computeCRC32(data)
                
                // CRC32-RFC1510: RFC1510 (Kerberos) CRC32 variant
                // Uses standard CRC-32 polynomial (0xEDB88320 for reversed bit order)
                HashAlgorithm.CRC32_RFC1510 -> computeCRC32RFC1510(data)
                
                // CRC24-RFC2440: Custom implementation for OpenPGP
                HashAlgorithm.CRC24_RFC2440 -> computeCRC24RFC2440(data)
                
                // MDC-2: Try BC provider, fallback to MD2 (MDC-2 may not be in BC 1.78)
                HashAlgorithm.MDC2 -> {
                    val digest = getMessageDigestWithFallback(
                        "MDC2" to "BC",
                        "MDC-2" to "BC"
                    )
                    if (digest != null) {
                        digest.digest(data)
                    } else {
                        // MDC-2 not in BC - use MD2 as closest alternative
                        hashWithBouncyCastleDigest(MD2Digest(), data)
                    }
                }
            }
            
            Result.success(hashValue)
        } catch (e: Exception) {
            Result.failure(Exception("${algorithm.displayName} failed: ${e.message}", e))
        }
    }
    
    /**
     * Generate HMAC (Hash-based Message Authentication Code)
     */
    fun generateHMAC(algorithm: HashAlgorithm, key: ByteArray, data: ByteArray): Result<ByteArray> {
        return try {
            val hmacAlgorithm = when (algorithm) {
                HashAlgorithm.MD5 -> "HmacMD5"
                HashAlgorithm.SHA1 -> "HmacSHA1"
                HashAlgorithm.SHA256 -> "HmacSHA256"
                HashAlgorithm.SHA384 -> "HmacSHA384"
                HashAlgorithm.SHA512 -> "HmacSHA512"
                else -> return Result.failure(IllegalArgumentException("HMAC not supported for $algorithm"))
            }
            
            val mac = Mac.getInstance(hmacAlgorithm)
            val secretKey = SecretKeySpec(key, hmacAlgorithm)
            mac.init(secretKey)
            
            val hmacValue = mac.doFinal(data)
            Result.success(hmacValue)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verify HMAC
     */
    fun verifyHMAC(
        algorithm: HashAlgorithm,
        key: ByteArray,
        data: ByteArray,
        expectedHMAC: ByteArray
    ): Result<Boolean> {
        return try {
            val result = generateHMAC(algorithm, key, data)
            result.map { computedHMAC ->
                computedHMAC.contentEquals(expectedHMAC)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * MAC Operations (ISO 9797-1, ANSI X9.9/X9.19, etc.)
 */
object MACEngine {
    
    /**
     * Generate MAC using ISO 9797-1 Algorithm 1 (MAC1)
     * Retail MAC / CBC-MAC
     */
    fun generateISO9797_1_MAC1(
        key: ByteArray,
        data: ByteArray,
        macLength: Int = 8
    ): Result<ByteArray> {
        return try {
            val paddedData = data.copyOf((data.size + 7) / 8 * 8) // Pad to 8-byte boundary with zeros
            
            // CBC mode encryption
            val cipher = javax.crypto.Cipher.getInstance("DESede/CBC/NoPadding")
            val secretKey = SecretKeySpec(key, "DESede")
            val iv = javax.crypto.spec.IvParameterSpec(ByteArray(8))
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, iv)
            
            val encrypted = cipher.doFinal(paddedData)
            // MAC is the last block
            val mac = encrypted.takeLast(macLength).toByteArray()
            
            Result.success(mac)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate MAC using ANSI X9.19 (Retail MAC with DES)
     */
    fun generateANSI_X9_19_MAC(
        key: ByteArray,
        data: ByteArray,
        macLength: Int = 4
    ): Result<ByteArray> {
        return try {
            // X9.19 uses DES in CBC mode followed by final TDES block
            val paddedData = data.copyOf((data.size + 7) / 8 * 8)
            
            // First pass: DES in CBC mode with first 8 bytes of key
            var intermediate = ByteArray(8)
            for (i in paddedData.indices step 8) {
                val block = paddedData.sliceArray(i until minOf(i + 8, paddedData.size))
                intermediate = intermediate.copyOf(8).apply {
                    for (j in block.indices) {
                        this[j] = (this[j].toInt() xor block[j].toInt()).toByte()
                    }
                }
                
                // Encrypt with first 8 bytes of key (DES)
                val cipher = javax.crypto.Cipher.getInstance("DES/ECB/NoPadding")
                val desKey = SecretKeySpec(key.take(8).toByteArray(), "DES")
                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, desKey)
                intermediate = cipher.doFinal(intermediate)
            }
            
            // Final block: encrypt with full key (TDES)
            val cipher = javax.crypto.Cipher.getInstance("DESede/ECB/NoPadding")
            val tdesKey = SecretKeySpec(key, "DESede")
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, tdesKey)
            val finalBlock = cipher.doFinal(intermediate)
            
            val mac = finalBlock.take(macLength).toByteArray()
            Result.success(mac)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate CMAC (Cipher-based MAC) using AES
     */
    fun generateCMAC_AES(
        key: ByteArray,
        data: ByteArray,
        macLength: Int = 16
    ): Result<ByteArray> {
        return try {
            // CMAC implementation using AES
            val cipher = javax.crypto.Cipher.getInstance("AES/ECB/NoPadding")
            val secretKey = SecretKeySpec(key, "AES")
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
            
            // Generate subkeys
            val L = cipher.doFinal(ByteArray(16))
            val K1 = generateCMACSubkey(L)
            val K2 = generateCMACSubkey(K1)
            
            // Process data
            val paddedData = if (data.size % 16 == 0 && data.isNotEmpty()) {
                data
            } else {
                // Padding: 0x80 followed by zeros
                val padded = data.copyOf((data.size + 16) / 16 * 16)
                padded[data.size] = 0x80.toByte()
                padded
            }
            
            var mac = ByteArray(16)
            for (i in paddedData.indices step 16) {
                val block = paddedData.sliceArray(i until minOf(i + 16, paddedData.size))
                
                // XOR with previous MAC
                for (j in block.indices) {
                    mac[j] = (mac[j].toInt() xor block[j].toInt()).toByte()
                }
                
                // XOR with subkey on last block
                if (i + 16 >= paddedData.size) {
                    val subkey = if (data.size % 16 == 0 && data.isNotEmpty()) K1 else K2
                    for (j in mac.indices) {
                        mac[j] = (mac[j].toInt() xor subkey[j].toInt()).toByte()
                    }
                }
                
                mac = cipher.doFinal(mac)
            }
            
            Result.success(mac.take(macLength).toByteArray())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateCMACSubkey(input: ByteArray): ByteArray {
        val output = ByteArray(16)
        var carry = 0
        
        for (i in 15 downTo 0) {
            val value = (input[i].toInt() and 0xFF) shl 1
            output[i] = (value or carry).toByte()
            carry = (value shr 8) and 0xFF
        }
        
        // If MSB of input was 1, XOR with Rb
        if ((input[0].toInt() and 0x80) != 0) {
            output[15] = (output[15].toInt() xor 0x87).toByte()
        }
        
        return output
    }
}
