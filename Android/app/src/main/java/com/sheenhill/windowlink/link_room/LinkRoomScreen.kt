package com.sheenhill.windowlink.link_room

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.systemBarsPadding
import com.sheenhill.windowlink.BuildConfig
import com.sheenhill.windowlink.R
import com.sheenhill.windowlink.link.FileMessage
import com.sheenhill.windowlink.link.Message
import com.sheenhill.windowlink.link.TextMessage
import com.sheenhill.windowlink.ui.theme.MColor
import com.sheenhill.windowlink.util.logI
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import java.io.File

/**
 * @desc
 * @author Stanza
 * created at 2022/5/1 21:49
 */

fun Context.getFileName(uri: Uri): String? = when (uri.scheme) {
    ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
    else -> uri.path?.let(::File)?.name
}

private fun Context.getContentFileName(uri: Uri): String? = runCatching {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
    }
}.getOrNull()

@Composable
fun LinkScreen(vm: LinkRoomVM) {
    val connectState = vm.connectionState.observeAsState(false)
    val openState = vm.serverIsStarted.observeAsState(false)
    val msgList = vm.msgList.observeAsState(emptyList())
    val ctx = LocalContext.current.applicationContext
    val clipB = LocalClipboardManager.current
    val result = remember { mutableStateOf(false) }
    val createFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        logI("CreateDocument  >>>${uri}")
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        logI("OpenDocument >>>${uri?.let { ctx.getFileName(it) }}")
        uri?.let{
            val newFile = vm.newFile(ctx, uri)
//            val newFile = vm.newFileBySAF(ctx, uri)
//            createFileLauncher.launch(newFile)
            newFile?.let {
                vm.copy(ctx, uri, it)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MColor.F2)
            .statusBarsPadding()
            .navigationBarsWithImePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        AppBar(
            isOpened = openState.value, start = {
                vm.startLink()
            }, connectState = connectState.value
        )
        TalkSpace(modifier = Modifier
            .fillMaxWidth()
            .weight(1f), list = msgList.value, copy = {
            clipB.setText(AnnotatedString(it))
        })
        InputSpace(send = {
            vm.sendMsg(it)
        }, openDir = {
            launcher.launch(arrayOf("*/*"))
        })
    }
}


@Composable
internal fun AppBar(
    isOpened: Boolean, start: () -> Unit = {}, connectState: Boolean
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .background(MColor.BG1E)
        .clickable {
            start()
        }) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "文件传输助手", style = TextStyle(
                    color = MColor.TextEF, fontSize = 16.sp, fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = if (connectState) "电脑已连接" else "无设备连接~", style = TextStyle(
                    color = MColor.Text999, fontSize = 9.sp, fontWeight = FontWeight.Medium
                )
            )
        }

        Icon(
            modifier = Modifier
                .padding(end = 24.dp)
                .size(20.dp)
                .align(Alignment.CenterEnd),
            painter = painterResource(R.drawable.ic_link),
            contentDescription = "已连接",
            tint = if (isOpened) Color.Unspecified else MColor.E7
        )
        Text(
            text = "V ${BuildConfig.VERSION_NAME}", style = TextStyle(
                color = MColor.Text999, fontSize = 9.sp, fontWeight = FontWeight.Medium
            ), modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp)
        )
    }
}

@Composable
fun TalkSpace(
    modifier: Modifier = Modifier, list: List<Message>, copy: (String) -> Unit
) {
    val state = rememberLazyListState()
    LaunchedEffect(list.size) {
        snapshotFlow { list.size }.filter { it != 0 }.collect {
            state.animateScrollToItem(it - 1)
        }
    }
    LazyColumn(
        modifier = modifier.background(MColor.BgTalkRoom),
        contentPadding = PaddingValues(vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
        state = state
    ) {
        items(list, key = { it.key }) { msg ->
            TalkCell(msg) { text ->
                copy(text)
            }
        }
    }
}

@Composable
fun TalkCell(msg: Message, cellOnClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
    ) {
        Spacer(modifier = Modifier.width(50.dp))
        when (msg) {
            is TextMessage -> TextCell(msg = msg, textOnClick = {
                cellOnClick(it)
            })
            is FileMessage -> FileCell(msg = msg)
        }

        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            painter = painterResource(R.drawable.avatar),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .size(40.dp),
            contentDescription = "",
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
internal fun InputSpace(
    send: (String) -> Unit, openDir: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MColor.BG1E)
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            modifier = Modifier
                .padding(start = 8.dp)
                .size(24.dp)
                .align(Alignment.CenterVertically)
                .clickable {
                    openDir()
                },
            contentDescription = "",
            tint = MColor.TextEF
        )
        val msg = remember { mutableStateOf("") }
        BasicTextField(
            value = msg.value,
            onValueChange = {
                msg.value = it
            },
            textStyle = TextStyle(color = MColor.TextEF),
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .weight(1f)
                .padding(4.dp)
                .clip(shape = RoundedCornerShape(6.dp))
                .background(MColor.BG29)
        )
        Button(
            modifier = Modifier
                .size(60.dp, 34.dp)
                .align(Alignment.CenterVertically),
            onClick = {
                send(msg.value)
                msg.value = ""
            },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
        ) {
            Text(
                text = "发送", style = TextStyle(color = MColor.White)
            )
        }
    }
}


