package com.bwire.keylock.domain.crypto

import com.bwire.keylock.util.calculateLuhnCheckDigit
import com.bwire.keylock.util.verifyLuhn

/**
 * Check Digit Engine
 * Handles check digit generation and validation
 * 
 * Supported methods:
 * - Luhn's number (MOD 10)
 * - Amex SE Number (MOD 9)
 */
object CheckDigitEngine {
    
    /**
     * Generate check digit for input data
     */
    fun generate(
        input: String,
        method: CheckDigitMethod
    ): Result<String> {
        return try {
            // Validate input contains only digits
            val cleaned = input.replace("\\s".toRegex(), "")
            if (!cleaned.all { it.isDigit() }) {
                throw IllegalArgumentException("Input must contain only digits")
            }
            
            if (cleaned.isEmpty()) {
                throw IllegalArgumentException("Input cannot be empty")
            }
            
            val digit = when (method) {
                CheckDigitMethod.LUHN -> generateLuhnCheckDigit(cleaned)
                CheckDigitMethod.AMEX_SE -> generateAmexSECheckDigit(cleaned)
            }
            
            Result.success(digit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate check digit in input data
     */
    fun check(
        input: String,
        method: CheckDigitMethod
    ): Result<Boolean> {
        return try {
            // Validate input contains only digits
            val cleaned = input.replace("\\s".toRegex(), "")
            if (!cleaned.all { it.isDigit() }) {
                throw IllegalArgumentException("Input must contain only digits")
            }
            
            if (cleaned.isEmpty()) {
                throw IllegalArgumentException("Input cannot be empty")
            }
            
            val isValid = when (method) {
                CheckDigitMethod.LUHN -> checkLuhn(cleaned)
                CheckDigitMethod.AMEX_SE -> checkAmexSE(cleaned)
            }
            
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate Luhn check digit
     * Calculates the Luhn MOD 10 check digit for the input
     */
    private fun generateLuhnCheckDigit(input: String): String {
        return input.calculateLuhnCheckDigit().toString()
    }
    
    /**
     * Check Luhn validity
     * Validates if last digit matches check digit for the base
     */
    private fun checkLuhn(input: String): Boolean {
        return input.verifyLuhn()
    }
    
    /**
     * Generate Amex SE Number (MOD 9) check digit
     * Uses first 8 digits only for calculation
     */
    private fun generateAmexSECheckDigit(input: String): String {
        // Use only first 8 digits for Amex SE calculation
        val dataToProcess = if (input.length >= 8) input.take(8) else input
        
        var sum = 0
        for (char in dataToProcess) {
            sum += char.digitToInt()
        }
        
        val remainder = sum % 9
        
        // Per Amex SE spec: if remainder is 0, return -1
        return if (remainder == 0) "-1" else remainder.toString()
    }
    
    /**
     * Check Amex SE Number (MOD 9) validity
     * Uses first 8 digits for calculation, validates against a specific position
     */
    private fun checkAmexSE(input: String): Boolean {
        if (input.length < 8) return false
        
        // Use first 8 digits for calculation
        val dataToProcess = input.take(8)
        var sum = 0
        for (char in dataToProcess) {
            sum += char.digitToInt()
        }
        
        val remainder = sum % 9
        
        // If remainder is 0, it should match -1 condition (invalid/fail)
        // Otherwise validate based on the calculated digit
        if (remainder == 0) {
            return false  // -1 case means no valid check digit can exist
        }
        
        // For non-zero remainders, check needs additional logic
        // For now, comparing with calculated expected value
        return remainder == (input.drop(8).take(1).toIntOrNull() ?: -1)
    }
}
