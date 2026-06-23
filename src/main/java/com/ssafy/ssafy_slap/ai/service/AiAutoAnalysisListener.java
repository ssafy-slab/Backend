package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.chat.service.ChatMessageCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AiAutoAnalysisListener {

    private static final Logger log = LoggerFactory.getLogger(AiAutoAnalysisListener.class);

    private final AiAnalysisService aiAnalysisService;

    public AiAutoAnalysisListener(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMessageCreated(ChatMessageCreatedEvent event) {
        try {
            if (aiAnalysisService.shouldAutoAnalyze(event.tripId())) {
                aiAnalysisService.analyzeAuto(event.tripId(), event.userId());
            }
        } catch (RuntimeException exception) {
            log.warn(
                    "Automatic AI analysis skipped or failed. tripId={}, messageId={}",
                    event.tripId(),
                    event.messageId(),
                    exception
            );
        }
    }
}
