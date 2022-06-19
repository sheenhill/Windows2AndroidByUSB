package test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import link_room.LinkRepository

/**
 * @desc
 * @author Stanza
 * created at 2022/4/20 13:20
 */
internal class TestViewModel(private val viewModelScope: CoroutineScope, private val rep: LinkRepository) {

    private var _connectionState = MutableStateFlow<Boolean>(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    private var _msgFlow = MutableStateFlow<String>("")
    val msgFlow: StateFlow<String> = _msgFlow

    fun startLink() {
        viewModelScope.launch(rep.cc) {
            rep.create()
            onData()
            // 通知手机，电脑上线了
            rep.sendHeartbeatPacket()
        }
    }

    private fun onData(){
        viewModelScope.launch(rep.cc) {
            rep.onData().collect {

            }
        }
    }

    fun sendMsg() {
        viewModelScope.launch(rep.cc) {
            rep.sendMsg(
                """收到了
                |我是手机
            """.trimMargin()
            )
        }
    }
}

