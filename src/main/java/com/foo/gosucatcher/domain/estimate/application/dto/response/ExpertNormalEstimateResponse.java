package com.foo.gosucatcher.domain.estimate.application.dto.response;

import com.foo.gosucatcher.domain.estimate.domain.ExpertEstimate;
import com.foo.gosucatcher.domain.expert.application.dto.response.ExpertResponse;

public record ExpertNormalEstimateResponse(
	Long id,
	ExpertResponse expertResponse,
	MemberEstimateResponse memberEstimateResponse,
	int totalCost,
	String activityLocation,
	String description
) {

	public static ExpertNormalEstimateResponse from(ExpertEstimate expertEstimate) {
		return new ExpertNormalEstimateResponse(
			expertEstimate.getId(),
			ExpertResponse.from(expertEstimate.getExpert()),
			MemberEstimateResponse.from(expertEstimate.getMemberEstimate()),
			expertEstimate.getTotalCost(),
			expertEstimate.getActivityLocation(),
			expertEstimate.getDescription());
	}
}
