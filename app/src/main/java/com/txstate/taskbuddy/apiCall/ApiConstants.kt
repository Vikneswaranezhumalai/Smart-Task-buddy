package com.txstate.taskbuddy.apiCall

import com.txstate.taskbuddy.BuildConfig

object ApiConstants {
    const val OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY // 🔹 Replace with actual API key
    const val BASE_URL = "https://api.openai.com/"
    object OpenAIModels {
        // **GPT-4o (Latest & Most Capable)**
        const val GPT_4O = "gpt-4o"
        // **Pricing:** $0.005 per 1K input tokens, $0.015 per 1K output tokens

        // **GPT-4 Turbo (Older version of GPT-4)**
        const val GPT_4_TURBO = "gpt-4-turbo"
        // **Pricing:** $0.01 per 1K input tokens, $0.03 per 1K output tokens

        // **GPT-3.5 Turbo (Cost-effective, lightweight)**
        const val GPT_3_5_TURBO = "gpt-3.5-turbo"
        // **Pricing:** $0.0005 per 1K input tokens, $0.0015 per 1K output tokens

        // **DALL-E 3 (Text-to-Image Generation)**
        const val DALL_E_3 = "dall-e-3"
        // **Pricing:** Varies based on resolution and features (approximately $0.04 per image)

        // **Whisper (Speech-to-Text)**
        const val WHISPER = "whisper-1"
        // **Pricing:** $0.006 per minute of audio

        // **Moderation Model (Content Safety)**
        const val MODERATION_LATEST = "text-moderation-latest"
        // **Pricing:** Free for safety moderation use

        // **Retrieval-Augmented Generation (RAG) Models (Coming Soon)**
        // These models will allow retrieving information from documents before generating answers.
    }


}
