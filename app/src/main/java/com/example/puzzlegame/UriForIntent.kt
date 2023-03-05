package com.example.puzzlegame

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable


class UriForIntent(val uri: Uri? = null) : Parcelable {
    constructor(parsel: Parcel) : this(
        parsel.readParcelable(Uri::class.java.classLoader)
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, 1)
    }

    companion object CREATOR : Parcelable.Creator<UriForIntent> {
        override fun createFromParcel(parcel: Parcel): UriForIntent {
            return UriForIntent(parcel)
        }

        override fun newArray(size: Int): Array<UriForIntent?> {
            return arrayOfNulls(size)
        }
    }
}

