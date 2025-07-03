package com.bridge.androidtechnicaltest.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Pupils")
data class Pupil(
        @PrimaryKey
        @ColumnInfo(name = "pupil_id")
        var pupilId: Long,

        @ColumnInfo(name = "name")
        var name: String,

        @ColumnInfo(name = "country")
        var country: String,

        @ColumnInfo(name = "image")
        var image: String,

        @ColumnInfo(name = "latitude")
        var latitude: Double,

        @ColumnInfo(name = "longitude")
        var longitude: Double,

        @ColumnInfo(name = "lastUpdated")
        var lastUpdated: Long = System.currentTimeMillis(),

        @ColumnInfo(name = "pending_sync")
        var pendingSync: Boolean = true,

        @ColumnInfo(name = "is_new")
        var isNew: Boolean = true,

        val imageSynced: Boolean = false
){
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Pupil) return false

                return pupilId == other.pupilId &&
                        name == other.name &&
                        country == other.country &&
                        image == other.image &&
                        latitude == other.latitude &&
                        longitude == other.longitude
        }

        override fun hashCode(): Int {
                var result = pupilId.hashCode()
                result = 31 * result + name.hashCode()
                result = 31 * result + country.hashCode()
                result = 31 * result + image.hashCode()
                result = 31 * result + latitude.hashCode()
                result = 31 * result + longitude.hashCode()
                return result
        }
}

class PupilList(
        val items: MutableList<Pupil>
)