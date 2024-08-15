package com.foo.gosucatcher.domain.bucket.dto.response;

import com.foo.gosucatcher.domain.bucket.domain.Bucket;

public record BucketResponse(
		Long id,
		Long expertId,
		Long memberId
) {

	public static BucketResponse from(Bucket bucket) {
		return new BucketResponse(bucket.getId(), bucket.getExpert().getId(), bucket.getMember().getId());
	}
}
