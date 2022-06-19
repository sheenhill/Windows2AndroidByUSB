package com.sheenhill.windowlink.link_room

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.sheenhill.windowlink.link.FileMessage
import com.sheenhill.windowlink.link.Message
import com.sheenhill.windowlink.link.TextMessage
import com.sheenhill.windowlink.util.logI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * @desc
 * @author Stanza
 * created at 2022/5/1 21:48
 */
class LinkRoomVM(private val rep: LinkRepository) : ViewModel() {

    private val _connectionState = MutableLiveData<Boolean>(false)
    val connectionState: LiveData<Boolean> = _connectionState

    private val _serverIsStarted = MutableLiveData<Boolean>(false)
    val serverIsStarted: LiveData<Boolean> = _serverIsStarted

    private val _msgList = MutableLiveData<List<Message>>(emptyList())
    val msgList: LiveData<List<Message>> = _msgList

    private val msgKey = AtomicInteger(0)

    companion object {
        private const val TAG = "LinkRoomVM"
        fun provideFactory(
            rep: LinkRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LinkRoomVM(rep) as T
            }
        }
    }

    fun startLink() {
        if (_serverIsStarted.value == true) return
        viewModelScope.launch(Dispatchers.Main) {
            val defStart = async(Dispatchers.IO) { rep.start() }
            _serverIsStarted.postValue(true)
            val defOnConnect = async {
                rep.onConnect().collect {
                    logI("$TAG.onConnect() >>> $it")
                    _connectionState.value = true
                }
            }
            val defOnData = async {
                rep.onData().collect {
                    logI("$TAG.onData() >>> $it")
                    _msgList.value = _msgList.value?.plus(
                        TextMessage(
                            key = msgKey.getAndAdd(1), text = it, done = true
                        )
                    )
                }
            }
            val defOnClose = async(Dispatchers.IO) {
                rep.onClose().collect {
                    logI("$TAG.onClose() >>> $it")
                    _connectionState.value = false
                }
            }
            awaitAll(defStart, defOnConnect, defOnData, defOnClose)
        }
    }

    fun sendMsg(str: String) {
        viewModelScope.launch {
            val key = msgKey.getAndAdd(1)
            _msgList.value = _msgList.value?.plus(TextMessage(key = key, text = str, done = false))
            rep.sendData(str)
            _msgList.value = _msgList.value?.map {
                if (it.key == key) {
                    when (it) {
                        is FileMessage -> it.copy(done = true)
                        is TextMessage -> it.copy(done = true)
                    }
                } else {
                    it
                }
            }
        }
    }

    fun newFile(ctx: Context, uri: Uri): Uri? {
        val resolver = ctx.contentResolver
        val fileName = ctx.getFileName(uri)!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pathUri = MediaStore.Downloads.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
            val details = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
            return resolver.insert(pathUri, details)
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(dir, fileName)
            file.outputStream().sink().buffer().writeUtf8("xxx")
            logI("(< Android 10)  file path  >>> ${file.path}")
            logI("(< Android 10)  file path  >>> ${file.absolutePath}")
            // 刷新文件夹（不需要）
//            MediaScannerConnection.scanFile(ctx, arrayOf(file.toString()), null
//            ) { path, uri ->
//                logI("ExternalStorage  >>>  Scanned $path:")
//                logI("ExternalStorage  >>>  uri=$uri")
//            }
            return file.toUri()
        }
    }

//    @SuppressLint("Range")
//    fun newFileBySAF(ctx: Context, uri: Uri): Uri? {
//        val cursor: Cursor? = ctx.contentResolver.query(
//            uri, null, null, null, null, null
//        )
//        var fileName = ""
//        cursor?.use {
//            if (it.moveToFirst()) {
//                fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
//                logI("File Name: $fileName")
//            }
//        }
//    }


    fun copy(ctx: Context, cur: Uri, new: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolver = ctx.contentResolver
            var isOk = false
            resolver.openInputStream(cur)?.source()?.buffer()?.use { source ->
                resolver.openOutputStream(new)?.sink()?.buffer()?.use {
                    source.readAll(it)
                    isOk = true
                }
            }
            logI("copy is ok >>> $isOk")
        }
    }


}
