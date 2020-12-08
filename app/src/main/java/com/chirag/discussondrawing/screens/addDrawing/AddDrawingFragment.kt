package com.chirag.discussondrawing.screens.addDrawing

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chirag.discussondrawing.MainActivity
import com.chirag.discussondrawing.R
import com.chirag.discussondrawing.common.NodeNames
import com.chirag.discussondrawing.databinding.FragmentAddDrawingBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.*


class AddDrawingFragment : Fragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentAddDrawingBinding
    private var databaseReference: DatabaseReference? = null
    private var filePathUri: Uri? = null
    private var fileBytes: ByteArrayOutputStream? = null
    private var storageReference: StorageReference? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private val REQUEST_CODE_PICK_IMAGE = 101
    private val REQUEST_CODE_CAPTURE_IMAGE = 102

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_drawing,
            container,
            false
        )

        storageReference = FirebaseStorage.getInstance().getReference("Drawings")
        databaseReference = FirebaseDatabase.getInstance().getReference("Drawings")

        bottomSheetDialog = BottomSheetDialog(requireContext())
        @SuppressLint("InflateParams") val view: View =
            layoutInflater.inflate(R.layout.file_options, null)
        view.findViewById<View>(R.id.llCameraOption).setOnClickListener(this)
        view.findViewById<View>(R.id.llGalleryOption).setOnClickListener(this)
        view.findViewById<View>(R.id.ivClose).setOnClickListener(this)
        bottomSheetDialog.setContentView(view)

        mBinding.ivDrawImage.setOnClickListener(this)

        mBinding.btnAdd.setOnClickListener {
            if (mBinding.etDrawingName.text.isNullOrEmpty() && mBinding.ivDrawImage.tag == getString(
                    R.string.add
                )
            )
                Toast.makeText(context, "Please add Name and Drawing image", Toast.LENGTH_LONG)
                    .show()
            else {
                if (fileBytes != null)
                    uploadByte(fileBytes!!)
                else
                    uploadFile(filePathUri)
            }
        }

        mBinding.btnClear.setOnClickListener {
            clearAll()
        }
        return mBinding.root
    }

    private fun clearAll() {
        mBinding.ivDrawImage.setImageResource(R.drawable.ic_vector_add_photo)
        mBinding.ivDrawImage.tag = getString(R.string.add)
        mBinding.etDrawingName.text?.clear()
        filePathUri = null
        fileBytes = null

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivDrawImage -> {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bottomSheetDialog.show()
                } else {
                    ActivityCompat.requestPermissions(
                        activity as MainActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1
                    )
                }
                val inputMethodManager =
                    activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
            R.id.llCameraOption -> {
                bottomSheetDialog.dismiss()
                val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intentCamera, REQUEST_CODE_CAPTURE_IMAGE)
            }
            R.id.llGalleryOption -> {
                bottomSheetDialog.dismiss()
                val intentGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentGallery, REQUEST_CODE_PICK_IMAGE)

            }
            R.id.ivClose -> bottomSheetDialog.dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bottomSheetDialog.show()
            } else {
                Toast.makeText(
                    context,
                    R.string.permission_file_access,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CAPTURE_IMAGE -> {     //Camera
                    val bitmap: Bitmap?
                    if (data != null) {
                        bitmap = data.extras!!["data"] as Bitmap?
                        val bytes = ByteArrayOutputStream()
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                        mBinding.ivDrawImage.setImageBitmap(bitmap)
                        fileBytes = bytes
                        mBinding.ivDrawImage.tag = getString(R.string.clear)
                    }
                }
                REQUEST_CODE_PICK_IMAGE -> {      //Gallery
                    var uri: Uri? = null
                    if (data != null) {
                        uri = data.data
                    }
                    mBinding.ivDrawImage.setImageURI(uri)
                    filePathUri = uri
                    mBinding.ivDrawImage.tag = getString(R.string.clear)
                }
            }
        }
    }

    private fun uploadFile(uri: Uri?) {
        val pushId = databaseReference?.key
        storageReference = FirebaseStorage.getInstance().reference
        val fileName = "${System.currentTimeMillis()}.jpg"
        val fileRef = storageReference?.child("Drawings")?.child(fileName)
        val uploadTask = fileRef?.putFile(uri!!)
        uploadProgress(uploadTask!!, fileRef, pushId)
    }

    private fun uploadByte(bytes: ByteArrayOutputStream) {
        val pushId = databaseReference?.key
        val storageReference = FirebaseStorage.getInstance().reference
        val fileName = "${System.currentTimeMillis()}.jpg"
        val fileRef = storageReference.child("Drawings").child(fileName)
        val uploadTask = fileRef.putBytes(bytes.toByteArray())
        uploadProgress(uploadTask, fileRef, pushId)
    }

    private fun uploadProgress(
        task: UploadTask,
        filePath: StorageReference,
        pushId: String?
    ) {
        @SuppressLint("InflateParams") val view: View =
            layoutInflater.inflate(R.layout.file_progress, null)
        val pbProgress = view.findViewById<ProgressBar>(R.id.pbProgress)
        val tvFileProgress = view.findViewById<TextView>(R.id.tvFileProgress)
        val ivPause = view.findViewById<ImageView>(R.id.ivPause)
        val ivPlay = view.findViewById<ImageView>(R.id.ivPlay)
        val ivCancel = view.findViewById<ImageView>(R.id.ivCancel)
        ivPause.setOnClickListener {
            task.pause()
            ivPlay.visibility = View.VISIBLE
            ivPause.visibility = View.GONE
        }
        ivPlay.setOnClickListener {
            task.resume()
            ivPlay.visibility = View.GONE
            ivPause.visibility = View.VISIBLE
        }
        ivCancel.setOnClickListener { task.cancel() }
        mBinding.llProgress.addView(view)
        tvFileProgress.text =
            getString(R.string.upload_progress, "Image", "0")
        task.addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
            val progress =
                100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            pbProgress.progress = progress.toInt()
            tvFileProgress.text = getString(
                R.string.upload_progress,
                "Image",
                pbProgress.progress.toString()
            )
        }
        task.addOnCompleteListener { task1: Task<UploadTask.TaskSnapshot?> ->
            mBinding.llProgress.removeView(view)
            if (task1.isSuccessful) {
                filePath.downloadUrl
                    .addOnSuccessListener { uri: Uri ->
                        val downloadUrl = uri.toString()
                        updateInfo(downloadUrl, pushId!!)
                    }
            }
        }
        task.addOnFailureListener { e: java.lang.Exception ->
            mBinding.llProgress.removeView(view)
            Toast.makeText(
                context,
                getString(R.string.failed_to_upload, e.message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateInfo(downloadUrl: String, pushId: String) {
        try {
            if (downloadUrl != "") {
                val messageMap = HashMap<String, Any>()

                messageMap[NodeNames.DRAWING_NAME] = mBinding.etDrawingName.text.toString().trim()
                messageMap[NodeNames.ADDITION_TIME] = ServerValue.TIMESTAMP
                messageMap[NodeNames.MARKER_COUNT] = "0"
                messageMap[NodeNames.IMAGE_URL] = downloadUrl
                Toast.makeText(
                    activity?.applicationContext,
                    "Image Uploaded Successfully ",
                    Toast.LENGTH_LONG
                ).show()
                homeScreenNavigation()

                val imageUploadId: String = databaseReference?.push()?.key.toString()
                databaseReference?.child(imageUploadId)?.updateChildren(
                    messageMap
                ) { error, _ ->
                    if (error != null) {
                        Toast.makeText(
                            context,
                            getString(R.string.failed_to_send_message, error.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                getString(R.string.failed_to_send_message, e.message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun homeScreenNavigation() {
        findNavController().navigate(AddDrawingFragmentDirections.actionAddDrawingFragmentToHomeFragment())
    }
}