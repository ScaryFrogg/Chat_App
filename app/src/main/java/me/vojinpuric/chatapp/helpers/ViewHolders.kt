package me.vojinpuric.chatapp.helpers

import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import me.vojinpuric.chatapp.R
import me.vojinpuric.chatapp.model.ChatMessage
import me.vojinpuric.chatapp.model.User

class UserViewHolder(val user: User) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.user_name).text = user.email
        Picasso.get().load(user.profileImageUri).into(
            viewHolder.itemView.findViewById<ImageView>(
                R.id.user_image
            )
        )
    }

    override fun getLayout() = R.layout.user_row_item
}

class RecentMessageViewHolder(val message: ChatMessage) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val contactId =
            if (FirebaseAuth.getInstance().uid == message.fromID) message.toID else message.fromID

        FirebaseDatabase.getInstance().getReference("/users/$contactId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue( User::class.java)
                    //Log.e("user",user.toString())
                    user?.let {
                        viewHolder.itemView.findViewById<TextView>(R.id.user_email).text = it.email
                        Picasso.get().load(user.profileImageUri).into(viewHolder.itemView.findViewById<ImageView>(R.id.user_image))
                    }
                }
            }
            )
        viewHolder.itemView.findViewById<TextView>(R.id.message_text).text = message.text

    }

    override fun getLayout() = R.layout.recent_message_item
}

class UserRequestViewHolder(private val user: User, private val listener: ResponseListener) :
    Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.user_name).text = user.email
        Picasso.get().load(user.profileImageUri).into(
            viewHolder.itemView.findViewById<ImageView>(
                R.id.user_image
            )
        )
        viewHolder.itemView.findViewById<Button>(R.id.btnAccept).setOnClickListener {
            //Log.e("btnAccept", "accept")
            listener.acceptCalled(user.uid)
        }
        viewHolder.itemView.findViewById<Button>(R.id.btnIgnore).setOnClickListener {
            //Log.e("btnIgnore", "ignore")
            listener.ignoreCalled(user.uid)
        }
    }

    override fun getLayout() = R.layout.friend_request_item

    interface ResponseListener {
        fun acceptCalled(uid: String)
        fun ignoreCalled(uid: String)
    }
}

class ChatFromItem(private val text: String, private val imageUri: String) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Picasso.get().load(imageUri).into(viewHolder.itemView.findViewById<ImageView>(R.id.user_image))
        viewHolder.itemView.findViewById<TextView>(R.id.message_text).text = text
    }

    override fun getLayout() = R.layout.from_message
}

class ChatToItem(private val text: String, private val imageUri: String) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Picasso.get().load(imageUri).into(viewHolder.itemView.findViewById<ImageView>(R.id.user_image))
        viewHolder.itemView.findViewById<TextView>(R.id.message_text).text = text

    }

    override fun getLayout() = R.layout.to_message
}