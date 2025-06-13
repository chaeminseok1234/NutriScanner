package com.example.nutriscanner.model

data class Message(
    val role: String,
    val content: String
)

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int
)

data class Choice(
    val message: Message
)

data class ChatResponse(
    val choices: List<Choice>
)
