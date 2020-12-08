package com.chirag.discussondrawing.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Drawing(
    var name: String = "",
    var additionTime: Long = 0,
    var markerCount: String = "",
    val imageURL: String = ""
) : Parcelable {
    override fun toString(): String {
        return "Drawing(name='$name', additionTime='$additionTime', markerCount='$markerCount', imageURL=$imageURL)"
    }
}
