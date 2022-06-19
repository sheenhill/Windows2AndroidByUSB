package com.sheenhill.windowlink

import android.Manifest
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.sheenhill.windowlink.link_room.LinkRepository
import com.sheenhill.windowlink.link_room.LinkRoomVM
import com.sheenhill.windowlink.link_room.LinkScreen
import com.sheenhill.windowlink.test.TestViewModel
import com.sheenhill.windowlink.ui.theme.MColor
import com.sheenhill.windowlink.ui.theme.WindowLinkTheme
import com.sheenhill.windowlink.util.logI
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import java.io.File

class MainActivity : ComponentActivity() {

    private val vm by lazy {
        ViewModelProvider(
            this, LinkRoomVM.provideFactory(LinkRepository())
        )[LinkRoomVM::class.java]
    }
    private val testVM by lazy {
        ViewModelProvider(
            this, TestViewModel.provideFactory(LinkRepository())
        )[TestViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WindowLinkTheme(
                darkTheme = true
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    FeatureThatRequiresStoragePermission {
                        LinkScreen(vm)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun FeatureThatRequiresStoragePermission(content: @Composable () -> Unit) {

        val storagePermissionState = rememberPermissionState(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        LaunchedEffect(storagePermissionState) {
            snapshotFlow {
                storagePermissionState.status
            }.filter { it == PermissionStatus.Granted }.collect {
                val dir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                logI("dir >>>> $dir")
                val file = File(dir.path, "link")
                // video文件夹不存在
                if (!file.exists()) {
                    logI("download/link/ is not exist")
                    // 创建文件夹
                    val bool = file.mkdirs();
                    logI("download/link/ mkdirs is ${bool}")
                } else {
                    logI("download/link/ is exist")
                }
            }
        }

        when (storagePermissionState.status) {
            PermissionStatus.Granted -> {
                content()
            }
            is PermissionStatus.Denied -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MColor.F2)
                ) {
                    val textToShow =
                        if ((storagePermissionState.status as PermissionStatus.Denied).shouldShowRationale) {
                            "读取文件权限是必须的"
                        } else {
                            "读取文件权限是必须的"
                        }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .aspectRatio(1.8f)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    textToShow,
                                    style = TextStyle(color = MColor.Text333, fontSize = 17.sp)
                                )
                            }
                            Button(onClick = { storagePermissionState.launchPermissionRequest() }) {
                                Text("同意", style = TextStyle(color = MColor.White))
                            }
                        }

                    }

                }
            }
        }
    }


}
