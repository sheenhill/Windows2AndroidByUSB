package link_room

import LinkClient
import bean.FileInfo
import kotlinx.coroutines.*
import link.Sign
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import okio.buffer
import okio.source
import test.CommandUtil
import util.logI
import kotlin.coroutines.CoroutineContext

class LinkRepository(val cc: CoroutineContext) {
    companion object {
        private const val TAG = "LinkRepository"
    }

    private var client: LinkClient? = null

    suspend fun create() = withContext(cc) {
        logI("$TAG.connect")
        val devices = CommandUtil.checkDevices()
        if (devices.isEmpty()) {
            logI("Link check >>> no device")
            return@withContext
        }
        CommandUtil.forward()
        client = LinkClient(11111)
    }

    suspend fun connect() {
        client!!.connect()
    }

    suspend fun sendHeartbeatPacket() {
        logI("$TAG.sendHeartbeatPacket()")
        while (true) {
            delay(1000)
            logI("$TAG heartbeat~ ")
            val res = client?.emit(Sign.HEARTBEAT, "client".toByteArray())
            if (res != 0) {
                logI("$TAG send heartbeat  fail~ ")
                client?.msgFlow?.emit(Sign.CLOSE to "")
                break
            }
        }
    }


    suspend fun onData() = flow<String> {
        logI("${TAG}.onData()  client={$client} msgFlow={${client?.msgFlow}}")
        client?.msgFlow?.collect {
            logI(" Link msg [ SIGN_DATA ]")
            if (it.first == Sign.DATA) {
                logI(" Link msg [ SIGN_DATA ]")
                emit(it.second)
            }
        }
    }

    suspend fun onClose() = flow {
        logI("${TAG}.onClose()")
        client?.msgFlow?.collect {
            if (it.first == Sign.CLOSE) {
                emit(it.second)
                logI(" Link msg [ Sign.CLOSE ]")
            }
        }
    }

    fun sendMsg(msg: String) {
        logI("MyRepository.sendMsg")
        client?.emit(Sign.DATA, msg.toByteArray())
    }

    fun sendFileDone(msg: String) {
        logI("${TAG}.sendFileDone")
        client?.emit(Sign.FILE_DONE, msg.toByteArray())
    }


    suspend fun pullFile(file: FileInfo): Boolean = withContext(cc) {
        try {
            val builder = ProcessBuilder()
            builder.command("cmd.exe", "/c", "adb pull ${file.path} sdcard/Download/${file.name}")
            val process = builder.start()
            val input = process.inputStream
            logI("pullFile (start)")
            val buffer = input.source().buffer()
            val msg = buffer.readUtf8()
            logI("pullFile >>>> $msg")
            process.waitFor()
            return@withContext true
        } catch (e: Exception) {
            return@withContext false
        }
    }


}