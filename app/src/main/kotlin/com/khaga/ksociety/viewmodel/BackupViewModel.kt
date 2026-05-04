package com.khaga.ksociety.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.khaga.ksociety.api.BackupManager
import com.khaga.ksociety.api.RetrofitClient
import com.khaga.ksociety.database.AppDatabase
import com.khaga.ksociety.database.FundDao
import com.khaga.ksociety.database.MemberDao
import com.khaga.ksociety.database.PaymentDao
import com.khaga.ksociety.database.ReportDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BackupStats(val funds: Int, val members: Int, val payments: Int)
data class BackupResult(val success: Boolean, val message: String)

class BackupViewModel(app: Application) : AndroidViewModel(app) {

    private val db         = AppDatabase.getInstance(app)
    private val fundDao    = FundDao(db)
    private val memberDao  = MemberDao(db)
    private val paymentDao = PaymentDao(db)
    private val reportDao  = ReportDao(db)

    private val _stats  = MutableLiveData<BackupStats>()
    private val _result = MutableLiveData<BackupResult>()
    private val _loading = MutableLiveData<Boolean>()

    val stats:   LiveData<BackupStats>   = _stats
    val result:  LiveData<BackupResult>  = _result
    val loading: LiveData<Boolean>       = _loading

    fun refreshStats() {
        viewModelScope.launch(Dispatchers.IO) {
            _stats.postValue(BackupStats(
                funds    = fundDao.getAll().size,
                members  = memberDao.getAll().size,
                payments = paymentDao.getAll().size
            ))
        }
    }

    fun performBackup(context: android.content.Context) {
        _loading.value = true
        BackupManager.performBackup(context, fundDao, memberDao, paymentDao, reportDao,
            object : BackupManager.BackupCallback {
                override fun onSuccess(message: String) {
                    _loading.postValue(false)
                    _result.postValue(BackupResult(true, message))
                }
                override fun onFailure(error: String) {
                    _loading.postValue(false)
                    _result.postValue(BackupResult(false, error))
                }
            })
    }

    fun performRestore(context: android.content.Context) {
        _loading.value = true
        BackupManager.performRestore(context,
            object : BackupManager.BackupCallback {
                override fun onSuccess(message: String) {
                    _loading.postValue(false)
                    _result.postValue(BackupResult(true, message))
                    refreshStats()
                }
                override fun onFailure(error: String) {
                    _loading.postValue(false)
                    _result.postValue(BackupResult(false, error))
                }
            })
    }

    fun clearAllData(onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clearAllData()
            refreshStats()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun saveBaseUrl(context: android.content.Context, url: String) {
        RetrofitClient.setBaseUrl(context, url)
    }

    fun getCurrentUrl(context: android.content.Context): String =
        RetrofitClient.getCurrentBaseUrl(context)
}
