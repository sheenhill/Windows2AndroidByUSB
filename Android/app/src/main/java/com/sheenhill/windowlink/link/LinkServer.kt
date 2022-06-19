package com.sheenhill.windowlink.link


import com.sheenhill.windowlink.util.logI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okio.buffer
import okio.sink
import okio.source
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.*


object LinkServer {
    private const val TAG = "LinkServer"

    private val server = ServerSocket(22222)

    private var socket: Socket? = null

    val message = MutableSharedFlow<Pair<Int, String>>()

    suspend fun start() {
        logI("$TAG.start()")
        while (true) {
            socket = server.accept()
            socket?.let { socket ->
                socket.keepAlive = true
                var lastTime = System.currentTimeMillis()
                val source = socket.source().buffer()
                message.emit(
                    Pair(
                        first = Sign.CONNECT, second = ""
                    )
                )
                while (!source.exhausted()) {
                    val length = source.readInt()
                    val type = source.readInt()
                    val data = source.readUtf8(length.toLong())
                    val curTime = System.currentTimeMillis()
                    if (curTime - lastTime > 2000) {
                        break
                    }
                    lastTime = curTime
                    logI("length = $length , type = $type , data = $data")
                    message.emit(
                        Pair(
                            first = type, second = data
                        )
                    )
                }
                logI("source.exhausted()")
                message.emit(
                    Pair(
                        first = Sign.CLOSE, second = "client"
                    )
                )
            }

        }
    }

    suspend fun emit(sign: Int, msg: ByteArray): Int = withContext(Dispatchers.IO){
        return@withContext try {
            socket!!.sink().buffer().writeInt(msg.size).writeInt(sign).write(msg).emit()
            0
        } catch (e: java.lang.NullPointerException) {
            logI("${TAG}.emit Exception >>> socket is NULL")
            -1
        } catch (e: SocketException) {
            logI("${TAG}.emit Exception >>> socket is CLOSED")
            -2
        }
    }
}


