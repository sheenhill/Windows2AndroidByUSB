package com.sheenhill.windowlink.test

import androidx.lifecycle.*
import com.sheenhill.windowlink.link_room.LinkRepository
import com.sheenhill.windowlink.util.logI
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * @desc
 * @author Stanza
 * created at 2022/4/20 13:20
 */
internal class TestViewModel(private val rep: LinkRepository) : ViewModel() {

    private var _connectionState = MutableLiveData<Boolean>(false)
    val connectionState: LiveData<Boolean> = _connectionState

    private var _msgFlow = MutableLiveData<String>()
    val msgFlow: LiveData<String> = _msgFlow

    companion object {
        fun provideFactory(
            rep: LinkRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TestViewModel(rep) as T
            }
        }
    }

    fun startLink() {
        viewModelScope.launch(rep.cc) {

        }
    }


    fun sendMsg() {
        viewModelScope.launch(rep.cc) {
            rep.sendData(
                """收到了
                |我是手机
            """.trimMargin()
            )
        }
    }
}

