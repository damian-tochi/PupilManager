package com.bridge.androidtechnicaltest.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bridge.androidtechnicaltest.db.IPupilRepository
import com.bridge.androidtechnicaltest.db.Pupil
import com.bridge.androidtechnicaltest.db.PupilList
import com.bridge.androidtechnicaltest.db.SyncStatus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class PupilListViewModel (private val repository: IPupilRepository) : ViewModel() {

    private val _pupils = MutableLiveData<PupilList>()
    val pupils: LiveData<PupilList> = _pupils

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _updateError = MutableLiveData<String>()
    val updateError: LiveData<String> = _updateError

    private val _updateSuccess = MutableLiveData<String>()
    val updateSuccess: LiveData<String> = _updateSuccess

    private val _deleteSuccess = MutableLiveData<String>()
    val deleteSuccess: LiveData<String> = _deleteSuccess

    private val compositeDisposable = CompositeDisposable()


    fun loadPupils(page: Int = 1) {
        val disposable = repository.getOrFetchPupils()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                _pupils.value = result
            }, { e ->
                _error.value = e.message
                Log.e("PupilListViewModel", "Error fetching pupil: ${e.message}")
            })

        compositeDisposable.add(disposable)
    }

    fun createPupil(pupil: Pupil) {
        val disposable = repository.createPupil(pupil)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _updateSuccess.value = "Pupil created successfully"
            }, { e ->
                _updateError.value = e.message
                Log.e("PupilListViewModel", "Error creating pupil: ${e.message}")
            })
        compositeDisposable.add(disposable)
    }

    fun updatePupil(id: Int, pupil: Pupil) {
        val disposable = repository.updatePupil(id.toString(), pupil)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _updateSuccess.value = "Pupil updated successfully"
            }, { e ->
                _updateError.value = e.message
                Log.e("PupilListViewModel", "Error updating pupil: ${e.message}")
            })

        compositeDisposable.add(disposable)
    }

    fun deletePupil(pupil: Pupil) {
        val disposable = repository.deletePupil(pupil)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _deleteSuccess.value = "Pupil deleted successfully"
            }, { e ->
                _updateError.value = e.message
                Log.e("PupilListViewModel", "Error deleting pupil: ${e.message}")
            })

        compositeDisposable.add(disposable)
    }

    fun syncPendingPupils() {
        val disposable = repository.syncPupils()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _updateSuccess.value = "Pending pupils synced successfully"
            }, { e ->
                _updateError.value = e.message
                Log.e("PupilListViewModel", "Error syncing pending pupils: ${e.message}")
            })

        compositeDisposable.add(disposable)
    }


    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun refreshPupils() {
        loadPupils()
    }

    fun clearAllData() {
        val disposable = repository.clearAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _updateSuccess.value = "All data cleared successfully"
            }, { e ->
                _updateError.value = e.message
                Log.e("PupilListViewModel", "Error clearing all data: ${e.message}")
            })

        compositeDisposable.add(disposable)
    }

    fun observeSyncStatus(): LiveData<SyncStatus> {
        return repository.observeSyncStatus()
    }


}