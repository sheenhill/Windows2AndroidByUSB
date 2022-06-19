package dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import ui.MColor


@Composable
fun SendFileDialog(
    visible: Boolean, fileName: String, send: () -> Unit, cancel: () -> Unit
) {
    val state = rememberDialogState(width = 320.dp, height = 240.dp)
    Dialog(
        visible = visible, onCloseRequest = {}, state = state, undecorated = true
    ) {
        val isSending = remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxSize().clip(shape = RoundedCornerShape(4.dp))
        ) {
                Row(
                    modifier = Modifier.height(40.dp).padding(start = 10.dp)
                ) {
                    Text(
                        text = "发送给：",
                        style = TextStyle(color = MColor.Text999),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                Row(
                    modifier = Modifier.height(60.dp).padding(start = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource("/drawable/avatar.jpg"),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "文件传输助手", style = TextStyle(fontSize = 13.sp), modifier = Modifier.padding(start = 10.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(64.dp)
                        .background(color = Color(0xFFF7F7F7)).padding(10.dp)
                ) {
                    Text(
                        text = "[文件] $fileName",
                        style = TextStyle(color = MColor.Text999),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isSending.value = true
                            send()
                        }, shape = RoundedCornerShape(0.dp), colors = ButtonDefaults.buttonColors(
                            backgroundColor = MColor.BgSend
                        ), elevation = null, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "发送", style = TextStyle(
                                color = Color.White, fontSize = 12.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Button(
                        onClick = {
                            cancel()
                        },
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MColor.White
                        ),
                        elevation = null,
                        border = BorderStroke(1.dp, MColor.F2),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "取消", style = TextStyle(
                                color = MColor.Text333, fontSize = 12.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                }
        }
    }
}



