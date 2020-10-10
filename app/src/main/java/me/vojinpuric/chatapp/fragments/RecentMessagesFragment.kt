package me.vojinpuric.chatapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_recent_messages.*
import me.vojinpuric.chatapp.MainActivity
import me.vojinpuric.chatapp.R
import me.vojinpuric.chatapp.helpers.RecentMessageViewHolder
import me.vojinpuric.chatapp.helpers.USER_KEY
import me.vojinpuric.chatapp.model.ChatMessage
import me.vojinpuric.chatapp.model.User


class RecentMessagesFragment : Fragment() {
    private val recentMessagesMap by lazy { HashMap<String, ChatMessage>() }
    private val mDatabase by lazy { FirebaseDatabase.getInstance() }
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var groupAdapter : GroupAdapter<GroupieViewHolder>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recent_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navigationController = view.findNavController()



        val myUid = mAuth.currentUser?.uid
        Log.e("onViewCreated:", "called")
        myUid?.let {
            groupAdapter = initializeAdapter()

            recycler_view.apply {
                adapter = groupAdapter
                layoutManager = LinearLayoutManager(context)
            }
            groupAdapter.setOnItemClickListener { vh, v ->
                val message = (vh as RecentMessageViewHolder).message
                val contactId = if (myUid == message.fromID) message.toID else message.fromID
                val userRef = mDatabase.getReference("/users/$contactId")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val user = p0.getValue(User::class.java)
                            //user.username
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })
                navigationController.navigate(
                    R.id.action_recentMessagesFragment_to_chatFragment, bundleOf(
                        USER_KEY to contactId
                    )
                )
            }
        }

    }

    override fun onResume() {
        if(this::groupAdapter.isInitialized){
            refreshRecentMessages(groupAdapter)
        }
        super.onResume()
    }

    private fun initializeAdapter(): GroupAdapter<GroupieViewHolder> {
        //empty adapter
        val adapter = GroupAdapter<GroupieViewHolder>()
        //connects to firebase and gets users
        mDatabase.getReference("/recent-messages/${mAuth.currentUser!!.uid}")
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                    recentMessagesMap[p0.key!!] = chatMessage
                    refreshRecentMessages(adapter)
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                    recentMessagesMap[p0.key!!] = chatMessage
                    refreshRecentMessages(adapter)
                }

                override fun onChildRemoved(p0: DataSnapshot) {}
            })
        return adapter
    }

    private fun refreshRecentMessages(adapter: GroupAdapter<GroupieViewHolder>) {
        adapter.clear()
        recentMessagesMap.values.sortedByDescending { it.timestamp }.forEach {
            adapter.add(RecentMessageViewHolder(it))
        }
    }


    companion object {
        fun newInstance() = RecentMessagesFragment()
    }

}
