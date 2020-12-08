package com.chirag.discussondrawing.screens.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.chirag.discussondrawing.R
import com.chirag.discussondrawing.common.Util.Companion.getTimeAgo
import com.chirag.discussondrawing.databinding.DrawingItemViewBinding
import com.chirag.discussondrawing.models.DrawingListModel

class DrawingListAdapter(
    var context: Context,
    private val drawListModelList: List<DrawingListModel>
) : RecyclerView.Adapter<DrawingListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinder: DrawingItemViewBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.drawing_item_view, parent, false
        )
        return ViewHolder(mBinder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drawingListModel = drawListModelList[position]
        val binding = holder.binding

        holder.binding.drawing = drawingListModel
        holder.binding.executePendingBindings()
        binding.tvTime.text = getTimeAgo(drawingListModel.additionTime.toLong())

        holder.itemView.setOnClickListener { view ->
            view.findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToDrawingDetailsFragment(drawingListModel)
            )
        }
    }

    override fun getItemCount(): Int {
        return drawListModelList.size
    }

    class ViewHolder internal constructor(var binding: DrawingItemViewBinding) :
        RecyclerView.ViewHolder(
            binding.root
        )
}