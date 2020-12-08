package com.chirag.discussondrawing.screens.home

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chirag.discussondrawing.R
import com.chirag.discussondrawing.common.NodeNames
import com.chirag.discussondrawing.common.NodeNames.MARKER_ADDED_BY
import com.chirag.discussondrawing.common.NodeNames.MARKER_DISCUSSION
import com.chirag.discussondrawing.common.NodeNames.MARKER_ID
import com.chirag.discussondrawing.common.NodeNames.MARKER_PIVOT_X
import com.chirag.discussondrawing.common.NodeNames.MARKER_PIVOT_Y
import com.chirag.discussondrawing.common.NodeNames.MARKER_TIMESTAMP
import com.chirag.discussondrawing.common.NodeNames.MARKER_TITLE
import com.chirag.discussondrawing.common.Util
import com.chirag.discussondrawing.databinding.FragmentDrawingDetailsBinding
import com.chirag.discussondrawing.models.DrawingListModel
import com.chirag.discussondrawing.models.MarkerDetails
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set


class DrawingDetailsFragment : Fragment() {

    private lateinit var mBinding: FragmentDrawingDetailsBinding
    private var addMarkerDialog: Dialog? = null
    private var databaseReference: DatabaseReference? = null
    private lateinit var drawingDetails: DrawingListModel
    private lateinit var markerDetailsList: ArrayList<MarkerDetails>

    private var childEventListener: ChildEventListener? = null
    private var query: Query? = null
    private var userIds: ArrayList<String>? = null

