package me.vojinpuric.chatapp.model

data class ChatMessage(val fromID:String, val text:String,val toID:String, val timestamp:Long){
    constructor():this("","","",0)
}