package me.vojinpuric.chatapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class User(val uid: String,var profileImageUri: String ,val email: String, var contacts :HashMap<String,String> ) : Parcelable {
    constructor() : this("","","", HashMap())
}

