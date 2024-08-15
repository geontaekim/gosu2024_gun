package com.foo.gosucatcher.domain.estimate.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foo.gosucatcher.domain.chat.application.dto.response.ChattingRoomResponse;
import com.foo.gosucatcher.domain.chat.application.dto.response.MessageResponse;
import com.foo.gosucatcher.domain.estimate.application.ExpertEstimateService;
import com.foo.gosucatcher.domain.estimate.application.dto.request.ExpertAutoEstimateCreateRequest;
import com.foo.gosucatcher.domain.estimate.application.dto.request.ExpertNormalEstimateCreateRequest;
import com.foo.gosucatcher.domain.estimate.application.dto.request.MemberEstimateRequest;
import com.foo.gosucatcher.domain.estimate.application.dto.response.ExpertAutoEstimateResponse;
import com.foo.gosucatcher.domain.estimate.application.dto.response.ExpertAutoEstimatesResponse;
import com.foo.gosucatcher.domain.estimate.application.dto.response.ExpertEstimateResponse;
import com.foo.gosucatcher.domain.estimate.application.dto.response.ExpertEstimatesResponse;
import com.foo.gosucatcher.domain.estimate.application.dto.response.ExpertNormalEstimateResponse;
import com.foo.gosucatcher.domain.estimate.application.dto.response.MemberEstimateResponse;
import com.foo.gosucatcher.domain.estimate.domain.Status;
import com.foo.gosucatcher.domain.expert.application.dto.response.ExpertResponse;
import com.foo.gosucatcher.domain.item.application.dto.response.sub.SubItemResponse;
import com.foo.gosucatcher.domain.item.domain.MainItem;
import com.foo.gosucatcher.domain.item.domain.SubItem;
import com.foo.gosucatcher.domain.matching.application.MatchingService;
import com.foo.gosucatcher.global.error.ErrorCode;
import com.foo.gosucatcher.global.error.exception.EntityNotFoundException;

