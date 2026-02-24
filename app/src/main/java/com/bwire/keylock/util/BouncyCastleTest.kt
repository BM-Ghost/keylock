package com.bwire.keylock.util

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.MessageDigest
import java.security.Security

/**
 * Test Bouncy Castle algorithms availability
 */
object BouncyCastleTest {
    
    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }
    
    /**
     * Get all available algorithms from all providers
     */
    fun listAllDigestAlgorithms(): Map<String, List<String>> {
        val algorithms = mutableMapOf<String, MutableList<String>>()
        
        Security.getProviders().forEach { provider ->
            provider.services.forEach { service ->
                if (service.type == "MessageDigest") {
                    algorithms.getOrPut(provider.name) { mutableListOf() }
                        .add(service.algorithm)
                }
            }
        }
        
        return algorithms
    }
    
    /**
     * Test specific algorithm
     */
    fun testAlgorithm(algorithm: String, provider: String? = null): Result<String> {
        return try {
            val digest = if (provider != null) {
                MessageDigest.getInstance(algorithm, provider)
            } else {
                MessageDigest.getInstance(algorithm)
            }
            val testData = "test".toByteArray()
            val hash = digest.digest(testData)
            Result.success("OK - ${hash.size} bytes")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
