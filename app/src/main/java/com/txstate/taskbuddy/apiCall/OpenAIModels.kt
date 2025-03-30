package com.txstate.taskbuddy.apiCall

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val role: String,  // "system" | "user" | "assistant"
    val content: String
)
@Serializable
data class OpenAIRequest(
    val model: String, // Example: "gpt-4o"
    val messages: List<Message>,
    val max_tokens: Int,
    val temperature: Double
)

data class OpenAIResponse(
    val choices: List<Choice> // ✅ Corrected OpenAI Response Structure
)

data class Choice(
    val message: Message // ✅ OpenAI returns a `message` object, not `text`
)
