package com.bridge.androidtechnicaltest.network

import com.bridge.androidtechnicaltest.db.Pupil
import com.bridge.androidtechnicaltest.db.PupilList
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PupilApi {
    @GET("pupils")
    fun getPupils(@Query("page") page: Int = 1): Single<PupilList>

    @GET("pupils/{id}")
    fun getPupilById(@Path("id") id: String): Single<Pupil>

    @POST("pupils")
    fun createPupil(@Body body: Pupil): Single<Pupil>

    @PUT("pupils/{id}")
    fun updatePupil(@Path("id") id: String, @Body body: Pupil): Single<Pupil>

    @DELETE("pupils/{id}")
    fun deletePupil(@Path("id") id: String): Completable

}