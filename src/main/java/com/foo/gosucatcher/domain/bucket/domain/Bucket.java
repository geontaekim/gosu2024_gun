package com.foo.gosucatcher.domain.bucket.domain;

import static com.foo.gosucatcher.global.error.ErrorCode.UNSUPPORTED_SELF_BUCKET;
import static java.lang.Boolean.FALSE;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.foo.gosucatcher.domain.bucket.exception.UnsupportedBucketException;
import com.foo.gosucatcher.domain.expert.domain.Expert;
import com.foo.gosucatcher.domain.member.domain.Member;
import com.foo.gosucatcher.global.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "buckets")
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE buckets SET is_deleted = true WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bucket extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "expert_id")
	private Expert expert;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private boolean isDeleted = FALSE;

	@Builder
	public Bucket(Expert expert, Member member) {
		long expertMemberId = expert.getMember().getId();
		long memberId = member.getId();

		if (memberId == expertMemberId) {
			throw new UnsupportedBucketException(UNSUPPORTED_SELF_BUCKET);
		}

		this.expert = expert;
		this.member = member;
	}
}