    private lateinit var markersListAdapter: MarkersListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_drawing_details, container, false)

        val args = arguments?.let { DrawingDetailsFragmentArgs.fromBundle(it) }
        drawingDetails = args!!.drawingDetails

        mBinding.drawingDetails = drawingDetails
        mBinding.executePendingBindings()

        markerDetailsList = ArrayList()
        userIds = ArrayList()

        markersListAdapter = MarkersListAdapter(requireContext(), markerDetailsList!!)
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        mBinding.rvMarkerList.layoutManager = linearLayoutManager

        mBinding.rvMarkerList.adapter = markersListAdapter

        databaseReference =
            FirebaseDatabase.getInstance().getReference("Drawings").child(drawingDetails.id)
                .child("Markers")

        setGestureEvents(drawingDetails)

        query = databaseReference!!.orderByChild(NodeNames.ADDITION_TIME)

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                updateList(true, snapshot.key)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                updateList(false, snapshot.key)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        query!!.addChildEventListener(childEventListener as ChildEventListener)

        return mBinding.root
    }

    private fun updateList(isNew: Boolean, userId: String?) {

        databaseReference!!.child(userId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val markerDetailsModel =
                        MarkerDetails(
                            snapshot.child(MARKER_TITLE).value.toString(),
                            snapshot.child(MARKER_ADDED_BY).value.toString(),
                            snapshot.child(MARKER_DISCUSSION).value.toString(),
                            snapshot.child(MARKER_PIVOT_X).value.toString().toFloat(),
                            snapshot.child(MARKER_PIVOT_Y).value.toString().toFloat(),
                            snapshot.child(MARKER_TIMESTAMP).value.toString()
                        )
                    if (isNew) {
                        markerDetailsList.add(markerDetailsModel)
                        userIds?.add(userId)
                    } else {
                        val indexOfClickedUser = userIds!!.indexOf(userId)
                        markerDetailsList[indexOfClickedUser] = markerDetailsModel
                    }
                    setPins()
                    markersListAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_fetch_list, error.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        query!!.removeEventListener(childEventListener!!)
    }

    private fun setGestureEvents(drawingDetails: DrawingListModel) {
        val gestureDetector =
            GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    mBinding.ivDrawImage.viewToSourceCoord(e.x, e.y)?.let {
                        for (item in markerDetailsList) {
                            if (it.x.toInt() == item.pivotX.toInt() && it.y.toInt() == item.pivotY.toInt()) {
                                MarkerBottomSheet(item).show(
                                    (context as FragmentActivity).supportFragmentManager,
                                    "showMarkerSheet"
                                )
                            }
                        }
                    } ?: run {
                        Toast.makeText(
                            context, "Single tap: Image not ready", Toast.LENGTH_LONG
                        ).show()
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {

                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    mBinding.ivDrawImage.viewToSourceCoord(e.x, e.y)?.let {
                        addNewMarker(it.x.toInt(), it.y.toInt())
                    } ?: run {
                        Toast.makeText(
                            context, "Double tap: Image not ready",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return true
                }
            })

        Glide.with(requireActivity().applicationContext)
            .asBitmap()
            .load(drawingDetails.imageURL)
            .into(object : CustomTarget<Bitmap?>() {

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    mBinding.ivDrawImage.setImage(ImageSource.bitmap(resource))
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
        mBinding.ivDrawImage.setOnTouchListener { _, motionEvent ->
            gestureDetector.onTouchEvent(
                motionEvent
            )
        }

    }

    private fun addNewMarker(pivotX: Int, pivotY: Int) {
        val mDialog = AlertDialog.Builder(activity)
        val view: View = requireActivity().layoutInflater.inflate(R.layout.dialog_add_marker, null)
        mDialog.setView(view)
        mDialog.setCancelable(true)

        val addButton = view.findViewById<Button>(R.id.btnAdd)
        val clearButton = view.findViewById<Button>(R.id.btnClear)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etAddedBy = view.findViewById<EditText>(R.id.etAddedBy)
        val etDiscussion = view.findViewById<EditText>(R.id.etDiscussion)
        addMarkerDialog = mDialog.create()
        addMarkerDialog?.show()
        addButton.setOnClickListener {
            if (etTitle.text.isNotEmpty() && etAddedBy.text.isNotEmpty() && etDiscussion.text.isNotEmpty()) {

                if (Util.connectionAvailable(requireContext())) {
                    val userMessagePush =
                        databaseReference!!.child("Markers").push()
                    val pushId = userMessagePush.key
                    if (pushId != null) {
                        sendMessage(
                            etTitle.text.toString().trim(),
                            etAddedBy.text.toString().trim(),
                            etDiscussion.text.toString().trim(),
                            pivotX,
                            pivotY,
                            pushId
                        )
                    }
                } else {
                    Toast.makeText(
                        context,
                        "No Internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                addMarkerDialog?.dismiss()
            } else
                Toast.makeText(context, "Please fill all the data", Toast.LENGTH_LONG).show()
        }
        clearButton.setOnClickListener {
            etTitle.text.clear()
            etAddedBy.text.clear()
            etDiscussion.text.clear()
            addMarkerDialog?.dismiss()
        }
    }

    private fun sendMessage(
        title: String,
        addedBy: String,
        discussion: String,
        pivotX: Int,
        pivotY: Int,
        pushId: String
    ) {
        val messageMap = HashMap<String, Any>()
        messageMap[MARKER_ID] = pushId
        messageMap[MARKER_TITLE] = title
        messageMap[MARKER_ADDED_BY] = addedBy
        messageMap[MARKER_DISCUSSION] = discussion
        messageMap[MARKER_PIVOT_X] = pivotX
        messageMap[MARKER_PIVOT_Y] = pivotY
        messageMap[MARKER_TIMESTAMP] = ServerValue.TIMESTAMP
        val messageUserMap = HashMap<String, Any>()
        messageUserMap[pushId] = messageMap
        databaseReference!!.updateChildren(messageUserMap) { error: DatabaseError?, _: DatabaseReference? ->
            if (error != null) {
                Toast.makeText(
                    context,
                    getString(R.string.failed_to_send_message, error.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
            run {

                //Toast.makeText(ChatActivity.this, R.string.message_sent_successfully, Toast.LENGTH_SHORT).show();
                /* var title = ""
                 when (msgType) {
                     MESSAGE_TYPE_TEXT -> title = "New Message"
                     MESSAGE_TYPE_IMAGE -> title = "New Image"
                     MESSAGE_TYPE_VIDEO -> title = "New Video"
                 }
                 val lastMessage = if (title != "New Message") title else msg
                 updateChatDetails(this@ChatActivity, currentUserId, lastMessage)*/
            }
        }
    }

    private fun setPins() {
        for (i in markerDetailsList) {
            val center = PointF(i.pivotX, i.pivotY)
            mBinding.ivDrawImage.setPin(center)
        }
    }

}