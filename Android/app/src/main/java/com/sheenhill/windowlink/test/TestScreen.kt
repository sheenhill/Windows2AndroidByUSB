package com.sheenhill.windowlink.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sheenhill.windowlink.ui.theme.MColor
import com.sheenhill.windowlink.util.logI

@Composable
internal fun TestScreen(vm: TestViewModel) {
    val state = vm.connectionState.observeAsState(false)
    val msgData = vm.msgFlow.observeAsState()
    logI("msgData >>> ${msgData.value}")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MColor.White)
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.height(100.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Row(
            modifier = Modifier.height(100.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    fontSize = 40.sp,
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
                "启动手机信道",
                style = TextStyle(
                    color = MColor.PurpleGrey80,
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
                    color = MColor.PurpleGrey80,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

