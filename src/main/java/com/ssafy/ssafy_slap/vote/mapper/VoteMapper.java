package com.ssafy.ssafy_slap.vote.mapper;

import com.ssafy.ssafy_slap.vote.domain.Vote;
import com.ssafy.ssafy_slap.vote.domain.VoteOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VoteMapper {

    boolean existsAccessibleTrip(@Param("tripId") Long tripId, @Param("userId") Long userId);
    boolean existsEditableTrip(@Param("tripId") Long tripId, @Param("userId") Long userId);
    void insertVote(@Param("vote") Vote vote);
    void insertOption(@Param("option") VoteOption option);
    Vote findVote(@Param("tripId") Long tripId, @Param("voteId") Long voteId);
    List<Vote> findVotesByTripId(@Param("tripId") Long tripId);
    List<VoteOption> findOptionsWithCounts(@Param("voteId") Long voteId);
    Long findSelectedOptionId(@Param("voteId") Long voteId, @Param("userId") Long userId);
    boolean existsOption(@Param("voteId") Long voteId, @Param("voteOptionId") Long voteOptionId);
    void upsertBallot(@Param("voteId") Long voteId, @Param("voteOptionId") Long voteOptionId,
                      @Param("userId") Long userId);
    int closeVote(@Param("tripId") Long tripId, @Param("voteId") Long voteId);
}
