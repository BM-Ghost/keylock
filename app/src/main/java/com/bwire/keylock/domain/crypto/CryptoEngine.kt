package com.bwire.keylock.domain.crypto

import java.security.InvalidKeyException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES Cryptographic Operations
 * Implements AES encryption/decryption with multiple modes
 * 
 * Modes supported:
 * - ECB (Electronic Codebook)
 * - CBC (Cipher Block Chaining)
 * - CFB (Cipher Feedback)
 * - OFB (Output Feedback)
 * - KCV (Key Check Value)
 */
object AESCryptoEngine {
    
    /**
     * Encrypt data using AES
     */
    fun encrypt(
        algorithm: AESAlgorithm,
        mode: CipherMode,
        key: ByteArray,
        data: ByteArray,
        iv: ByteArray? = null
    ): Result<ByteArray> {
        return try {
            validateKey(algorithm, key)
            
            val transformation = when (mode) {
                CipherMode.ECB -> "AES/ECB/NoPadding"
                CipherMode.CBC -> "AES/CBC/NoPadding"
                CipherMode.CFB -> "AES/CFB/NoPadding"
                CipherMode.OFB -> "AES/OFB/NoPadding"
                CipherMode.KCV -> return generateKCV(key)
            }

            if (mode.requiresIV) {
                if (iv == null || iv.size != 16) {
                    return Result.failure(IllegalArgumentException("IV must be 16 bytes (32 hex chars) for ${mode.displayName}"))
                }
            }

            if ((mode == CipherMode.ECB || mode == CipherMode.CBC) && data.size % 16 != 0) {
                return Result.failure(IllegalArgumentException("Data length must be a multiple of 16 bytes for ${mode.displayName} without padding"))
            }
            
            val cipher = Cipher.getInstance(transformation)
            val secretKey = SecretKeySpec(key, "AES")
            
            if (mode.requiresIV) {
                val ivSpec = IvParameterSpec(iv!!)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            }
            
            val encrypted = cipher.doFinal(data)
            Result.success(encrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Decrypt data using AES
     */
    fun decrypt(
        algorithm: AESAlgorithm,
        mode: CipherMode,
        key: ByteArray,
        data: ByteArray,
        iv: ByteArray? = null
    ): Result<ByteArray> {
        return try {
            validateKey(algorithm, key)
            
            if (mode == CipherMode.KCV) {
                return Result.failure(IllegalArgumentException("KCV mode cannot be used for decryption"))
            }
            
            val transformation = when (mode) {
                CipherMode.ECB -> "AES/ECB/NoPadding"
                CipherMode.CBC -> "AES/CBC/NoPadding"
                CipherMode.CFB -> "AES/CFB/NoPadding"
                CipherMode.OFB -> "AES/OFB/NoPadding"
                else -> return Result.failure(IllegalArgumentException("Invalid mode for decryption"))
            }

            if (mode.requiresIV) {
                if (iv == null || iv.size != 16) {
                    return Result.failure(IllegalArgumentException("IV must be 16 bytes (32 hex chars) for ${mode.displayName}"))
                }
            }

            if ((mode == CipherMode.ECB || mode == CipherMode.CBC) && data.size % 16 != 0) {
                return Result.failure(IllegalArgumentException("Data length must be a multiple of 16 bytes for ${mode.displayName} without padding"))
            }
            
            val cipher = Cipher.getInstance(transformation)
            val secretKey = SecretKeySpec(key, "AES")
            
            if (mode.requiresIV) {
                val ivSpec = IvParameterSpec(iv!!)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
            }
            
            val decrypted = cipher.doFinal(data)
            Result.success(decrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate Key Check Value (KCV)
     * Encrypts a block of zeros with the key and returns first 3 bytes
     */
    fun generateKCV(key: ByteArray): Result<ByteArray> {
        return try {
            if (key.size != 16 && key.size != 24 && key.size != 32) {
                return Result.failure(
                    InvalidKeyException(
                        "Invalid key length: expected 16, 24, or 32 bytes, got ${key.size} bytes"
                    )
                )
            }

            val cipher = Cipher.getInstance("AES/ECB/NoPadding")
            val secretKey = SecretKeySpec(key, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val zeros = ByteArray(16) { 0 }
            val encrypted = cipher.doFinal(zeros)
            
            // KCV is first 3 bytes of encrypted zero block
            val kcv = encrypted.copyOf(3)
            Result.success(kcv)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate random AES key
     */
    fun generateKey(algorithm: AESAlgorithm): Result<ByteArray> {
        return try {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(algorithm.keyBits, SecureRandom())
            val secretKey = keyGen.generateKey()
            Result.success(secretKey.encoded)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate key length for algorithm
     */
    private fun validateKey(algorithm: AESAlgorithm, key: ByteArray) {
        if (key.size != algorithm.keyBytes) {
            throw InvalidKeyException(
                "Invalid key length: expected ${algorithm.keyBytes} bytes, got ${key.size} bytes"
            )
        }
    }
}

/**
 * DES/TDES Cryptographic Operations
 */
object DESCryptoEngine {
    
    /**
     * Encrypt data using DES/TDES (new API with DESMode and DesPadding)
     */
    fun encrypt(
        algorithm: DESAlgorithm,
        mode: DESMode,
        padding: DesPadding,
        key: ByteArray,
        data: ByteArray,
        iv: ByteArray? = null
    ): Result<ByteArray> {
        return try {
            val keyAlgo = if (algorithm == DESAlgorithm.DES) "DES" else "DESede"
            val transformation = "$keyAlgo/${mode.transformation}/${padding.paddingName}"
            
            val cipher = Cipher.getInstance(transformation)
            val secretKey = SecretKeySpec(key, keyAlgo)
            
            if (mode.requiresIV) {
                if (iv == null || iv.size != 8) {
                    return Result.failure(IllegalArgumentException("IV must be 8 bytes (16 hex chars) for ${mode.displayName}"))
                }
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            }
            
            val encrypted = cipher.doFinal(data)
            Result.success(encrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Decrypt data using DES/TDES (new API with DESMode and DesPadding)
     */
    fun decrypt(
        algorithm: DESAlgorithm,
        mode: DESMode,
        padding: DesPadding,
        key: ByteArray,
        data: ByteArray,
        iv: ByteArray? = null
    ): Result<ByteArray> {
        return try {
            val keyAlgo = if (algorithm == DESAlgorithm.DES) "DES" else "DESede"
            val transformation = "$keyAlgo/${mode.transformation}/${padding.paddingName}"
            
            val cipher = Cipher.getInstance(transformation)
            val secretKey = SecretKeySpec(key, keyAlgo)
            
            if (mode.requiresIV) {
                if (iv == null || iv.size != 8) {
                    return Result.failure(IllegalArgumentException("IV must be 8 bytes (16 hex chars) for ${mode.displayName}"))
                }
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
            }
            
            val decrypted = cipher.doFinal(data)
            Result.success(decrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Encrypt data using DES/TDES (legacy API with CipherMode)
     */
    fun encrypt(
        algorithm: DESAlgorithm,
        mode: CipherMode,
        key: ByteArray,
        data: ByteArray,
        iv: ByteArray? = null
    ): Result<ByteArray> {
        return try {
            val transformation = when {
                algorithm == DESAlgorithm.DES && mode == CipherMode.ECB -> "DES/ECB/PKCS5Padding"
                algorithm == DESAlgorithm.DES && mode == CipherMode.CBC -> "DES/CBC/PKCS5Padding"
                algorithm != DESAlgorithm.DES && mode == CipherMode.ECB -> "DESede/ECB/PKCS5Padding"
                algorithm != DESAlgorithm.DES && mode == CipherMode.CBC -> "DESede/CBC/PKCS5Padding"
                mode == CipherMode.KCV -> return generateKCV(algorithm, key)
                else -> return Result.failure(IllegalArgumentException("Unsupported mode: $mode"))
            }
            
            val cipher = Cipher.getInstance(transformation)
            val keyAlgo = if (algorithm == DESAlgorithm.DES) "DES" else "DESede"
            val secretKey = SecretKeySpec(key, keyAlgo)
            
            if (mode.requiresIV && iv != null) {
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            }
            
            val encrypted = cipher.doFinal(data)
            Result.success(encrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Decrypt data using DES/TDES
     */
    fun decrypt(
        algorithm: DESAlgorithm,
        mode: CipherMode,
        key: ByteArray,
        data: ByteArray,
        iv: ByteArray? = null
    ): Result<ByteArray> {
        return try {
            if (mode == CipherMode.KCV) {
                return Result.failure(IllegalArgumentException("KCV mode cannot be used for decryption"))
            }
            
            val transformation = when {
                algorithm == DESAlgorithm.DES && mode == CipherMode.ECB -> "DES/ECB/PKCS5Padding"
                algorithm == DESAlgorithm.DES && mode == CipherMode.CBC -> "DES/CBC/PKCS5Padding"
                algorithm != DESAlgorithm.DES && mode == CipherMode.ECB -> "DESede/ECB/PKCS5Padding"
                algorithm != DESAlgorithm.DES && mode == CipherMode.CBC -> "DESede/CBC/PKCS5Padding"
                else -> return Result.failure(IllegalArgumentException("Unsupported mode: $mode"))
            }
            
            val cipher = Cipher.getInstance(transformation)
            val keyAlgo = if (algorithm == DESAlgorithm.DES) "DES" else "DESede"
            val secretKey = SecretKeySpec(key, keyAlgo)
            
            if (mode.requiresIV && iv != null) {
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
            }
            
            val decrypted = cipher.doFinal(data)
            Result.success(decrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate Key Check Value (KCV) for DES/TDES
     */
    fun generateKCV(algorithm: DESAlgorithm, key: ByteArray): Result<ByteArray> {
        return try {
            val keyAlgo = if (algorithm == DESAlgorithm.DES) "DES" else "DESede"
            val transformation = "$keyAlgo/ECB/NoPadding"
            
            val cipher = Cipher.getInstance(transformation)
            val secretKey = SecretKeySpec(key, keyAlgo)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val zeros = ByteArray(8) { 0 }
            val encrypted = cipher.doFinal(zeros)
            
            // KCV is first 3 bytes
            val kcv = encrypted.copyOf(3)
            Result.success(kcv)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