@Composable
fun TextCell(msg: TextMessage, textOnClick: (String) -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .weight(1f),
//        horizontalArrangement = Arrangement.End
//    ) {
//        Text(
//            text = text,
//            modifier = Modifier
//                .clip(RoundedCornerShape(4.dp))
//                .background(MColor.BgTalkCell)
//                .heightIn(min = 40.dp, max = Dp.Infinity)
//                .padding(vertical = 6.dp, horizontal = 8.dp)
//                .clickable {
//                    cellOnClick()
//                },
//            style = TextStyle(
//                color = MColor.Text333,
//                fontSize = 16.sp
//            )
//        )
//    }
//
//    Canvas(
//        modifier = Modifier
//            .padding(top = 14.dp)
//            .size(6.dp, 20.dp)
//    ) {
//        val path = Path()
//        path.lineTo(6.dp.toPx(), 7.dp.toPx())
//        path.lineTo(0f, 14.dp.toPx())
//        path.lineTo(0f, 0f)
//        drawPath(
//            path = path,
//            color = MColor.BgTalkCell
//        )
//    }
//
    Row {
        if (!msg.done) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(20.dp)
            ) {
                val circleSize = (DefaultSizes.arcRadius + DefaultSizes.strokeWidth) * 2
                CircularProgressIndicator(
                    color = MColor.Text333,
                    strokeWidth = DefaultSizes.strokeWidth,
                    modifier = Modifier.size(circleSize),
                )
            }
        }
        Text(
            text = msg.text,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MColor.BgTalkCell)
                .heightIn(min = 40.dp, max = Dp.Infinity)
                .padding(vertical = 6.dp, horizontal = 8.dp)
                .clickable {
                    textOnClick(msg.text)
                },
            style = TextStyle(
                color = MColor.Text333, fontSize = 16.sp
            )
        )
        Canvas(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(6.dp, 20.dp)
        ) {
            val path = Path()
            path.lineTo(6.dp.toPx(), 5.dp.toPx())
            path.lineTo(0f, 10.dp.toPx())
            path.lineTo(0f, 0f)
            drawPath(
                path = path, color = MColor.BgTalkCell
            )
        }
    }
}


@Composable
fun FileCell(msg: FileMessage) {
    Row {
        if (!msg.done) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(20.dp)
            ) {
                val circleSize = (DefaultSizes.arcRadius + DefaultSizes.strokeWidth) * 2
                CircularProgressIndicator(
                    color = MColor.Text333,
                    strokeWidth = DefaultSizes.strokeWidth,
                    modifier = Modifier.size(circleSize),
                )
            }
        }

        Column(
            modifier = Modifier
                .background(
                    color = Color.White
                )
                .size(240.dp, 94.dp)
                .border(BorderStroke(1.dp, MColor.F2))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = msg.fileName, modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                    )
                    Text(
                        text = msg.fileSizeInfo,
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                        style = TextStyle(color = MColor.Text999, fontSize = 12.sp)
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_file),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
                )
            }
            Divider(color = MColor.F2)
            Box(
                modifier = Modifier
                    .height(23.dp)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = msg.fromInfo,
                    modifier = Modifier.align(Alignment.CenterStart),
                    style = TextStyle(color = MColor.Text999, fontSize = 12.sp)
                )
            }
        }
    }
}


@Immutable
private data class SwipeRefreshIndicatorSizes(
    val size: Dp,
    val arcRadius: Dp,
    val strokeWidth: Dp,
    val arrowWidth: Dp,
    val arrowHeight: Dp,
)

private val DefaultSizes = SwipeRefreshIndicatorSizes(
    size = 40.dp,
    arcRadius = 7.5.dp,
    strokeWidth = 2.5.dp,
    arrowWidth = 10.dp,
    arrowHeight = 5.dp,
)