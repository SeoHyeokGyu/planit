package com.planit.enums

enum class FeedSortType {
    LATEST,      // 최신순 (createdAt DESC)
    LIKES,       // 좋아요순 (likeCount DESC)
    COMMENTS,    // 댓글순 (commentCount DESC)
    POPULAR      // 인기순 (likeCount + commentCount DESC)
}