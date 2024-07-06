package com.cupofcoffee.data.remote.websocket

import android.util.Log
import com.cupofcoffee.data.remote.model.MeetingDTO
import com.cupofcoffee.data.remote.model.PlaceDTO
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class PlaceWebSocketListener(
    private val placeChannel: Channel<Map<String, PlaceDTO>>
) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.d("12345", response.toString())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        val places = Json.decodeFromString<Map<String, PlaceDTO>>(text)
        Log.d("12345", places.toString())
        placeChannel.trySend(places)
    }
}