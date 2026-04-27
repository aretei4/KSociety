package com.khaga.ksociety.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.khaga.ksociety.database.AppDatabase
import com.khaga.ksociety.database.FundDao
import com.khaga.ksociety.model.Fund
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddFundViewModel(app: Application) : AndroidViewModel(app) {

    private val fundDao = FundDao(AppDatabase.getInstance(app))

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    fun saveFund(fund: Fund) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) { fundDao.insert(fund) }
            _saveResult.postValue(id > 0)
        }
    }
}
