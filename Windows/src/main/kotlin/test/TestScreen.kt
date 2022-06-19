package test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.MColor
import util.logI

@Composable
internal fun TestScreen(vm: TestViewModel) {
    val state = vm.connectionState.collectAsState(false)
    val msgData = vm.msgFlow.collectAsState()
    logI("msgData >>> ${msgData.value}")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MColor.White)
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "手机信道状态：",
                style = TextStyle(
                    color = MColor.Text333,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (state.value) "已连接" else "已断开",
                style = TextStyle(
                    color = if (state.value) MColor.LotteryBlue else MColor.Text999,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "电脑信道状态：",
                style = TextStyle(
                    color = MColor.Text333,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (state.value) "已连接" else "已断开",
                style = TextStyle(
                    color = if (state.value) MColor.LotteryBlue else MColor.Text999,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            modifier = Modifier
                .size(250.dp, 100.dp)
                .padding(16.dp),
            onClick = { vm.startLink() },
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                "启动电脑信道",
                style = TextStyle(
                    color = MColor.TextEF,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Button(
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .size(250.dp, 100.dp)
                .padding(16.dp),
            onClick = {  }
        ) {
            Text(
                "发送数据",
                style = TextStyle(
                    color = MColor.TextEF,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

