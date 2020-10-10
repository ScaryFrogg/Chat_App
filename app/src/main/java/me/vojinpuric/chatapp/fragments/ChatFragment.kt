package me.vojinpuric.chatapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_chat.*
import me.vojinpuric.chatapp.R
import me.vojinpuric.chatapp.helpers.ChatFromItem
import me.vojinpuric.chatapp.helpers.ChatToItem
import me.vojinpuric.chatapp.helpers.PROFILE_IMAGE_PLACEHOLDER
import me.vojinpuric.chatapp.helpers.USER_KEY
import me.vojinpuric.chatapp.model.ChatMessage
import me.vojinpuric.chatapp.model.User

class ChatFragment : Fragment() {
    private val mDatabase by lazy { FirebaseDatabase.getInstance() }
    private val mAuth by lazy { FirebaseAuth.getInstance() }

    companion object {
        var partnerImage = PROFILE_IMAGE_PLACEHOLDER
        var myImage = PROFILE_IMAGE_PLACEHOLDER
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = requireArguments().getString(USER_KEY)!!
        val adapter = GroupAdapter<GroupieViewHolder>()

        val myUid = mAuth.currentUser!!.uid

        recycler_view.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)
        }
        //get chat contact avatar image
        mDatabase.getReference("/users/$userId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(User::class.java)
                    //Log.e("user",user.toString())
                    user?.let {
                        partnerImage = it.profileImageUri
                    }
                }
            }
            )
        //get yours avatar image
        mDatabase.getReference("/users/$myUid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(User::class.java)
                    //Log.e("user",user.toString())
                    user?.let {
                        myImage = it.profileImageUri
                    }
                }
            }
            )

        mDatabase.getReference("/user-messages/$myUid/${userId}")
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val chatMessage = p0.getValue(ChatMessage::class.java)
                    if (chatMessage!!.fromID == myUid) {
                        adapter.add(
                            ChatToItem(
                                chatMessage.text,
                                myImage
                            )
                        )
                    } else {
                        adapter.add(
                            ChatFromItem(
                                chatMessage.text,
                                partnerImage
                            )
                        )
                    }
                    //do not remove let
                    recycler_view?.let{
                        it.scrollToPosition(adapter.itemCount - 1)
                    }
                }

                override fun onChildRemoved(p0: DataSnapshot) {}
            })

        btnSend.setOnClickListener {
            if (messageBox.text.isNotBlank()) {
                val chatMessage = ChatMessage(
                    myUid,
                    messageBox.text.toString(),
                    userId,
                    System.currentTimeMillis()
                )
                mDatabase.getReference("/user-messages/$myUid/${userId}").push()
                    .setValue(chatMessage)
                mDatabase.getReference("/user-messages/${userId}/$myUid").push()
                    .setValue(chatMessage)
                mDatabase.getReference("/recent-messages/${userId}/$myUid").setValue(chatMessage)
                mDatabase.getReference("/recent-messages/$myUid/${userId}").setValue(chatMessage)
            }
            messageBox.text.clear()
            recycler_view.scrollToPosition(adapter.itemCount - 1)
        }
    }
    

}
