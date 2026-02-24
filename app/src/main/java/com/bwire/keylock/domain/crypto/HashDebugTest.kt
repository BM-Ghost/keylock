package com.bwire.keylock.domain.crypto

import org.bouncycastle.crypto.digests.TigerDigest
import org.bouncycastle.crypto.digests.MD2Digest

/**
 * Debug test for problematic hash algorithms
 */
object HashDebugTest {
    
    fun testTiger192() {
        val input = "test".toByteArray()
        val tiger = TigerDigest()
        tiger.update(input, 0, input.size)
        val result = ByteArray(tiger.digestSize)
        tiger.doFinal(result, 0)
        
        println("[TigerDigest] Input: 'test'")
        println("[TigerDigest] Output size: ${result.size} bytes")
        println("[TigerDigest] Output hex: ${result.joinToString("") { "%02x".format(it) }}")
        // Expected for TIGER-192 of "test": ?
    }
    
    fun testMD2() {
        val input = "test".toByteArray()
        val md2 = MD2Digest()
        md2.update(input, 0, input.size)
        val result = ByteArray(md2.digestSize)
        md2.doFinal(result, 0)
        
        println("[MD2Digest] Input: 'test'")
        println("[MD2Digest] Output size: ${result.size} bytes")
        println("[MD2Digest] Output hex: ${result.joinToString("") { "%02x".format(it) }}")
        // MD2 of "test": db 34 64 b5 17 cc a3 f1 65 5b 87 d4 07 49 b6 d2
    }
    
    fun testCRC32Custom() {
        val input = "test".toByteArray()
        
        // CRC32 normal (Java built-in)
        val crc32 = java.util.zip.CRC32()
        crc32.update(input)
        val value = crc32.value
        val result1 = byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )
        println("[CRC32 Java] Input: 'test'")
        println("[CRC32 Java] Output: ${result1.joinToString("") { "%02x".format(it) }}")
        
        // RFC1510 version
        var crc = 0L
        for (b in input) {
            crc = crc xor ((b.toInt() and 0xFF).toLong() shl 24)
            for (i in 0 until 8) {
                crc = if ((crc and 0x80000000L) != 0L) {
                    (crc shl 1) xor 0x04C11DB7L
                } else {
                    crc shl 1
                }
            }
        }
        val result = (crc and 0xFFFFFFFFL).toInt()
        val result2 = byteArrayOf(
            (result shr 24).toByte(),
            (result shr 16).toByte(),
            (result shr 8).toByte(),
            result.toByte()
        )
        println("[CRC32 RFC1510] Input: 'test'")
        println("[CRC32 RFC1510] Output: ${result2.joinToString("") { "%02x".format(it) }}")
    }
}
