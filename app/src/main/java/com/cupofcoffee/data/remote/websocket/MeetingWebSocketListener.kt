package com.cupofcoffee.data.remote.websocket

import com.cupofcoffee.data.remote.model.MeetingDTO
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MeetingWebSocketListener(
    private val meetingChannel: Channel<List<MeetingDTO>>
) :
    WebSocketListener() {

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        val meetings = Json.decodeFromString<Array<MeetingDTO>>(text)
        meetingChannel.trySend(meetings.toList())
    }
}