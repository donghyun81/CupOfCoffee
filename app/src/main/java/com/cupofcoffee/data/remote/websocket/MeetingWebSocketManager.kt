package com.cupofcoffee.data.remote.websocket

import android.util.Log
import com.cupofcoffee.BuildConfig
import com.cupofcoffee.data.remote.model.MeetingDTO
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

private const val BASE_URL = BuildConfig.BASE_URL
private const val MEETING_PATH = "meetings"

class MeetingWebSocketManager(private val meetingChannel: Channel<List<MeetingDTO>>) {

    private val client = OkHttpClient()
    private lateinit var webSocket: WebSocket

    fun connect() {
        val request = Request.Builder().url(BASE_URL + MEETING_PATH).build()
        val listener = MeetingWebSocketListener(meetingChannel)
        webSocket = client.newWebSocket(request, listener)
    }

    fun send(message: String) {
        webSocket.send(message)
    }

    fun close() {
        webSocket.close(1000, "Goodbye!")
    }
}