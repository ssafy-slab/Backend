package com.ssafy.ssafy_slap.vote.service;

public interface VoteCloseProcessor {

    void beforeClose(Long tripId, Long voteId, Long userId);
}
