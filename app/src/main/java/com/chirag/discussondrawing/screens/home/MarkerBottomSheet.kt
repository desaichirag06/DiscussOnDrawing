package com.chirag.discussondrawing.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chirag.discussondrawing.R
import com.chirag.discussondrawing.common.Util.Companion.getTimeAgo
import com.chirag.discussondrawing.models.MarkerDetails
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class MarkerBottomSheet(markerDetails: MarkerDetails) : BottomSheetDialogFragment() {

    private var markerDetail: MarkerDetails = markerDetails

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        val view1: View = inflater.inflate(R.layout.marker_bottom_sheet, container, false)

        val tvMarkerName = view1.findViewById<TextView>(R.id.tvMarkerName)
        val tvMarkerDesc = view1.findViewById<TextView>(R.id.tvMarkerDesc)
        val tvMarkerByAndTime = view1.findViewById<TextView>(R.id.tvMarkerByAndTime)
        tvMarkerName.text = markerDetail.name
        tvMarkerDesc.text = markerDetail.discussion

        tvMarkerByAndTime.text =
            "${markerDetail.addedBy} - ${getTimeAgo(markerDetail.timeStamp.toLong())}"

        return view1
    }
}