@WebMvcTest(value = {ExpertEstimateController.class}, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class ExpertEstimateControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	ExpertEstimateService expertEstimateService;

	@MockBean
	private MatchingService matchingService;

	private ExpertNormalEstimateCreateRequest expertNormalEstimateCreateRequest;
	private ExpertResponse expertResponse;
	private MemberEstimateRequest memberEstimateRequest;
	private MemberEstimateResponse memberEstimateResponse;
	private SubItemResponse subItemResponse;
	private MainItem mainItem;
	private SubItem subItem;
	private String baseUrl = "/api/v1/expert-estimates";

	@BeforeEach
	void setUp() {
		expertNormalEstimateCreateRequest =
			new ExpertNormalEstimateCreateRequest(100, "서울시 강남구", "상세설명을씁니다");
		expertResponse =
			new ExpertResponse(1L, "상점이름입니다", "서울시 강남구", 10, "설명입니다여긴", 4.0, 10,null);

		memberEstimateRequest = new MemberEstimateRequest(1L,
			"서울 강남구 개포1동", LocalDateTime.now().plusDays(3), "추가 내용");

		mainItem = MainItem.builder().name("메인 서비스 이름").description("메인 서비스 설명").build();

		subItem = SubItem.builder().mainItem(mainItem).name("세부 서비스 이름").description("세부 서비스 설명").build();

		subItemResponse = new SubItemResponse(1L, subItem.getMainItem().getName(), subItem.getName(), subItem.getDescription());

		memberEstimateResponse = new MemberEstimateResponse(1L, 1L,
			1L, subItemResponse, "서울 강남구 개포1동", LocalDateTime.now().plusDays(4), "추가 내용", Status.PROCEEDING);
	}

	@Test
	@DisplayName("고수 일반 견적서 등록 성공")
	void createExpertEstimateSuccessTest() throws Exception {

		//given
		Long expertId = 1L;

		ChattingRoomResponse chattingRoomResponse = new ChattingRoomResponse(1L, memberEstimateResponse);
		MessageResponse messageResponse = new MessageResponse(1L, expertResponse.id(), chattingRoomResponse, "고수 견적서 내용입니다.");

		ExpertNormalEstimateResponse expertNormalEstimateResponse = new ExpertNormalEstimateResponse(1L, expertResponse, memberEstimateResponse,
			100, "서울시 강남구", "상세설명을씁니다");
		given(expertEstimateService.createNormal(anyLong(), anyLong(), any()))
			.willReturn(expertNormalEstimateResponse);
		given(matchingService.sendFirstMessageForNormal(anyLong(), any())).willReturn(messageResponse);

		//when -> then
		mockMvc.perform(post(baseUrl + "/normal?memberEstimateId={memberEstimateId}", 1L)
				.param("expertId", String.valueOf(expertId))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expertNormalEstimateCreateRequest)))
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.senderId").value(1))
			.andExpect(jsonPath("$.chattingRoomResponse.id").value(1))
			.andExpect(jsonPath("$.chattingRoomResponse.memberEstimateResponse.id").value(1))
			.andExpect(jsonPath("$.chattingRoomResponse.memberEstimateResponse.memberId").value(1))
			.andExpect(jsonPath("$.chattingRoomResponse.memberEstimateResponse.expertId").value(1))
			.andExpect(jsonPath("$.chattingRoomResponse.memberEstimateResponse.subItemResponse.id").value(1))
			.andExpect(jsonPath("$.chattingRoomResponse.memberEstimateResponse.subItemResponse.mainItemName").value(subItemResponse.mainItemName()))
			.andExpect(jsonPath("$.chattingRoomResponse.memberEstimateResponse.subItemResponse.name").value(subItemResponse.name()))
			.andExpect(jsonPath("$.chattingRoomResponse.memberEstimateResponse.subItemResponse.description").value(subItemResponse.description()))
			.andExpect(jsonPath("$.chattingRoomResponse.memberEstimateResponse.location").value("서울 강남구 개포1동"))
			.andExpect(jsonPath("$.chattingRoomResponse.memberEstimateResponse.detailedDescription").value("추가 내용"))
			.andExpect(jsonPath("$.content").value("고수 견적서 내용입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 일반 견적서 등록 실패 - 존재하지 않는 고수")
	void createExpertEstimateFailTest_notFoundExpert() throws Exception {

		//given
		Long expertId = 1L;

		given(expertEstimateService.createNormal(anyLong(), anyLong(), any()))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_EXPERT));

		//when -> then
		mockMvc.perform(post(baseUrl + "/normal?memberEstimateId={memberEstimateId}", 1L)
				.param("expertId", String.valueOf(expertId))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expertNormalEstimateCreateRequest)))
			.andExpect(status().isNotFound())
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("E001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 고수입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 일반 견적서 등록 실패 - 존재하지 않는 고객 요청 견적서")
	void createExpertEstimateFailTest_notFoundMemberEstimate() throws Exception {

		//given
		Long expertId = 1L;

		given(expertEstimateService.createNormal(anyLong(), anyLong(), any()))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER_ESTIMATE));

		//when -> then
		mockMvc.perform(post(baseUrl + "/normal?memberEstimateId={memberEstimateId}", 1L)
				.param("expertId", String.valueOf(expertId))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expertNormalEstimateCreateRequest)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 회원 요청 견적서입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 일반 견적서 등록 실패 - 잘못된 값 입력")
	void createExpertEstimateFailTest_invalidValue() throws Exception {

		//given
		Long expertId = 1L;

		expertNormalEstimateCreateRequest =
			new ExpertNormalEstimateCreateRequest(100, "서울시 강남구", "짧은 설명");

		//when -> then
		mockMvc.perform(post(baseUrl + "/normal?memberEstimateId={memberEstimateId}", 1L)
				.param("expertId", String.valueOf(expertId))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expertNormalEstimateCreateRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("C001"))
			.andExpect(jsonPath("$.errors[0].value").value("짧은 설명"))
			.andExpect(jsonPath("$.errors[0].reason").value("견적서에 대한 설명은 6자 이상 적어주세요."))
			.andExpect(jsonPath("$.message").value("잘못된 값을 입력하셨습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 바로 견적서 등록 성공")
	public void createAuto() throws Exception {
		//given
		Long expertId = 1L;

		ExpertAutoEstimateCreateRequest expertAutoEstimateCreateRequest = new ExpertAutoEstimateCreateRequest(1L, 10000, "서울시 강남구", "설명을 작성합니다");

		ExpertResponse expertResponse = new ExpertResponse(1L, "업체명", "강남구", 5, "설명을 작성합니다", 4.0, 5,null);

		ExpertAutoEstimateResponse expertAutoEstimateResponse = new ExpertAutoEstimateResponse(1L, expertResponse, 1L, 10000, "서울시 강남구", "설명을 작성합니다");

		given(expertEstimateService.createAuto(anyLong(), any()))
			.willReturn(expertAutoEstimateResponse);

		//when -> then
		mockMvc.perform(post(baseUrl + "/auto")
				.param("expertId", String.valueOf(expertId))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expertAutoEstimateCreateRequest)))
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.expert.id").value(1))
			.andExpect(jsonPath("$.expert.storeName").value("업체명"))
			.andExpect(jsonPath("$.expert.location").value("강남구"))
			.andExpect(jsonPath("$.expert.maxTravelDistance").value(5))
			.andExpect(jsonPath("$.expert.description").value("설명을 작성합니다"))
			.andExpect(jsonPath("$.subItemId").value(1))
			.andExpect(jsonPath("$.totalCost").value(10000))
			.andExpect(jsonPath("$.activityLocation").value("서울시 강남구"))
			.andExpect(jsonPath("$.description").value("설명을 작성합니다"))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 바로 견적서 등록 실패")
	public void createAutoFailed() throws Exception {
		//given
		Long expertId = 1L;

		ExpertAutoEstimateCreateRequest expertAutoEstimateCreateRequest = new ExpertAutoEstimateCreateRequest(null, 10000, "서울시 강남구", "설명을 작성합니다");

		ExpertResponse expertResponse = new ExpertResponse(1L, "업체명", "강남구", 5, "설명을 작성합니다", 4.0, 5,null);

		ExpertAutoEstimateResponse expertAutoEstimateResponse = new ExpertAutoEstimateResponse(1L, expertResponse, null, 10000, "서울시 강남구", "설명을 작성합니다");

		given(expertEstimateService.createAuto(anyLong(), any()))
			.willReturn(expertAutoEstimateResponse);

		//when -> then
		mockMvc.perform(post(baseUrl + "/auto")
				.param("expertId", String.valueOf(expertId))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expertAutoEstimateCreateRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("C001"))
			.andExpect(jsonPath("$.errors[0].field").value("subItemId"))
			.andExpect(jsonPath("$.errors[0].value").value(""))
			.andExpect(jsonPath("$.errors[0].reason").value("제공할 서비스 ID를 입력해주세요."))
			.andExpect(jsonPath("$.message").value("잘못된 값을 입력하셨습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 응답 견적서 전체 조회")
	void findAllSuccessTest() throws Exception {

		//given
		ExpertEstimatesResponse estimatesResponse = new ExpertEstimatesResponse(
			List.of(new ExpertEstimateResponse(1L, expertResponse, memberEstimateResponse, 100, "서울시 강남구", "설명을 적어보세요")
			));
		given(expertEstimateService.findAll()).willReturn(estimatesResponse);

		//when -> then
		mockMvc.perform(get(baseUrl))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.expertEstimateResponseList[0].id").value(1))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.id").value(1))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.storeName").value(expertResponse.storeName()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.location").value(expertResponse.location()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.maxTravelDistance").value(expertResponse.maxTravelDistance()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.description").value(expertResponse.description()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.rating").value(expertResponse.rating()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.reviewCount").value(expertResponse.reviewCount()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.id").value(memberEstimateResponse.id()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.memberId").value(memberEstimateResponse.memberId()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.expertId").value(memberEstimateResponse.expertId()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.subItemResponse.mainItemName").value(memberEstimateResponse.subItemResponse().mainItemName()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.subItemResponse.name").value(memberEstimateResponse.subItemResponse().name()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.subItemResponse.description").value(memberEstimateResponse.subItemResponse().description()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.location").value("서울 강남구 개포1동"))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.detailedDescription").value("추가 내용"))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.status").value("PROCEEDING"))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].totalCost").value(100))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].activityLocation").value("서울시 강남구"))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].description").value("설명을 적어보세요"))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 응답 견적서 ID로 조회 성공")
	void findExpertEstimateByIdSuccessTest() throws Exception {

		//given
		ExpertEstimateResponse expertNormalEstimateResponse = new ExpertEstimateResponse(1L, expertResponse, memberEstimateResponse, 100, "서울시 강남구", "설명을 적어보세요");
		given(expertEstimateService.findById(anyLong())).willReturn(expertNormalEstimateResponse);

		//when -> then
		mockMvc.perform(get(baseUrl + "/{id}", 1L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.expert.id").value(1))
			.andExpect(jsonPath("$.expert.storeName").value(expertResponse.storeName()))
			.andExpect(jsonPath("$.expert.location").value(expertResponse.location()))
			.andExpect(jsonPath("$.expert.maxTravelDistance").value(expertResponse.maxTravelDistance()))
			.andExpect(jsonPath("$.expert.description").value(expertResponse.description()))
			.andExpect(jsonPath("$.expert.rating").value(expertResponse.rating()))
			.andExpect(jsonPath("$.expert.reviewCount").value(expertResponse.reviewCount()))
			.andExpect(jsonPath("$.memberEstimate.id").value(1))
			.andExpect(jsonPath("$.memberEstimate.memberId").value(1))
			.andExpect(jsonPath("$.memberEstimate.subItemResponse.id").value(1))
			.andExpect(jsonPath("$.memberEstimate.subItemResponse.mainItemName").value(subItemResponse.mainItemName()))
			.andExpect(jsonPath("$.memberEstimate.subItemResponse.name").value(subItemResponse.name()))
			.andExpect(jsonPath("$.memberEstimate.subItemResponse.description").value(subItemResponse.description()))
			.andExpect(jsonPath("$.memberEstimate.location").value(memberEstimateResponse.location()))
			.andExpect(jsonPath("$.memberEstimate.detailedDescription").value(memberEstimateResponse.detailedDescription()))
			.andExpect(jsonPath("$.totalCost").value(100))
			.andExpect(jsonPath("$.description").value("설명을 적어보세요"))
			.andExpect(jsonPath("$.activityLocation").value("서울시 강남구"))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 응답 견적서 ID로 조회 실패 - 존재하지 않는 고수 응답 견적서")
	void findExpertEstimateByIdFailTest_notFoundExpertEstimate() throws Exception {

		//given
		given(expertEstimateService.findById(anyLong()))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_EXPERT_ESTIMATE));

		//when -> then
		mockMvc.perform(get(baseUrl + "/{id}", 1L))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("EE001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 고수가 응답한 견적서 입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 응답 견적서 삭제 성공 테스트")
	void delete() throws Exception {
		//given
		Long expertEstimateId = 1L;

		doNothing().when(expertEstimateService).delete(expertEstimateId);

		//when -> then
		mockMvc.perform(MockMvcRequestBuilders.delete(baseUrl + "/{id}", expertEstimateId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@DisplayName("고수 응답 견적서 삭제 실패 테스트")
	void deleteFailed() throws Exception {
		//given
		Long expertEstimateId = 1L;

		doThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_EXPERT_ESTIMATE)).when(expertEstimateService).delete(any(Long.class));

		//when -> then
		mockMvc.perform(MockMvcRequestBuilders.delete(baseUrl + "/{id}", expertEstimateId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("EE001"))
			.andExpect(jsonPath("$.errors").isArray())
			.andExpect(jsonPath("$.message").value("존재하지 않는 고수가 응답한 견적서 입니다."));
	}

	@Test
	@DisplayName("특정 회원 요청 견적서와 거래된 모든 고수 견적서 목록 조회 성공 테스트")
	void findAllByMemberEstimateId() throws Exception {
		//given
		Long memberEstimateId = 1L;

		ExpertEstimatesResponse estimatesResponse = new ExpertEstimatesResponse(
			List.of(new ExpertEstimateResponse(1L, expertResponse, memberEstimateResponse, 100, "서울시 강남구", "설명을 적어보세요")
			));

		given(expertEstimateService.findAllByMemberEstimateId(memberEstimateId)).willReturn(estimatesResponse);

		//when -> then
		mockMvc.perform(get(baseUrl + "/member-estimates/{memberEstimateId}", memberEstimateId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.expertEstimateResponseList[0].id").value(1))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.id").value(1))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.storeName").value(expertResponse.storeName()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.location").value(expertResponse.location()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.maxTravelDistance").value(expertResponse.maxTravelDistance()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.description").value(expertResponse.description()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.rating").value(expertResponse.rating()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].expert.reviewCount").value(expertResponse.reviewCount()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.id").value(memberEstimateResponse.id()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.memberId").value(memberEstimateResponse.memberId()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.expertId").value(memberEstimateResponse.expertId()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.subItemResponse.mainItemName").value(memberEstimateResponse.subItemResponse().mainItemName()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.subItemResponse.name").value(memberEstimateResponse.subItemResponse().name()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.subItemResponse.description").value(memberEstimateResponse.subItemResponse().description()))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.location").value("서울 강남구 개포1동"))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.detailedDescription").value("추가 내용"))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].memberEstimate.status").value("PROCEEDING"))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].totalCost").value(100))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].activityLocation").value("서울시 강남구"))
			.andExpect(jsonPath("$.expertEstimateResponseList[0].description").value("설명을 적어보세요"))
			.andDo(print());
	}

	@Test
	@DisplayName("매칭되지 않은 상태인 특정 고수의 견적서 목록 조회 성공 테스트")
	void findAllUnmatchedAutoByExpertId() throws Exception {
		//given
		Long expertId = 1L;

		ExpertAutoEstimatesResponse expertAutoEstimatesResponse = new ExpertAutoEstimatesResponse(
			List.of(new ExpertAutoEstimateResponse(1L, expertResponse, 1L, 100, "서울시 강남구", "설명을 적어보세요"))
		);

		given(expertEstimateService.findAllUnmatchedAutoByExpertId(expertId)).willReturn(expertAutoEstimatesResponse);

		//when -> then
		mockMvc.perform(get(baseUrl + "/auto")
				.param("expertId", String.valueOf(expertId))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].id").value(1))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].expert.id").value(1))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].expert.storeName").value(expertResponse.storeName()))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].expert.location").value(expertResponse.location()))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].expert.maxTravelDistance").value(expertResponse.maxTravelDistance()))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].expert.description").value(expertResponse.description()))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].expert.rating").value(expertResponse.rating()))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].expert.reviewCount").value(expertResponse.reviewCount()))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].subItemId").value(1))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].totalCost").value(100))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].activityLocation").value("서울시 강남구"))
			.andExpect(jsonPath("$.expertAutoEstimateResponses[0].description").value("설명을 적어보세요"))
			.andDo(print());
	}
}
