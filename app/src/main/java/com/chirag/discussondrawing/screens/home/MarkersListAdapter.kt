package com.chirag.discussondrawing.screens.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.chirag.discussondrawing.R
import com.chirag.discussondrawing.common.Util.Companion.getTimeAgo
import com.chirag.discussondrawing.databinding.MarkerItemViewBinding
import com.chirag.discussondrawing.models.MarkerDetails

class MarkersListAdapter(
    var context: Context,
    private val drawListModelList: List<MarkerDetails>
) : RecyclerView.Adapter<MarkersListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinder: MarkerItemViewBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.marker_item_view, parent, false
        )
        return ViewHolder(mBinder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val markerDetails = drawListModelList[position]
        val binding = holder.binding

        holder.binding.marker = markerDetails
        holder.binding.executePendingBindings()
        if (markerDetails.timeStamp.isNotEmpty())
            binding.tvTime.text = getTimeAgo(markerDetails.timeStamp.toLong())

        holder.itemView.setOnClickListener {
            MarkerBottomSheet(markerDetails).show(
                (context as FragmentActivity).supportFragmentManager,
                "showMarkerSheet"
            )
        }
    }

    override fun getItemCount(): Int {
        return drawListModelList.size
    }


    class ViewHolder(var binding: MarkerItemViewBinding) :
        RecyclerView.ViewHolder(
            binding.root
        )
}