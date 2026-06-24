package com.ssafy.ssafy_slap.ai.service;

public interface AiAnalysisNotifier {
    void completed(Long tripId, Long analysisRunId);

    void noResult(
            Long tripId,
            Long analysisRunId,
            String reasonCode,
            String message
    );
}
