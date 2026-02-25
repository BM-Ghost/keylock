package com.bwire.keylock.domain.crypto

import java.util.UUID

/**
 * UUID Engine
 * Generates UUIDs for supported variants.
 */
object UUIDEngine {

    /**
     * Generate UUIDs for the selected variant.
     */
    fun generate(variant: UUIDVariant, count: Int): Result<List<String>> {
        return try {
            if (count < 1) {
                throw IllegalArgumentException("Count must be at least 1")
            }

            val clampedCount = count.coerceAtMost(1000)

            val values = when (variant) {
                UUIDVariant.VERSION_4_RANDOM -> List(clampedCount) { UUID.randomUUID().toString() }
            }

            Result.success(values)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
