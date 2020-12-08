package com.chirag.discussondrawing.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MarkerDetails(
    var name: String = "",
    var addedBy: String = "",
    var discussion: String = "",
    val pivotX: Float = 0f,
    val pivotY: Float = 0f,
    val timeStamp: String = ""
) : Parcelable {
    override fun toString(): String {
        return "MarkerDetails(name='$name', addedBy='$addedBy', discussion='$discussion', pivotX=$pivotX, pivotY=$pivotY)"
    }
}
