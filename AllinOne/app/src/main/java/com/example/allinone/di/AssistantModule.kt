package com.example.allinone.di

import android.content.Context
import com.example.allinone.AssistantBrain
import com.example.allinone.VoiceAssistantHandler
import com.example.allinone.assistant.context.AssistantContextManager
import com.example.allinone.assistant.executor.AssistantSessionProcessor
import com.example.allinone.assistant.intent.AssistantIntentDetector
import com.example.allinone.assistant.parser.AssistantEntityExtractor
import com.example.allinone.assistant.response.AssistantResponseProvider
import com.example.allinone.core.utils.IntelligenceEngine
import com.example.allinone.data.database.AiChatDao
import com.example.allinone.data.database.AssistantMemoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AssistantModule {

    @Provides
    @Singleton
    fun provideAssistantContextManager(): AssistantContextManager = AssistantContextManager()

    @Provides
    @Singleton
    fun provideAssistantResponseProvider(): AssistantResponseProvider = AssistantResponseProvider()

    @Provides
    @Singleton
    fun provideAssistantEntityExtractor(
        contextManager: AssistantContextManager
    ): AssistantEntityExtractor = AssistantEntityExtractor(contextManager)

    @Provides
    @Singleton
    fun provideAssistantIntentDetector(
        extractor: AssistantEntityExtractor,
        contextManager: AssistantContextManager
    ): AssistantIntentDetector = AssistantIntentDetector(extractor, contextManager)

    @Provides
    @Singleton
    fun provideAssistantSessionProcessor(
        contextManager: AssistantContextManager
    ): AssistantSessionProcessor = AssistantSessionProcessor(contextManager)

    @Provides
    @Singleton
    fun provideAssistantBrain(
        contextManager: AssistantContextManager,
        intentDetector: AssistantIntentDetector,
        sessionProcessor: AssistantSessionProcessor,
        responseProvider: AssistantResponseProvider
    ): AssistantBrain {
        return AssistantBrain(contextManager, intentDetector, sessionProcessor, responseProvider)
    }

    @Provides
    @Singleton
    fun provideVoiceAssistantHandler(
        @ApplicationContext context: Context
    ): VoiceAssistantHandler = VoiceAssistantHandler(
        context = context,
        onResults = {},
        onListeningStateChanged = {},
        onError = {}
    )
}
