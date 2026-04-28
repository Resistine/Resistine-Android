package com.resistine.android.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.resistine.android.database.AppDatabase
import com.resistine.android.network.WazuhLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogUploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(applicationContext)
        val logDao = database.logDao()
        val prefs = applicationContext.getSharedPreferences("wazuh_prefs", Context.MODE_PRIVATE)
        
        val agentId = prefs.getString("agent_id", null)
        val agentKey = prefs.getString("agent_key", null)
        val agentName = prefs.getString("agent_name", null)
        val serverIp = "10.0.0.29" // Should ideally be retrieved from a config/prefs

        if (agentId == null || agentKey == null || agentName == null) {
            return@withContext Result.failure()
        }

        val pendingLogs = logDao.getPendingLogs()
        if (pendingLogs.isEmpty()) {
            return@withContext Result.success()
        }

        val logger = WazuhLogger(applicationContext, serverIp, 1514)
        
        try {
            // Establish connection and perform handshake
            val connected = logger.connectOneShot(agentId, agentKey)
            if (!connected) {
                Log.e("LogUploadWorker", "Failed to connect to Wazuh manager")
                return@withContext Result.retry()
            }
            
            var allSuccessful = true
            val uploadedIds = mutableListOf<Long>()

            pendingLogs.forEach { logEntry ->
                val success = logger.sendSingleLog(agentId, agentKey, logEntry.message)
                if (success) {
                    uploadedIds.add(logEntry.id)
                } else {
                    allSuccessful = false
                }
            }
            
            if (uploadedIds.isNotEmpty()) {
                logDao.deleteLogsByIds(uploadedIds)
            }
            
            if (allSuccessful) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e("LogUploadWorker", "Error uploading logs", e)
            Result.retry()
        } finally {
            logger.disconnect()
        }
    }
}