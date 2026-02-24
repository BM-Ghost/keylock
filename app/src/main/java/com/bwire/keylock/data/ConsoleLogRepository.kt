package com.bwire.keylock.data

import android.content.Context
import android.content.SharedPreferences
import com.bwire.keylock.ui.screens.crypto.ConsoleMessage
import com.bwire.keylock.ui.screens.crypto.MessageLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for persisting console log messages
 * Messages persist across app restarts until explicitly cleared
 */
class ConsoleLogRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "console_log_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_MESSAGES = "console_messages"
        private const val MAX_MESSAGES = 500 // Limit to prevent excessive storage
    }
    
    /**
     * Save console messages to persistent storage
     */
    suspend fun saveMessages(messages: List<ConsoleMessage>) = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            
            // Only save the most recent messages
            val messagesToSave = messages.takeLast(MAX_MESSAGES)
            
            messagesToSave.forEach { message ->
                val jsonObject = JSONObject().apply {
                    put("timestamp", message.timestamp)
                    put("level", message.level.name)
                    put("message", message.message)
                }
                jsonArray.put(jsonObject)
            }
            
            prefs.edit()
                .putString(KEY_MESSAGES, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Load console messages from persistent storage
     */
    suspend fun loadMessages(): List<ConsoleMessage> = withContext(Dispatchers.IO) {
        try {
            val messagesJson = prefs.getString(KEY_MESSAGES, null) ?: return@withContext emptyList()
            val jsonArray = JSONArray(messagesJson)
            val messages = mutableListOf<ConsoleMessage>()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val message = ConsoleMessage(
                    timestamp = jsonObject.getLong("timestamp"),
                    level = MessageLevel.valueOf(jsonObject.getString("level")),
                    message = jsonObject.getString("message")
                )
                messages.add(message)
            }
            
            messages
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Clear all console messages
     */
    suspend fun clearMessages() = withContext(Dispatchers.IO) {
        prefs.edit()
            .remove(KEY_MESSAGES)
            .apply()
    }
}
