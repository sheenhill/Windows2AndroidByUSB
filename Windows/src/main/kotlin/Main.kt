// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import link_room.LinkRepository
import link_room.LinkScreen
import link_room.LinkVM
import test.TestViewModel

fun main() = application {
    val scope = rememberCoroutineScope()
    val rep = LinkRepository(Dispatchers.IO)
    val vm = LinkVM(scope, rep)
    val testVM = TestViewModel(scope, rep)
    val windowState = rememberWindowState(
        size = DpSize(600.dp, 600.dp)
    )
    Window(
        title = "Link v0.2",
        icon = painterResource("/drawable/icon.png"),
        onCloseRequest = ::exitApplication,
        state = windowState
    ) {
        LinkScreen(vm = vm)
//        TestScreen(vm = testVM)
    }
}





