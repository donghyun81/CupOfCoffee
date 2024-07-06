package com.cupofcoffee.data.remote.websocket

import com.cupofcoffee.BuildConfig
import com.cupofcoffee.data.remote.model.PlaceDTO
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

private const val BASE_URL = BuildConfig.BASE_WS_URL
private const val PLACE_PATH = "places"

class PlaceWebSocketManager(private val placeChannel: Channel<Map<String, PlaceDTO>>) {

    private val client = OkHttpClient()
    private lateinit var webSocket: WebSocket

    fun connect() {
        val request = Request.Builder().url(BASE_URL + PLACE_PATH).build()
        val listener = PlaceWebSocketListener(placeChannel)
        webSocket = client.newWebSocket(request, listener)
    }

    fun send(message: String) {
        webSocket.send(message)
    }

    fun close() {
        webSocket.close(1000, "Goodbye!")
    }
}