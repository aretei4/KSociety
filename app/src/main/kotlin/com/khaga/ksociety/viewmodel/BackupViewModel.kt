package com.khaga.ksociety.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.khaga.ksociety.api.BackupManager
import com.khaga.ksociety.api.DbStats
import com.khaga.ksociety.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BackupViewModel(app: Application) : AndroidViewModel(app) {

    data class BackupResult(val success: Boolean, val message: String)

    private val _loading = MutableLiveData(false)
    private val _result  = MutableLiveData<BackupResult>()
    private val _stats   = MutableLiveData<DbStats>()

    val loading: LiveData<Boolean>      = _loading
    val result:  LiveData<BackupResult> = _result
    val stats:   LiveData<DbStats>      = _stats
    private val db        = AppDatabase.getInstance(app)
    fun refreshStats() {
        viewModelScope.launch {
            _stats.value = BackupManager.getDbStats(getApplication())
        }
    }

    fun clearDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            db.clearAllData()
          //  loadAll()
        }
    }

    fun performBackup(ctx: Context) {
        viewModelScope.launch {
            _loading.value = true
            val outcome = BackupManager.performBackup(ctx)
            _loading.value = false
            if (outcome.isSuccess) {
                val r = outcome.getOrThrow()
                _result.value = BackupResult(true,
                    "✅ Backup saved!\n" +
                            "${r.fundsCount} funds · ${r.membersCount} members · ${r.paymentsCount} payments\n" +
                            "File: ${r.fileName}")
            } else {
                _result.value = BackupResult(false,
                    "❌ Backup failed\n${outcome.exceptionOrNull()?.message}")
            }
        }
    }

    fun performRestore(ctx: Context) {
        viewModelScope.launch {
            _loading.value = true
            val outcome = BackupManager.performRestore(ctx)
            _loading.value = false
            _result.value = if (outcome.isSuccess) {
                BackupResult(true, "✅ Restore complete!\n${outcome.getOrThrow()}")
            } else {
                BackupResult(false, "❌ Restore failed\n${outcome.exceptionOrNull()?.message}")
            }
        }
    }

    fun getCurrentUrl(ctx: Context) = BackupManager.getBaseUrl(ctx)
    fun saveBaseUrl(ctx: Context, url: String) = BackupManager.setBaseUrl(ctx, url)
    fun getDeviceId(ctx: Context) = BackupManager.getDeviceId(ctx)
    fun saveDeviceId(ctx: Context, id: String) = BackupManager.setDeviceId(ctx, id)
}