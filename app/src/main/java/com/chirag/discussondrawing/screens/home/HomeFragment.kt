package com.chirag.discussondrawing.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chirag.discussondrawing.R
import com.chirag.discussondrawing.common.NodeNames
import com.chirag.discussondrawing.common.NodeNames.ADDITION_TIME
import com.chirag.discussondrawing.common.NodeNames.MARKERS
import com.chirag.discussondrawing.databinding.FragmentHomeBinding
import com.chirag.discussondrawing.models.DrawingListModel
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var mBinding: FragmentHomeBinding
    var databaseReferenceDrawings: DatabaseReference? = null
    var databaseReferenceMarkers: DatabaseReference? = null

    private var childEventListener: ChildEventListener? = null
    private var query: Query? = null
    private var userIds: ArrayList<String>? = null
    private var drawingListModelList: ArrayList<DrawingListModel>? = null

    private lateinit var drawingListAdapter: DrawingListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        mBinding.lifecycleOwner = this

        mBinding.btnAddDrawing.setOnClickListener { detailsScreenNavigation() }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawingListModelList = ArrayList()
        userIds = ArrayList()

        drawingListAdapter = DrawingListAdapter(requireContext(), drawingListModelList!!)
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        mBinding.rvDrawingsList.layoutManager = linearLayoutManager

        mBinding.rvDrawingsList.adapter = drawingListAdapter

        databaseReferenceDrawings = FirebaseDatabase.getInstance().reference.child("Drawings")
        databaseReferenceMarkers = FirebaseDatabase.getInstance().reference.child("Markers")
        query = databaseReferenceDrawings!!.orderByChild(NodeNames.ADDITION_TIME)

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                updateList(snapshot, true, snapshot.key)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                updateList(snapshot, false, snapshot.key)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        query!!.addChildEventListener(childEventListener as ChildEventListener)
    }

    private fun detailsScreenNavigation() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddDrawingFragment())
    }

    private fun updateList(dataSnapshot: DataSnapshot, isNew: Boolean, userId: String?) {

        val markerCount =
            if (dataSnapshot.child(MARKERS).value == null) "0" else dataSnapshot.child(
                MARKERS
            ).childrenCount.toString()


        val additionTime =
            if (dataSnapshot.child(ADDITION_TIME).value != null) dataSnapshot.child(ADDITION_TIME).value.toString() else ""

        mBinding.emptyListView.visibility = View.GONE
        databaseReferenceDrawings!!.child(userId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val drawingName =
                        if (snapshot.child(NodeNames.DRAWING_NAME).value != null) snapshot.child(
                            NodeNames.DRAWING_NAME
                        ).value.toString() else ""
                    val imageUrl =
                        if (snapshot.child(NodeNames.IMAGE_URL).value != null) snapshot.child(
                            NodeNames.IMAGE_URL
                        ).value.toString() else ""
                    val drawingListModel =
                        DrawingListModel(userId, drawingName, additionTime, markerCount, imageUrl)
                    if (isNew) {
                        drawingListModelList?.add(drawingListModel)
                        userIds?.add(userId)
                    } else {
                        val indexOfClickedUser = userIds!!.indexOf(userId)
                        drawingListModelList?.set(indexOfClickedUser, drawingListModel)
                    }
                    drawingListAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_fetch_list, error.message),
                        Toast.LENGTH_SHORT
                    ).show()
                    mBinding.emptyListView.visibility = View.VISIBLE
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        query!!.removeEventListener(childEventListener!!)
    }
}