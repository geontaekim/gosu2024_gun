package com.foo.gosucatcher.domain.estimate.application.dto.response;

import java.util.List;

import com.foo.gosucatcher.domain.estimate.domain.ExpertEstimate;

public record ExpertAutoEstimatesResponse(
	List<ExpertAutoEstimateResponse> expertAutoEstimateResponses
) {

	public static ExpertAutoEstimatesResponse from(List<ExpertEstimate> expertEstimates) {
		return new ExpertAutoEstimatesResponse(expertEstimates.stream()
			.map(ExpertAutoEstimateResponse::from)
			.toList());
	}
}
