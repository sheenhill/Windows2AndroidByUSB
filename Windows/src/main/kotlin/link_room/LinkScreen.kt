package link_room

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bean.FileInfo
import composable.DropInput
import dialog.SendFileDialog
import link.FileMessage
import link.Message
import link.TextMessage
import ui.MColor
import util.logI

@Composable
fun LinkScreen(
    vm: LinkVM
) {
    val state = vm.connectionState.collectAsState(false)
    val msgList2 = vm.msgList2.collectAsState()
    val file = vm.file.collectAsState(FileInfo(""))
    val showSendDialog = remember { derivedStateOf { file.value.path.isNotEmpty() } }
    val clipB = LocalClipboardManager.current
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Header(isConnected = state.value, reconnect = {
                vm.connectSever()
            })
            Divider(color = MColor.E7)
            TalkSpace(modifier = Modifier.fillMaxSize(1f).weight(1f), list = msgList2.value, copy = {
                clipB.setText(AnnotatedString(it))
            })
            Divider(color = MColor.E7)
            OperationSpace(send = {
                vm.sendMsg(it)
            }, choiceFile = {
                vm.choiceFile()
            }, dropFile = {
                vm.dropFile(it)
            })
        }

        SendFileDialog(visible = showSendDialog.value, fileName = file.value.name, send = {
            logI("file info >>> ${file.value}")
            vm.sendFile()
        }, cancel = {
            vm.cancelSendFile()
        })
    }

}

@Composable
fun Header(isConnected: Boolean, reconnect: () -> Unit = {}) {
    Row(modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 24.dp), text = "文件传输助手", style = TextStyle(fontSize = 18.sp)
        )
        Icon(
            modifier = Modifier.padding(start = 8.dp).align(Alignment.CenterVertically).size(16.dp).clickable {
                reconnect()
            },
            painter = painterResource("/drawable/link.svg"),
            contentDescription = "已连接",
            tint = if (isConnected) Color.Unspecified else MColor.E7
        )
    }
}


@Composable
fun TalkSpace(
    modifier: Modifier = Modifier, list: List<Message>, copy: (String) -> Unit
) {
    val state = rememberLazyListState()
    LaunchedEffect(list.size) {
        state.animateScrollToItem(if (list.size - 1 > 0) list.size - 1 else 0)
    }
    LazyColumn(
        modifier = modifier.background(MColor.F2),
        contentPadding = PaddingValues(vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
        state = state,
    ) {
        items(list, key = { it.key }) { msg ->
            TalkCell(msg) { text->
                copy(text)
            }
        }
    }
}

@Composable
fun TalkCell(msg: Message, textOnClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
    ) {
        Spacer(modifier = Modifier.width(100.dp))
        when (msg) {
            is TextMessage -> TextCell(msg = msg, textOnClick = {
                textOnClick(it)
            })
            is FileMessage -> FileCell(msg = msg)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            painter = painterResource("/drawable/avatar.jpg"),
            modifier = Modifier.clip(RoundedCornerShape(4.dp)).size(34.dp),
            contentDescription = "",
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(28.dp))
    }
}

@Composable
fun TextCell(msg: TextMessage, textOnClick: (String) -> Unit) {
    Row {
        if(!msg.done){
            Box(modifier = Modifier.padding(top = 8.dp).size(20.dp)) {
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
            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(MColor.BgTalkCell)
                .heightIn(min = 40.dp, max = Dp.Infinity).padding(vertical = 6.dp, horizontal = 8.dp).clickable {
                    textOnClick(msg.text)
                },
            style = TextStyle(
                color = MColor.Text333, fontSize = 16.sp
            )
        )
        Canvas(modifier = Modifier.padding(top = 10.dp).size(6.dp, 20.dp)) {
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
        if(!msg.done) {
            Box(modifier = Modifier.padding(top = 8.dp).size(20.dp)) {
                val circleSize = (DefaultSizes.arcRadius + DefaultSizes.strokeWidth) * 2
                CircularProgressIndicator(
                    color = MColor.Text333,
                    strokeWidth = DefaultSizes.strokeWidth,
                    modifier = Modifier.size(circleSize),
                )
            }
        }

        Column(
            modifier = Modifier.background(
                color = Color.White
            ).size(240.dp, 94.dp).border(BorderStroke(1.dp, MColor.F2))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(70.dp), horizontalArrangement = Arrangement.SpaceBetween
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
                    painter = painterResource("/drawable/ic_file.svg"),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
                )
            }
            Divider(color = MColor.F2)
            Box(
                modifier = Modifier.height(23.dp).padding(horizontal = 12.dp),
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

@Composable
fun OperationSpace(
    send: (String) -> Unit, choiceFile: () -> Unit, dropFile: (FileInfo) -> Unit
) {
    Column(
        modifier = Modifier.background(Color.White).height(160.dp)
    ) {
        Icon(
            painter = painterResource("/drawable/add.svg"),
            modifier = Modifier.padding(start = 16.dp, top = 12.dp).size(16.dp).clickable {
                choiceFile()
            },
            contentDescription = "",
            tint = MColor.Text333
        )
        val msg = remember { mutableStateOf("") }
        DropInput(modifier = Modifier.weight(1f), msg = msg.value, change = {
            msg.value = it
        }, dropFile = {
            dropFile(it)
        })
        Button(
            onClick = {
                send(msg.value)
                msg.value = ""
            }, modifier = Modifier.align(Alignment.End).padding(end = 24.dp, bottom = 8.dp)
        ) {
            Text(text = "发送")
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


//@Composable
//@Preview
//fun TestScreen() {
////    App()
//}
