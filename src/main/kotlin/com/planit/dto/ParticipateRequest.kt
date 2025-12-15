package com.planit.dto

data class ParticipateRequest(
    var loginId: String  // 헤더에서 받을 수도 있지만, Body로도 받을 수 있게 유지
)