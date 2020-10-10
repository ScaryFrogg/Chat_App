package me.vojinpuric.chatapp.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import me.vojinpuric.chatapp.MainActivity
import me.vojinpuric.chatapp.R
import me.vojinpuric.chatapp.helpers.RC_IMAGE_PICKER
import java.util.*

class ProfileFragment : Fragment() {
    private var profileImageUri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }


    override fun onResume() {
        super.onResume()
        refreshScreen()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profile_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,
                RC_IMAGE_PICKER
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //uploading image
        if (requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            profileImageUri = data.data
            uploadImageToFirebaseStorage()
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= 29) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        requireContext().contentResolver,
                        profileImageUri!!
                    )
                )
            } else {
                // older version
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, profileImageUri)
            }
        }
    }

    private fun uploadImageToFirebaseStorage() {
        val filename = UUID.randomUUID().toString()
        profileImageUri?.let {
            FirebaseStorage.getInstance().getReference("/images/$filename").apply {
                putFile(it).addOnSuccessListener {
                    this.downloadUrl.addOnSuccessListener { downloadUri ->
                        Log.e("file uploaded", "location: $downloadUri")
                        profileImageUri=downloadUri
                        saveUserToRealtimeDatabase()
                    }
                }
            }
        }
    }
    private fun refreshScreen(){
        MainActivity.currentUser?.let {
            profile_email.text=it.email
            tb.text=it.contacts.toString()
            Picasso.get().load(it.profileImageUri).into(profile_image)
        }
    }

    private fun saveUserToRealtimeDatabase() {
        val authRef = MainActivity.mFirebaseAuth
        val uid = authRef.uid
        val ref = MainActivity.mFirebaseDatabase.getReference("/users/$uid")
        MainActivity.currentUser!!.profileImageUri = profileImageUri.toString()
        ref.setValue(MainActivity.currentUser).addOnSuccessListener { refreshScreen() }
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}
