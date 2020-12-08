package com.chirag.discussondrawing.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DrawingListModel(
    var id: String = "",
    var name: String = "",
    var additionTime: String = "",
    var markerCount: String = "",
    val imageURL: String = ""
) : Parcelable {
    override fun toString(): String {
        return "DrawingListModel(id='$id', name='$name', additionTime='$additionTime', markerCount='$markerCount', imageURL='$imageURL')"
    }
}
