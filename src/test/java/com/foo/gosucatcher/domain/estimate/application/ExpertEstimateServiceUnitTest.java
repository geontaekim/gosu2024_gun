package com.foo.gosucatcher.domain.estimate.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foo.gosucatcher.domain.estimate.application.dto.request.ExpertNormalEstimateCreateRequest;
import com.foo.gosucatcher.domain.estimate.application.dto.response.ExpertAutoEstimatesResponse;
import com.foo.gosucatcher.domain.estimate.application.dto.response.ExpertEstimateResponse;
import com.foo.gosucatcher.domain.estimate.application.dto.response.ExpertEstimatesResponse;
import com.foo.gosucatcher.domain.estimate.application.dto.response.ExpertNormalEstimateResponse;
import com.foo.gosucatcher.domain.estimate.domain.ExpertEstimate;
import com.foo.gosucatcher.domain.estimate.domain.ExpertEstimateRepository;
import com.foo.gosucatcher.domain.estimate.domain.MemberEstimate;
import com.foo.gosucatcher.domain.estimate.domain.MemberEstimateRepository;
import com.foo.gosucatcher.domain.expert.domain.Expert;
import com.foo.gosucatcher.domain.expert.domain.ExpertRepository;
import com.foo.gosucatcher.domain.item.domain.MainItem;
import com.foo.gosucatcher.domain.item.domain.SubItem;
import com.foo.gosucatcher.domain.item.domain.SubItemRepository;
import com.foo.gosucatcher.domain.member.domain.Member;
import com.foo.gosucatcher.global.error.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ExpertEstimateServiceUnitTest {

	@InjectMocks
	private ExpertEstimateService expertEstimateService;

	@Mock
	private ExpertEstimateRepository expertEstimateRepository;

	@Mock
	private ExpertRepository expertRepository;

	@Mock
	private MemberEstimateRepository memberEstimateRepository;

	@Mock
	private SubItemRepository subItemRepository;

	private Expert expert;
	private Member member;
	private MainItem mainItem;
	private SubItem subItem;
	private MemberEstimate memberEstimate;
	private ExpertEstimate expertEstimate;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.name("이홍섭")
			.password("q1w2e3")
			.email("sjun@naver.com")
			.phoneNumber("010")
			.build();

		mainItem = MainItem.builder()
			.name("메인 서비스 이름")
			.description("메인 서비스 설명").
			build();

		subItem = SubItem.builder()
			.mainItem(mainItem)
			.name("세부 서비스 이름")
			.description("세부 서비스 설명")
			.build();

		expert = Expert.builder()
			.member(member)
			.storeName("축구 레슨")
			.location("서울시 강남구")
			.maxTravelDistance(10)
			.description("축구 레슨 해드립니다.")
			.build();

		memberEstimate = MemberEstimate.builder()
			.member(member)
			.subItem(subItem)
			.preferredStartDate(LocalDateTime.now().plusDays(1))
			.detailedDescription("메시가 되고 싶어요")
			.build();

		expertEstimate = ExpertEstimate.builder()
			.totalCost(10000)
			.expert(expert)
			.memberEstimate(memberEstimate)
			.description("메시를 만들어 드립니다")
			.subItem(subItem)
			.build();
	}

	@Test
	@DisplayName("일반 고수 견적서 생성 성공")
	void createExpertEstimateSuccessTest() throws Exception {

		//given
		Long expertId = 1L;
		ExpertNormalEstimateCreateRequest request =
			new ExpertNormalEstimateCreateRequest(100, "서울시 강남구", "메시를 만들어 드립니다.");

		when(expertEstimateRepository.save(any(ExpertEstimate.class)))
			.thenReturn(expertEstimate);
		when(expertRepository.findById(expertId))
			.thenReturn(Optional.of(expert));
		when(memberEstimateRepository.findById(memberEstimate.getId()))
			.thenReturn(Optional.of(memberEstimate));

		//when
		ExpertNormalEstimateResponse expertNormalEstimateResponse = expertEstimateService.createNormal(expertId,
			memberEstimate.getId(), request);

		//then
		assertThat(expertNormalEstimateResponse.totalCost()).isEqualTo(request.totalCost());
		assertThat(expertNormalEstimateResponse.memberEstimateResponse().id()).isEqualTo(memberEstimate.getId());
	}

	@Test
	@DisplayName("고수 견적서 생성 실패 - 존재하지 않는 고수")
	void createExpertEstimateFailTest_notFoundExpert() throws Exception {

		//given
		ExpertNormalEstimateCreateRequest request =
			new ExpertNormalEstimateCreateRequest(100, "서울시 강남구", "메시를 만들어 드립니다.");

		when(expertRepository.findById(anyLong()))
			.thenReturn(Optional.empty());

		//when -> then
		assertThrows(EntityNotFoundException.class,
			() -> expertEstimateService.createNormal(1L, memberEstimate.getId(), request));
	}

	@Test
	@DisplayName("고수 견적서 생성 실패 - 존재하지 않는 고객의 요청서")
	void createExpertEstimateFailTest_notFoundMemberEstimate() throws Exception {

		//given
		ExpertNormalEstimateCreateRequest request =
			new ExpertNormalEstimateCreateRequest(100, "서울시 강남구", "메시를 만들어 드립니다.");

		when(expertRepository.findById(anyLong()))
			.thenReturn(Optional.of(expert));
		when(memberEstimateRepository.findById(null))
			.thenReturn(Optional.empty());

		//when -> then
		assertThrows(EntityNotFoundException.class,
			() -> expertEstimateService.createNormal(1L, memberEstimate.getId(), request));
	}

	@Test
	@DisplayName("고수 견적서 전체 조회 성공")
	void findAllSuccessTest() throws Exception {

		//given
		List<ExpertEstimate> estimates = Arrays.asList(expertEstimate);

		// Mock 객체 설정: findAllWithFetchJoin() 메서드 호출 시 estimates 리스트 반환하도록 설정
		when(expertEstimateRepository.findAllWithFetchJoin())
			.thenReturn(estimates);

		//when
		ExpertEstimatesResponse estimatesResponse = expertEstimateService.findAll();

		//then
		assertThat(estimatesResponse.expertEstimateResponseList()).hasSize(1);
		assertThat(estimatesResponse.expertEstimateResponseList().get(0).totalCost()).isEqualTo(
			expertEstimate.getTotalCost());
	}

	@Test
	@DisplayName("ID로 고수 견적서 조회 성공")
	void findExpertEstimateByIdSuccessTest() throws Exception {

		//given
		Long expertEstimateId = expertEstimate.getId();
		when(expertEstimateRepository.findById(expertEstimateId))
			.thenReturn(Optional.of(expertEstimate));

		//when
		ExpertEstimateResponse estimateResponse = expertEstimateService.findById(expertEstimateId);

		//then
		assertThat(estimateResponse.id()).isEqualTo(expertEstimate.getId());
		assertThat(estimateResponse.totalCost()).isEqualTo(expertEstimate.getTotalCost());
	}

	@Test
	@DisplayName("ID로 고수 견적서 조회 실패 - 존재하지 않는 견적서")
	void findExpertEstimateByIdFialTest_notFoundExpertEstimate() throws Exception {

		//given
		Long expertEstimateId = expertEstimate.getId();
		when(expertEstimateRepository.findById(expertEstimateId))
			.thenReturn(Optional.empty());

		//when -> then
		assertThrows(EntityNotFoundException.class,
			() -> expertEstimateService.findById(expertEstimateId));
	}

	@Test
	@DisplayName("고수 견적서 삭제 성공")
	void deleteExpertEstimateSuccessTest() throws Exception {

		//given
		when(expertEstimateRepository.findById(null))
			.thenReturn(Optional.of(expertEstimate));

		//when
		assertDoesNotThrow(() -> expertEstimateService.delete(expertEstimate.getId()));

		//then
		verify(expertEstimateRepository, times(1)).delete(expertEstimate);
	}

	@Test
	@DisplayName("조건에 맞는 모든 고수 견적서 목록 조회 성공")
	void findAllByConditions() {

		//given
		Long subItemId = 1L;
		String activityLocation = "서울시 강남구";

		List<ExpertEstimate> expertEstimates = IntStream.range(0, 10)
			.mapToObj(i -> ExpertEstimate.builder()
				.totalCost(10000)
				.expert(expert)
				.memberEstimate(memberEstimate)
				.description("메시를 만들어 드립니다")
				.subItem(subItem)
				.build())
			.toList();

		when(expertEstimateRepository.findAllBySubItemIdAndLocation(subItemId, activityLocation))
			.thenReturn(expertEstimates);

		//when
		ExpertAutoEstimatesResponse expertAutoEstimatesResponse = expertEstimateService.findAllByConditions(subItemId, activityLocation);

		//then
		assertThat(expertAutoEstimatesResponse.expertAutoEstimateResponses()).hasSize(10);
		assertThat(expertAutoEstimatesResponse.expertAutoEstimateResponses().get(0).description()).isEqualTo("메시를 만들어 드립니다");
	}

	@Test
	@DisplayName("특정 회원 요청 견적서와 거래된 모든 고수 견적서 목록 조회 성공")
	void findAllByMemberEstimateId() {

		//given
		Long memberEstimateId = 1L;

		List<ExpertEstimate> expertEstimates = Arrays.asList(expertEstimate);

		when(memberEstimateRepository.findById(memberEstimateId))
			.thenReturn(Optional.of(memberEstimate));

		when(expertEstimateRepository.findAllByMemberEstimate(memberEstimate))
			.thenReturn(expertEstimates);

		//when
		ExpertEstimatesResponse expertEstimatesResponse = expertEstimateService.findAllByMemberEstimateId(memberEstimateId);

		//then
		assertThat(expertEstimatesResponse.expertEstimateResponseList()).hasSize(1);
		assertThat(expertEstimatesResponse.expertEstimateResponseList().get(0).totalCost()).isEqualTo(
			expertEstimate.getTotalCost());
	}

	@Test
	@DisplayName("매칭되지 않은 상태인 특정 고수의 견적서 목록 조회 성공")
	void findAllUnmatchedAutoByExpertId() {

		//given
		Long expertId = 1L;

		List<ExpertEstimate> expertEstimates = Arrays.asList(expertEstimate);

		when(expertEstimateRepository.findAllByExpertIdAndMemberEstimateIsNull(expertId))
			.thenReturn(expertEstimates);

		//when
		ExpertAutoEstimatesResponse expertAutoEstimatesResponse = expertEstimateService.findAllUnmatchedAutoByExpertId(expertId);

		//then
		assertThat(expertAutoEstimatesResponse.expertAutoEstimateResponses()).hasSize(1);
		assertThat(expertAutoEstimatesResponse.expertAutoEstimateResponses().get(0).totalCost()).isEqualTo(
			expertEstimate.getTotalCost());
	}
}
