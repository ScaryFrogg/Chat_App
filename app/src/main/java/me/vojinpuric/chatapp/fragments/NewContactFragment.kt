package me.vojinpuric.chatapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_new_contact.*
import me.vojinpuric.chatapp.R
import me.vojinpuric.chatapp.helpers.PROFILE_IMAGE_PLACEHOLDER
import me.vojinpuric.chatapp.model.User


class NewContactFragment : Fragment() {
    private val mDatabase by lazy{ FirebaseDatabase.getInstance()}
    private val mAuth by lazy{FirebaseAuth.getInstance()}
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myUid = mAuth.currentUser!!.uid
        btnSearch.setOnClickListener {
            if (etSearchByEmail.text.isNotBlank() and (etSearchByEmail.text.toString()!= mAuth.currentUser!!.email!!)){
                val query =mDatabase.reference.child("users").orderByChild("email").equalTo(etSearchByEmail.text.toString())
                    .addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if( p0.hasChildren()){
                                for (snapshot in p0.children) {
                                    val user = snapshot.getValue(User::class.java)
                                    Log.e("search", user!!.email)
                                    Toast.makeText(context,"Contact request sent",Toast.LENGTH_LONG).show()
                                    mDatabase.getReference("/friend-request/${user.uid}/").push()
                                        .setValue(User(myUid, PROFILE_IMAGE_PLACEHOLDER,mAuth.currentUser!!.email!!,
                                            HashMap()))
                                }
                            }else{
                                //TODO different message for your own email
                                Log.e("search","user does not exists")
                            }

                        }
                    })

            }
        }
    }
    companion object {
        fun newInstance() = NewContactFragment()
    }
}
