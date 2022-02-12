package com.centyllion.ui.tabs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.ui.AppContext
import com.centyllion.ui.icon
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.AlignJustify
import java.text.DateFormat
import java.util.*

object LogsTab : Tab {
    override val name = "Logs"
    override val icon = FontAwesomeIcons.Solid.AlignJustify

    @Composable
    override fun header(appContext: AppContext) {
        super.header(appContext)
        val count = appContext.unseenLogs
        if (count > 0) Text( text = "($count)", fontSize = 12.sp)
    }

    @Composable
    override fun content(appContext: AppContext) {
        Box(Modifier.padding(4.dp)) {

            val listState = rememberLazyListState()

            LazyColumn(state = listState) {
                items(appContext.logs) { log ->

                    Row(modifier = Modifier.padding(6.dp)) {

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = log.severity.icon, contentDescription = null,
                            tint = appContext.theme.severityColor(log.severity),
                            modifier = Modifier.fillMaxHeight().align(Alignment.CenterVertically),
                        )

                        Spacer(modifier = Modifier.width(8.dp))


                        Text(
                            text = AnnotatedString(log.message),
                            softWrap = true,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1F).align(Alignment.CenterVertically),
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = DateFormat.getInstance().format(Date(log.timestamp)),
                            softWrap = true,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .weight(0.15f)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState)
            )
        }
    }
}
