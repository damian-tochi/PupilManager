package com.bridge.androidtechnicaltest.db

import android.util.Log
import androidx.lifecycle.LiveData
import com.bridge.androidtechnicaltest.di.ApiErrors
import com.bridge.androidtechnicaltest.network.PupilApi
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins.onError
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.URI
import kotlinx.coroutines.flow.flowOn
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import androidx.lifecycle.asFlow


interface IPupilRepository {
    fun getOrFetchPupils(): Single<PupilList>
    fun getPupilById(id: String): Single<Pupil>
    fun createPupil(pupil: Pupil): Single<Pupil>
    fun updatePupil(id: String, pupil: Pupil): Single<Pupil>
    fun deletePupil(pupil: Pupil): Completable
    fun syncPupils(): Completable
    fun clearAll(): Completable
    fun observeSyncStatus(): LiveData<SyncStatus>
}

class PupilRepository(private val database: AppDatabase, private val pupilApi: PupilApi) :
    IPupilRepository {

    private val _syncingPupil = MutableStateFlow(false)
    private val syncingPupil: StateFlow<Boolean> = _syncingPupil

    private val _syncError = MutableStateFlow(false)
    val syncError: StateFlow<Boolean> = _syncError

    override fun getOrFetchPupils(): Single<PupilList> {
        return database.pupilDao.getPupils()
            .flatMap { localList ->
                if (localList.isEmpty()) {
                    fetchFromRemote()
                } else {
                    val localLastUpdated = localList.maxOf { it.lastUpdated }

                    pupilApi.getPupils()
                        .flatMap { remoteList ->
                            val remoteLastUpdated =
                                remoteList.items.maxOfOrNull { it.lastUpdated } ?: 0L

                            if (localLastUpdated > remoteLastUpdated || remoteList.items.isEmpty()) {
                                syncLocalToRemote().andThen(Single.just(PupilList(localList.toMutableList())))
                            } else {
                                val updatedRemote = remoteList.items.map {
                                    it.copy(
                                        lastUpdated = System.currentTimeMillis(),
                                        pendingSync = false
                                    )
                                }
                                Completable.fromAction {
                                    database.pupilDao.insertAll(updatedRemote)
                                }.andThen(Single.just(PupilList(updatedRemote.toMutableList())))
                            }
                        }
                        .onErrorResumeNext { error ->
                            handleApiError(error, "PupilRepository: FetchPupil")
                            Single.just(PupilList(localList))
                        }
                }
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getPupilById(id: String): Single<Pupil> {
        return pupilApi.getPupilById(id)
            .doOnSuccess { pupil ->
                Completable.fromAction {
                    database.pupilDao.insertPupil(pupil)
                }.subscribeOn(Schedulers.io()).subscribe()
            }
            .doOnError { error ->
                handleApiError(error, "PupilRepository: GetPupilByID")
            }
            .subscribeOn(Schedulers.io())
    }

    override fun createPupil(pupil: Pupil): Single<Pupil> {
        val localPupil = pupil.copy(
            lastUpdated = System.currentTimeMillis(),
            pupilId = System.currentTimeMillis(),
            pendingSync = true,
            isNew = true,
            imageSynced = false
        )

        return Completable.fromAction {
            database.pupilDao.insertPupil(localPupil)
        }.andThen(
            if (!localPupil.imageSynced) {
                val file = File(URI(localPupil.image))
                uploadImageToCloudinary(file)
                    .flatMap { cloudUrl ->
                        val updatedWithImage = localPupil.copy(
                            image = cloudUrl,
                            imageSynced = true,
                            lastUpdated = System.currentTimeMillis()
                        )
                        Completable.fromAction {
                            database.pupilDao.updatePupil(updatedWithImage)
                        }.andThen(
                            pupilApi.createPupil(updatedWithImage.copy(pupilId = 0))
                                .flatMap { created ->
                                    val synced = created.copy(
                                        pendingSync = false,
                                        isNew = false,
                                        imageSynced = true,
                                        lastUpdated = System.currentTimeMillis()
                                    )
                                    Completable.fromAction {
                                        database.pupilDao.deletePupil(localPupil)
                                        database.pupilDao.insertPupil(synced)
                                    }.andThen(Single.just(synced))
                                }
                                .onErrorResumeNext { error ->
                                    handleApiError(error, "PupilRepository: CreatePupil")
                                    Single.just(updatedWithImage)
                                }
                        )
                    }
                    .onErrorResumeNext { error ->
                        handleApiError(error, "ImageUpload: CreatePupil")
                        Single.just(localPupil)
                    }
            } else {
                Single.just(localPupil)
            }
        ).subscribeOn(Schedulers.io())
    }

    override fun updatePupil(id: String, pupil: Pupil): Single<Pupil> {
        val localPupil = pupil.copy(
            lastUpdated = System.currentTimeMillis(),
            pendingSync = true
        )

        return Completable.fromAction {
            database.pupilDao.updatePupil(localPupil)
        }.andThen(
            if (!localPupil.imageSynced) {
                val file = File(URI(localPupil.image))
                uploadImageToCloudinary(file)
                    .flatMap { cloudUrl ->
                        val updatedWithImage = localPupil.copy(
                            image = cloudUrl,
                            imageSynced = true,
                            lastUpdated = System.currentTimeMillis()
                        )
                        Completable.fromAction {
                            database.pupilDao.updatePupil(updatedWithImage)
                        }.andThen(
                            pupilApi.updatePupil(id, updatedWithImage)
                                .flatMap { updatedPupil ->
                                    val synced = updatedPupil.copy(
                                        pendingSync = false,
                                        imageSynced = true,
                                        lastUpdated = System.currentTimeMillis()
                                    )
                                    Completable.fromAction {
                                        database.pupilDao.updatePupil(synced)
                                    }.andThen(Single.just(synced))
                                }
                                .onErrorResumeNext { error ->
                                    handleApiError(error, "PupilRepository: UpdatePupil")
                                    Single.just(updatedWithImage)
                                }
                        )
                    }
                    .onErrorResumeNext { error ->
                        handleApiError(error, "ImageUpload: UpdatePupil")
                        Single.just(localPupil)
                    }
            } else {
                pupilApi.updatePupil(id, localPupil)
                    .flatMap { updatedPupil ->
                        val synced = updatedPupil.copy(
                            pendingSync = false,
                            lastUpdated = System.currentTimeMillis()
                        )
                        Completable.fromAction {
                            database.pupilDao.updatePupil(synced)
                        }.andThen(Single.just(synced))
                    }
                    .onErrorResumeNext { error ->
                        handleApiError(error, "PupilRepository: UpdatePupil")
                        Single.just(localPupil)
                    }
            }
        ).subscribeOn(Schedulers.io())
    }

    override fun deletePupil(pupil: Pupil): Completable {
        return pupilApi.deletePupil(pupil.pupilId.toString())
            .onErrorResumeNext { throwable ->
                if (throwable is HttpException && throwable.code() == 404) {
                    Completable.fromAction {
                        database.pupilDao.deletePupil(pupil)
                    }
                } else {
                    handleApiError(throwable, "PupilRepository: DeletePupil")
                    Completable.error(throwable)
                }
            }
            .andThen(
                Completable.fromAction {
                    database.pupilDao.deletePupil(pupil)
                }
            )
            .doOnError { error ->
                handleApiError(error, "PupilRepository: DeletePupil")
            }
            .subscribeOn(Schedulers.io())
    }

    override fun syncPupils(): Completable {
        _syncingPupil.value = true
        _syncError.value = false
        return database.pupilDao.getPendingPupils()
            .flattenAsFlowable { it }
            .flatMapCompletable { unSyncedPupil ->

                val imageUploadFlow = if (!unSyncedPupil.imageSynced) {
                    val file = File(URI(unSyncedPupil.image))
                    uploadImageToCloudinary(file)
                        .flatMapCompletable { cloudUrl ->
                            val updated = unSyncedPupil.copy(
                                image = cloudUrl,
                                imageSynced = true,
                                lastUpdated = System.currentTimeMillis()
                            )

                            Completable.fromAction {
                                database.pupilDao.updatePupil(updated)
                            }
                        }
                        .doOnError {
                            _syncingPupil.value = false
                            _syncError.value = true
                        }
                } else {
                    Completable.complete()
                }

                imageUploadFlow.andThen(
                    Completable.defer {
                        val latest =
                            database.pupilDao.getPupilById(unSyncedPupil.pupilId).blockingGet()

                        val apiCall = if (latest.isNew) {
                            pupilApi.createPupil(latest.copy(pupilId = 0))
                                .flatMapCompletable { created ->
                                    _syncingPupil.value = false
                                    Completable.fromAction {
                                        database.pupilDao.deletePupil(latest)
                                        database.pupilDao.insertPupil(
                                            created.copy(
                                                pendingSync = false,
                                                isNew = false,
                                                imageSynced = true,
                                                lastUpdated = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                        } else {
                            pupilApi.updatePupil(latest.pupilId.toString(), latest)
                                .flatMapCompletable { updated ->
                                    _syncingPupil.value = false
                                    Completable.fromAction {
                                        database.pupilDao.updatePupil(
                                            updated.copy(
                                                pendingSync = false,
                                                imageSynced = true,
                                                lastUpdated = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                        }

                        apiCall.onErrorResumeNext { error ->
                            handleApiError(error, "PupilRepository: syncPupils")
                            _syncError.value = true
                            _syncingPupil.value = false
                            Completable.fromAction {
                                database.pupilDao.updatePupil(
                                    latest.copy(pendingSync = true)
                                )
                            }
                        }
                    }
                )
            }
            .subscribeOn(Schedulers.io())
    }


    override fun clearAll(): Completable {
        return Completable.fromAction {
            database.pupilDao.clearAll()
        }
            .subscribeOn(Schedulers.io())
            .doOnError { error ->
                Log.e("PupilRepository", "Error clearing all pupils: ${error.localizedMessage}")
            }
    }

    override fun observeSyncStatus(): LiveData<SyncStatus> {
        return combine<List<Pupil>, Boolean, Boolean, SyncStatus>(
            database.pupilDao.observePendingPupils(),
            syncingPupil,
            syncError
        ) { pendingList, syncing, error ->
            if (pendingList.isEmpty()) {
                SyncStatus.SUCCESS
            } else {
                if (error) {
                    SyncStatus.ERROR
                }else if (syncing) {
                    SyncStatus.SYNCING
                } else {
                    SyncStatus.PENDING
                }
            }
        }
            .catch { emit(SyncStatus.ERROR) }
            .flowOn(Dispatchers.IO)
            .asLiveData()
    }

    private fun fetchFromRemote(): Single<PupilList> {
        return pupilApi.getPupils()
            .map { pupilList ->
                val sanitized = pupilList.items.map { it.sanitized() }
                PupilList(sanitized.toMutableList())
            }
            .flatMap { cleanList ->
                val updatedRemote = cleanList.items.map {
                    it.copy(
                        lastUpdated = System.currentTimeMillis(),
                        pendingSync = false,
                        isNew = false
                    )
                }
                Completable.fromAction {
                    database.pupilDao.insertAll(updatedRemote)
                }.andThen(Single.just(PupilList(updatedRemote.toMutableList())))
            }
            .doOnError { error ->
                handleApiError(error, "PupilRepository: fetchFromRemote")
            }
            .subscribeOn(Schedulers.io())
    }

    private fun syncLocalToRemote(): Completable {
        return database.pupilDao.getPupils()
            .flattenAsFlowable { it }
            .filter { it.pendingSync && it.isNew }
            .flatMapCompletable { unsynced ->
                val request = unsynced.copy(pupilId = 0)

                pupilApi.createPupil(request)
                    .flatMapCompletable { created ->
                        Completable.fromAction {
                            database.pupilDao.insertPupil(
                                created.copy(pendingSync = false, isNew = false)
                            )
                        }
                    }
            }
    }

    private fun uploadImageToCloudinary(imageFile: File): Single<String> {
        val mediaType = "image/*".toMediaTypeOrNull()
        val fileBody = imageFile.asRequestBody(mediaType)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", imageFile.name, fileBody)
            .addFormDataPart("upload_preset", "unsigned_newglobe_test")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/dythx2jzw/image/upload")
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        return Single.create<String> { emitter ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    emitter.onError(e)
                    Log.e(
                        "PupilRepository: Cloudinary Error",
                        "Upload failed: ${e.localizedMessage}"
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val url = responseBody?.let { JSONObject(it).getString("secure_url") }
                        if (url != null) {
                            emitter.onSuccess(url)
                        }
                    } else {
                        onError(IOException("Upload failed: ${response.message}"))
                    }
                }
            })
        }.subscribeOn(Schedulers.io())
    }
}

fun Pupil.sanitized(): Pupil {
    return this.copy(
        name = name ?: "unspecified",
        country = country ?: "unspecified",
        image = image ?: "unspecified",
        lastUpdated = System.currentTimeMillis()
    )
}

private fun handleApiError(error: Throwable, tag: String) {
    if (error is HttpException) {
        val errorBody = error.response()?.errorBody()?.string()
        errorBody?.let {
            try {
                val parsed = Gson().fromJson(it, ApiErrors::class.java)
                val errorMsg = parsed.errors?.entries?.joinToString("\n") { (field, messages) ->
                    "$field: ${messages.joinToString()}"
                } ?: parsed.title ?: "$errorBody"

                Log.e(tag, "API Error: $errorMsg")
            } catch (e: Exception) {
                Log.e(tag, "Failed to parse error: ${e.localizedMessage} | $errorBody")
            }
        }
    } else {
        Log.e(tag, "Unexpected error: ${error.localizedMessage}")
    }
}

