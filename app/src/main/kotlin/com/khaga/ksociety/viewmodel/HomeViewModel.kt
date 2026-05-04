package com.khaga.ksociety.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.khaga.ksociety.database.AppDatabase
import com.khaga.ksociety.database.FundDao
import com.khaga.ksociety.database.MemberDao
import com.khaga.ksociety.model.Fund
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val db        = AppDatabase.getInstance(app)
    private val fundDao   = FundDao(db)
    private val memberDao = MemberDao(db)

    private val _funds         = MutableLiveData<List<Fund>>()
    private val _activeCount   = MutableLiveData<Int>()
    private val _memberCount   = MutableLiveData<Int>()
    private val _overdueCount  = MutableLiveData<Int>()

    val funds:        LiveData<List<Fund>> = _funds
    val activeCount:  LiveData<Int>        = _activeCount
    val memberCount:  LiveData<Int>        = _memberCount
    val overdueCount: LiveData<Int>        = _overdueCount

    fun loadAll() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val list = fundDao.getAll()
                _funds.postValue(list)
                _activeCount.postValue(list.count { !it.isClosed })
                _memberCount.postValue(memberDao.getTotalCount())
                _overdueCount.postValue(fundDao.getOverdueCount())
            }
        }
    }

    fun deleteFund(fundId: Long, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { fundDao.delete(fundId) }
            onDone(ok)
            if (ok) loadAll()
        }
    }

    fun clearDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            db.clearAllData()
            loadAll()
        }
    }

    fun seedDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            db.clearAllData()
            db.seedSampleData()
            loadAll()
        }
    }
}
