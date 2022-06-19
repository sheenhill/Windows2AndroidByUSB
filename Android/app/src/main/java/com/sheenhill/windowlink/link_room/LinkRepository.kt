package com.sheenhill.windowlink.link_room

import com.sheenhill.windowlink.link.LinkServer
import com.sheenhill.windowlink.link.Sign
import com.sheenhill.windowlink.util.logI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.net.Socket
import kotlin.coroutines.CoroutineContext


class LinkRepository(val cc: CoroutineContext = Dispatchers.IO) {
    companion object{
        private const val TAG = "LinkRepository"
    }

    private val server: LinkServer = LinkServer


    suspend fun start() {
        server.start()
    }

    fun onData() = flow<String> {
        logI("${TAG}.onData()")
        server.message.collect {
            if (it.first == Sign.DATA) {
                emit(it.second)
                logI(" Link msg [ SIGN_DATA ]")
            }
        }
    }

    fun onConnect() = flow<Boolean> {
        logI("${TAG}.onConnect()")
        server.message.collect {
            logI(" Link ---> [ SIGN_HEARTBEAT ]")
            if (Sign.HEARTBEAT == it.first)
                emit(true)
        }
    }

    fun onClose() = flow<Boolean> {
        server.message.collect {
            logI(" Link ---> [ SIGN_CLOSE ]")
            if (Sign.CLOSE == it.first)
                emit(false)
        }
    }

     suspend fun sendData(msg: String){
            logI("${TAG}.sendMsg")
            server.emit(Sign.DATA, msg.toByteArray())
    }

}