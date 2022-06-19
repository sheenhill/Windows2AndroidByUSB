package link_room

import bean.FileInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import link.FileMessage
import link.Message
import link.TextMessage
import test.CommandUtil
import util.logI
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

class LinkVM(private val viewModelScope: CoroutineScope, private val rep: LinkRepository) {

    companion object {
        private const val TAG = "LinkVM"
    }

    private var _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    private var _msgList2 = MutableStateFlow<List<Message>>(emptyList())
    val msgList2: StateFlow<List<Message>> = _msgList2

    private var _file = MutableStateFlow(FileInfo())
    val file: StateFlow<FileInfo> = _file

    private val msgKey = AtomicInteger(0)

    fun connectSever() {
        viewModelScope.launch {
            rep.create()
            _connectionState.value = true
            val defConnect = async(Dispatchers.Default) {
                rep.connect()
            }
            val defOnData = async {
                rep.onData().collect {
                    logI("$TAG.onData() >>> $it")
                    _msgList2.value = _msgList2.value +
                            TextMessage(key = msgKey.getAndAdd(1), text = it, done = true)
                }
            }
            val defOnClose = async {
                rep.onClose().collect {
                    logI("$TAG.onClose() >>> $it")
                    _connectionState.value = false
                }
            }
            val defSendHeartbeat = async {
                rep.sendHeartbeatPacket()
            }
            awaitAll(defConnect, defOnData, defOnClose, defSendHeartbeat)
        }
    }

    fun sendMsg(str: String) {
        viewModelScope.launch {
            val key = msgKey.getAndAdd(1)
            _msgList2.value = _msgList2.value + TextMessage(key = key, text = str, done = false)
            rep.sendMsg(str)
            _msgList2.value = _msgList2.value.map {
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

    fun choiceFile() {
        viewModelScope.launch {
            val fileChooser = JFileChooser(FileSystemView.getFileSystemView())
            fileChooser.currentDirectory = File(System.getProperty("user.dir"))
            fileChooser.dialogTitle = "Link"
            fileChooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
            fileChooser.isAcceptAllFileFilterUsed = true
            fileChooser.selectedFile = null
            fileChooser.currentDirectory = null
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                logI("choose file or folder is: $file")
                _file.value = FileInfo(file.path)
            } else {
                logI("No Selection ")
                _file.value = FileInfo("")
            }
        }
    }

    fun dropFile(fileInfo: FileInfo) {
        viewModelScope.launch {
            _file.value = fileInfo
        }
    }

    fun cancelSendFile() {
        viewModelScope.launch {
            _file.value = FileInfo("")
        }
    }

    fun sendFile() {
        viewModelScope.launch {
            val key = msgKey.getAndAdd(1)
            _msgList2.value =
                _msgList2.value + FileMessage(key = key, fileName = _file.value.name, fileSize = _file.value.size)
            val file = _file.value
            CommandUtil.pushFile(file)
            _msgList2.value = _msgList2.value.map {
                if (it.key == key) {
                    when (it) {
                        is FileMessage -> it.copy(done = true)
                        is TextMessage -> it.copy(done = true)
                    }
                } else {
                    it
                }
            }
            rep.sendFileDone(file.name)
            _file.value = FileInfo("")
        }
    }
}