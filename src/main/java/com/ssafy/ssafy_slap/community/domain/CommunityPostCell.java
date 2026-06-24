package com.ssafy.ssafy_slap.community.domain;

public class CommunityPostCell {

    private Long postCellId;
    private Long postId;
    private int sortOrder;
    private String cellType;
    private String textContent;
    private String imageUrl;

    public CommunityPostCell() {
    }

    public CommunityPostCell(Long postCellId, Long postId, int sortOrder, String cellType, String textContent, String imageUrl) {
        this.postCellId = postCellId;
        this.postId = postId;
        this.sortOrder = sortOrder;
        this.cellType = cellType;
        this.textContent = textContent;
        this.imageUrl = imageUrl;
    }

    public Long getPostCellId() { return postCellId; }
    public void setPostCellId(Long postCellId) { this.postCellId = postCellId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public String getCellType() { return cellType; }
    public void setCellType(String cellType) { this.cellType = cellType; }
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
