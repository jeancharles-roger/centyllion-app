package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Action
import com.centyllion.model.Event
import kotlin.properties.Delegates.observable

class EventController(event: Event, size: ColumnSize) : NoContextController<Event, Column>() {

    override var data by observable(event) { _, old, new ->
        if (old != new) refresh()
    }

    val title = span("${data.action} on ${data.collection}")
    val date = span(data.date)
    val level = Level(left = listOf(title), right = listOf(date))

    val body = Message(
        body = listOf(level) + data.arguments.map { span(it) },
        color = color()
    )

    override val container = Column(body, size = size)

    fun color() = when (data.action) {
        Action.Create -> ElementColor.Primary
        Action.Save -> ElementColor.Info
        Action.Delete -> ElementColor.Warning
        Action.Error -> ElementColor.Danger
    }

    override fun refresh() {
        title.text = "${data.action} on ${data.collection}"
        date.text = data.date
        body.body = listOf(level) + data.arguments.map { span(it) }
    }
}
