package com.ssafy.ssafy_slap.community.dto;

import com.ssafy.ssafy_slap.community.domain.CommunityPostCell;

public record CommunityPostCellResponse(
        Long postCellId,
        int sortOrder,
        String cellType,
        String textContent,
        String imageUrl,
        String alignment
) {
    public static CommunityPostCellResponse from(CommunityPostCell cell) {
        return new CommunityPostCellResponse(
                cell.getPostCellId(),
                cell.getSortOrder(),
                cell.getCellType(),
                cell.getTextContent(),
                cell.getImageUrl(),
                cell.getAlignment()
        );
    }
}
