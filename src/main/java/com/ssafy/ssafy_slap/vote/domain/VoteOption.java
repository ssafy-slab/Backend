package com.ssafy.ssafy_slap.vote.domain;

public class VoteOption {

    private Long voteOptionId;
    private Long voteId;
    private Long placeId;
    private String optionTitle;
    private String description;
    private Integer sortOrder;
    private Long voteCount;

    public VoteOption() {
    }

    public VoteOption(Long voteOptionId, Long voteId, Long placeId, String optionTitle,
                      String description, Integer sortOrder, Long voteCount) {
        this.voteOptionId = voteOptionId;
        this.voteId = voteId;
        this.placeId = placeId;
        this.optionTitle = optionTitle;
        this.description = description;
        this.sortOrder = sortOrder;
        this.voteCount = voteCount;
    }

    public Long getVoteOptionId() { return voteOptionId; }
    public void setVoteOptionId(Long voteOptionId) { this.voteOptionId = voteOptionId; }
    public Long getVoteId() { return voteId; }
    public Long getPlaceId() { return placeId; }
    public String getOptionTitle() { return optionTitle; }
    public String getDescription() { return description; }
    public Integer getSortOrder() { return sortOrder; }
    public Long getVoteCount() { return voteCount; }
}
