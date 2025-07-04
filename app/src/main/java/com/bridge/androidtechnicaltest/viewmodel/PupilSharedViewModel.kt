package com.bridge.androidtechnicaltest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.db.Pupil
import com.bridge.androidtechnicaltest.db.PupilDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class PupilSharedViewModel(private val pupilDao: PupilDao) : ViewModel() {
    private val _selectedPupil = MutableLiveData<Pupil>()
    val selectedPupil: LiveData<Pupil> = _selectedPupil

    private val _unSyncedCount = MutableStateFlow(0)
    val unSyncedCount: StateFlow<Int> = _unSyncedCount.asStateFlow()

    init {
        observeUnsyncedPupils()
    }

    fun observeUnsyncedPupils() {
        pupilDao.observePendingPupils()
            .onEach {
                pending -> _unSyncedCount.value = pending.size
            }
            .catch { _unSyncedCount.value = 0 }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun selectPupil(pupil: Pupil) {
        _selectedPupil.value = pupil
    }

    fun updatePupil(id: Int, name: String, country: String, image: String) {
        _selectedPupil.value?.let {
            it.pupilId = id.toLong()
            it.name = name
            it.country = country
            it.image = image
        }
    }

    fun updateLatLng(latitude: Double, longitude: Double) {
        _selectedPupil.value?.let {
            it.latitude = latitude
            it.longitude = longitude
        }
    }

    fun updateCountry(country: String) {
        _selectedPupil.value?.let {
            it.country = country
        }
    }

    fun updateImage(image: String) {
        _selectedPupil.value?.let {
            it.image = image
            it.imageSynced = false
        }
    }
}
