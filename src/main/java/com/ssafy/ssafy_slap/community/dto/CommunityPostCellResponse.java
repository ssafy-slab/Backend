package com.ssafy.ssafy_slap.community.dto;

import com.ssafy.ssafy_slap.community.domain.CommunityPostCell;

public record CommunityPostCellResponse(
        Long postCellId,
        int sortOrder,
        String cellType,
        String textContent,
        String imageUrl,
        String alignment,
        Integer fontSizePx,
        Boolean bold
) {
    public static CommunityPostCellResponse from(CommunityPostCell cell) {
        return new CommunityPostCellResponse(
                cell.getPostCellId(),
                cell.getSortOrder(),
                cell.getCellType(),
                cell.getTextContent(),
                cell.getImageUrl(),
                cell.getAlignment(),
                normalizedFontSizePx(cell),
                normalizedBold(cell)
        );
    }

    private static Integer normalizedFontSizePx(CommunityPostCell cell) {
        if (cell.getFontSizePx() != null) {
            return cell.getFontSizePx();
        }
        return "IMAGE".equals(cell.getCellType()) ? 0 : 14;
    }

    private static Boolean normalizedBold(CommunityPostCell cell) {
        return cell.getBold() != null ? cell.getBold() : false;
    }
}
