package me.vojinpuric.chatapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_new_message.*
import me.vojinpuric.chatapp.MainActivity
import me.vojinpuric.chatapp.R
import me.vojinpuric.chatapp.helpers.USER_KEY
import me.vojinpuric.chatapp.helpers.UserRequestViewHolder
import me.vojinpuric.chatapp.helpers.UserViewHolder
import me.vojinpuric.chatapp.model.User


class NewMessageFragment : Fragment() {
    private val mDatabase by lazy { FirebaseDatabase.getInstance() }
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var requestsAdapter: GroupAdapter<GroupieViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //adapter for friend requests
        requestsAdapter = initializeRequestsAdapter()
        requests_recycler_view.apply {
            adapter = requestsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        //adapter for existing contacts
        groupAdapter = initializeAdapter()
        users_recycler_view.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context)
        }
        groupAdapter.setOnItemClickListener { vh, v ->
            v.findNavController().navigate(
                R.id.action_newMessageFragment_to_chatFragment, bundleOf(
                    USER_KEY to (vh as UserViewHolder).user.uid
                )
            )
        }

    }

    private fun initializeRequestsAdapter(): GroupAdapter<GroupieViewHolder> {
        //empty adapter
        val adapter = GroupAdapter<GroupieViewHolder>()
        //connects to firebase and gets users
        val listener = object : UserRequestViewHolder.ResponseListener {
            override fun acceptCalled(uid: String) {
                mDatabase.getReference("/users/${mAuth.currentUser!!.uid}/contacts").push()
                    .setValue(uid)
                mDatabase.getReference("/users/${uid}/contacts").push()
                    .setValue(mAuth.currentUser!!.uid)
                MainActivity.currentUser!!.contacts[uid] = uid
                ignoreCalled(uid)
            }

            override fun ignoreCalled(uid: String) {
                val removeQuery: Query =
                    mDatabase.reference.child("friend-request/${mAuth.currentUser!!.uid}")
                        .orderByChild("uid")
                        .equalTo(uid).limitToFirst(1)
                removeQuery.addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (userSnapshot in dataSnapshot.children) {
                            Log.e("removing... ",userSnapshot.toString())
                            userSnapshot.ref.removeValue()
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) { Log.e("TAG", "onCancelled", databaseError.toException()) }
                })
            }

        }
        mDatabase.getReference("/friend-request/${mAuth.currentUser!!.uid}")
            .apply {
                addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                    }

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {
                        Log.e("onChildRemoved", "called")
                        //
                        Log.e("requestAdapter", requestsAdapter.itemCount.toString())
                    }

                    override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                        //converts users to Groupie viewHolders
                        Log.e("data changed", p0.toString())
                        p0.getValue(User::class.java)?.let { user ->
                            adapter.add(
                                UserRequestViewHolder(
                                    user,
                                    listener
                                )
                            )
                        }
                    }
                })
            }
        return adapter
    }

    private fun initializeAdapter(): GroupAdapter<GroupieViewHolder> {
        //empty adapter
        val adapter = GroupAdapter<GroupieViewHolder>()
        //connects to firebase and gets users
        mDatabase.getReference("/users/${mAuth.currentUser!!.uid}/contacts")
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val contactUid = p0.getValue(String::class.java)
                    mDatabase.getReference("/users/$contactUid").apply {
                        addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(p0: DataSnapshot) {
                                //converts users to Groupie viewHolders
                                p0.getValue(User::class.java)?.let {
                                    adapter.add(
                                        UserViewHolder(
                                            it
                                        )
                                    )
                                }
                            }
                        })
                    }
                }

                override fun onChildRemoved(p0: DataSnapshot) {}
            })

        return adapter
    }
    companion object {
        fun newInstance() = NewMessageFragment()
    }

}

