package com.example.gitcommai.RepoClasses

import android.os.Parcel
import android.os.Parcelable

data class ReposItem(
    val created_at: String,
    val fork:Boolean,
    val description: String?,
    val disabled: Boolean,
    val downloads_url: String,
    val events_url: String,
    val full_name: String,
    val has_downloads: Boolean,
    val has_issues: Boolean,
    val has_pages: Boolean,
    val html_url: String,
    val id: Int,
    val is_template: Boolean,
    val merges_url: String,
    val name: String,
    val owner: Owner,
    val `private`: Boolean,
    val releases_url: String,
    val updated_at: String,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(Owner::class.java.classLoader) ?: Owner("", "", "", "", "", "", "", 0, "", "", "", "", "", false, "", "", "", "", ""),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(created_at)
        parcel.writeByte(if (fork) 1 else 0)
        parcel.writeString(description)
        parcel.writeByte(if (disabled) 1 else 0)
        parcel.writeString(downloads_url)
        parcel.writeString(events_url)
        parcel.writeString(full_name)
        parcel.writeByte(if (has_downloads) 1 else 0)
        parcel.writeByte(if (has_issues) 1 else 0)
        parcel.writeByte(if (has_pages) 1 else 0)
        parcel.writeString(html_url)
        parcel.writeInt(id)
        parcel.writeByte(if (is_template) 1 else 0)
        parcel.writeString(merges_url)
        parcel.writeString(name)
        parcel.writeParcelable(owner, flags)
        parcel.writeByte(if (`private`) 1 else 0)
        parcel.writeString(releases_url)
        parcel.writeString(updated_at)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ReposItem> {
        override fun createFromParcel(parcel: Parcel): ReposItem {
            return ReposItem(parcel)
        }

        override fun newArray(size: Int): Array<ReposItem?> {
            return arrayOfNulls(size)
        }
    }
}