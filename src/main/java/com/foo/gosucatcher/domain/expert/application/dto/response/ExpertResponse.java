package com.foo.gosucatcher.domain.expert.application.dto.response;

import com.foo.gosucatcher.domain.expert.domain.Expert;

public record ExpertResponse(
	Long id,
	String storeName,
	String location,
	int maxTravelDistance,
	String description,
	double rating,
	int reviewCount,
	String filename
) {

	public static ExpertResponse from(Expert expert) {
		return new ExpertResponse(
			expert.getId(),
			expert.getStoreName(),
			expert.getLocation(),
			expert.getMaxTravelDistance(),
			expert.getDescription(),
			expert.getRating(),
			expert.getReviewCount(),
			expert.getMember().getProfileMemberImage() != null
				? expert.getMember().getProfileMemberImage().getFilename() : null
		);
	}
}
