package test

import bean.FileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import util.logI
import okio.buffer
import okio.source

object CommandUtil {

        private const val TAG = "CommandCase"

        suspend fun checkDevices(): List<String> = withContext(Dispatchers.IO) {
            val builder = ProcessBuilder()
            builder.command("cmd.exe", "/c", "adb devices")
            val process = builder.start()
            val buffer = process.inputStream.source().buffer()
            buffer.readUtf8Line() // 首行无用
            val devices = mutableListOf<String>()
            var line2 = buffer.readUtf8Line()
            while (!line2.isNullOrEmpty()) {
                devices.add(line2.substring(0 until line2.indexOf('\t')))
                line2 = buffer.readUtf8Line()
            }
            logI("$TAG.checkDevices >>> $devices")
            process.waitFor()
            devices
        }

        suspend fun forward() {
            withContext(Dispatchers.IO) {
                val builder = ProcessBuilder()
                builder.command("cmd.exe", "/c", "adb forward tcp:11111 tcp:22222")
                val process = builder.start()
                val input = process.inputStream
                val buffer = input.source().buffer()
                val msg = buffer.readUtf8()
                logI("$TAG.forward >>>> $msg")
                process.waitFor()
            }
        }



        suspend fun exec() {
            withContext(Dispatchers.IO) {
                val builder = ProcessBuilder()
                builder.command("cmd.exe", "/c", "adb")
                val process = builder.start()
                val input = process.inputStream
                logI("exec (before)")
                val buffer = input.source().buffer()
                val msg = buffer.readUtf8()
                logI("buffer >>>> $msg")
                process.waitFor()
            }
        }

        /**
         * 传输文件到手机
         */
        suspend fun pushFile(file: FileInfo):Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val builder = ProcessBuilder()
                    builder.command("cmd.exe", "/c", "adb push ${file.path} sdcard/Download/link/${file.name}")
                    val process = builder.start()
                    val input = process.inputStream
                    logI("pushFile (start)")
                    val buffer = input.source().buffer()
                    val msg = buffer.readUtf8()
                    logI("pushFile >>>> $msg")
                    process.waitFor()
                    return@withContext true
                }catch (e:Exception){
                    return@withContext false
                }
            }
    }
