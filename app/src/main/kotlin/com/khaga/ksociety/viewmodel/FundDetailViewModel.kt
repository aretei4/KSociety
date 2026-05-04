package com.khaga.ksociety.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.khaga.ksociety.database.AppDatabase
import com.khaga.ksociety.database.FundDao
import com.khaga.ksociety.database.MemberDao
import com.khaga.ksociety.database.PaymentDao
import com.khaga.ksociety.database.ReportDao
import com.khaga.ksociety.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FundDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val db         = AppDatabase.getInstance(app)
    private val fundDao    = FundDao(db)
    private val memberDao  = MemberDao(db)
    private val paymentDao = PaymentDao(db)
    private val reportDao  = ReportDao(db)

    private val _fund     = MutableLiveData<Fund?>()
    private val _members  = MutableLiveData<List<Member>>()
    private val _payments = MutableLiveData<List<Payment>>()
    private val _reports  = MutableLiveData<List<MonthlyReport>>()

    val fund:     LiveData<Fund?>               = _fund
    val members:  LiveData<List<Member>>        = _members
    val payments: LiveData<List<Payment>>       = _payments
    val reports:  LiveData<List<MonthlyReport>> = _reports

    fun load(fundId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _fund.postValue(fundDao.getById(fundId))
            loadMembersInternal(fundId)
            loadPaymentsInternal(fundId)
            loadReportsInternal(fundId)
        }
    }

    fun loadMembers(fundId: Long, query: String = "") {
        viewModelScope.launch(Dispatchers.IO) { loadMembersInternal(fundId, query) }
    }

    fun loadPayments(fundId: Long) {
        viewModelScope.launch(Dispatchers.IO) { loadPaymentsInternal(fundId) }
    }

    private suspend fun loadMembersInternal(fundId: Long, query: String = "") {
        _members.postValue(
            if (query.isBlank()) memberDao.getByFund(fundId)
            else memberDao.search(fundId, query)
        )
    }

    private suspend fun loadPaymentsInternal(fundId: Long) {
        _payments.postValue(paymentDao.getByFund(fundId))
    }

    private suspend fun loadReportsInternal(fundId: Long) {
        _reports.postValue(reportDao.getByFund(fundId))
    }

    /** Insert multiple members in a single IO transaction; returns count inserted. */
    fun bulkInsertMembers(members: List<Member>, onDone: (Int) -> Unit) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                var inserted = 0
                members.forEach { m -> if (memberDao.insert(m) > 0) inserted++ }
                inserted
            }
            onDone(count)
            if (count > 0) withContext(Dispatchers.IO) {
                members.firstOrNull()?.fundId?.let { loadMembersInternal(it) }
            }
        }
    }

    fun insertMember(member: Member, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) { memberDao.insert(member) }
            onDone(id > 0)
            if (id > 0) withContext(Dispatchers.IO) { loadMembersInternal(member.fundId) }
        }
    }

    fun updateMember(member: Member, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { memberDao.update(member) }
            onDone(ok)
            if (ok) withContext(Dispatchers.IO) { loadMembersInternal(member.fundId) }
        }
    }

    /** Delete member + all their payments */
    fun deleteMember(memberId: Long, fundId: Long, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { memberDao.delete(memberId) }
            onDone(ok)
            if (ok) {
                withContext(Dispatchers.IO) {
                    loadMembersInternal(fundId)
                    loadPaymentsInternal(fundId)
                }
            }
        }
    }

    fun deletePayment(paymentId: Long, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { paymentDao.delete(paymentId) }
            onDone(ok)
        }
    }

    fun updatePayment(payment: Payment, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { paymentDao.update(payment) }
            onDone(ok)
            if (ok) withContext(Dispatchers.IO) { loadPaymentsInternal(payment.fundId) }
        }
    }

    fun insertPayment(payment: Payment, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) { paymentDao.insert(payment) }
            onDone(id > 0)
            if (id > 0) withContext(Dispatchers.IO) { loadPaymentsInternal(payment.fundId) }
        }
    }

    /** Delete fund — called from HomeFragment */
    fun deleteFund(fundId: Long, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { fundDao.delete(fundId) }
            onDone(ok)
        }
    }
}