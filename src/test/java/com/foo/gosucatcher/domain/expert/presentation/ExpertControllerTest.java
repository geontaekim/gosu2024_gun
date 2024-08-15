package com.foo.gosucatcher.domain.expert.presentation;

import static com.foo.gosucatcher.domain.member.domain.Roles.ROLE_USER;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foo.gosucatcher.domain.expert.application.ExpertService;
import com.foo.gosucatcher.domain.expert.application.dto.request.ExpertCreateRequest;
import com.foo.gosucatcher.domain.expert.application.dto.request.ExpertSubItemRequest;
import com.foo.gosucatcher.domain.expert.application.dto.request.ExpertUpdateRequest;
import com.foo.gosucatcher.domain.expert.application.dto.response.ExpertResponse;
import com.foo.gosucatcher.domain.expert.application.dto.response.SlicedExpertsResponse;
import com.foo.gosucatcher.domain.expert.domain.Expert;
import com.foo.gosucatcher.domain.expert.domain.ExpertRepository;
import com.foo.gosucatcher.domain.image.ImageService;
import com.foo.gosucatcher.domain.image.application.dto.response.ImageResponse;
import com.foo.gosucatcher.domain.image.application.dto.response.ImagesResponse;
import com.foo.gosucatcher.domain.item.application.dto.response.sub.SubItemResponse;
import com.foo.gosucatcher.domain.item.application.dto.response.sub.SubItemsResponse;
import com.foo.gosucatcher.domain.member.domain.Member;
import com.foo.gosucatcher.domain.member.domain.MemberImage;
import com.foo.gosucatcher.domain.member.domain.MemberRepository;
import com.foo.gosucatcher.global.error.ErrorCode;
import com.foo.gosucatcher.global.error.exception.BusinessException;
import com.foo.gosucatcher.global.error.exception.EntityNotFoundException;
import com.foo.gosucatcher.global.error.exception.InvalidValueException;

@WebMvcTest(value = {ExpertController.class}, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class ExpertControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	ExpertService expertService;

	@MockBean
	MemberRepository memberRepository;

	@MockBean
	ExpertRepository expertRepository;

	@MockBean
	ImageService imageService;

	@Mock
	private Member member;

	private ExpertCreateRequest expertCreateRequest;

	@BeforeEach
	void setUp() {
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));

		expertCreateRequest = new ExpertCreateRequest("업체명1", "위치1", 100, "부가설명1");

	}

	@Test
	@DisplayName("고수 등록 성공")
	void createExpertSuccessTest() throws Exception {
		// given
		ExpertResponse expertResponse = new ExpertResponse(1L, "업체명1", "위치1", 100, "부가설명1", 0.0, 0, null);
		given(expertService.create(anyLong(), any(ExpertUpdateRequest.class))).willReturn(expertResponse);

		// when -> then
		mockMvc.perform(
				post("/api/v1/experts").contentType(MediaType.APPLICATION_JSON).param("expertId", "1")
					.content(objectMapper.writeValueAsString(expertCreateRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.storeName").value("업체명1"))
			.andExpect(jsonPath("$.location").value("위치1"))
			.andExpect(jsonPath("$.maxTravelDistance").value(100))
			.andExpect(jsonPath("$.description").value("부가설명1"))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 등록 실패: 존재하지 않는 회원 ID")
	void createExpertFailTest_notFoundMember() throws Exception {
		// given
		given(expertService.create(anyLong(), any(ExpertUpdateRequest.class)))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER));
		ExpertCreateRequest request = new ExpertCreateRequest("업체명1", "위치1", 100, "부가설명1");

		// when -> then
		mockMvc.perform(post("/api/v1/experts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.param("expertId", "9999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("M001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 등록 실패: 중복된 상점명")
	void createExpertFailTest_duplication() throws Exception {
		// given
		ExpertCreateRequest duplicatedExpertCreateRequest = new ExpertCreateRequest("업체명1", "위치1", 100, "부가설명1");

		given(expertService.create(anyLong(), any(ExpertUpdateRequest.class)))
			.willThrow(new EntityNotFoundException(ErrorCode.DUPLICATED_EXPERT_STORENAME));

		// when -> then
		mockMvc.perform(post("/api/v1/experts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(duplicatedExpertCreateRequest))
				.param("expertId", "9999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("E002"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("상점명이 중복될 수 없습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 ID로 조회 성공")
	void getExpertByIdSuccessTest() throws Exception {
		// given
		ExpertResponse expertResponse = new ExpertResponse(1L, "업체명1", "위치1", 100, "부가설명1", 0.0, 0, null);
		given(expertService.findById(1L)).willReturn(expertResponse);

		// when -> then
		mockMvc.perform(get("/api/v1/experts").param("expertId", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.storeName").value("업체명1"))
			.andExpect(jsonPath("$.location").value("위치1"))
			.andExpect(jsonPath("$.maxTravelDistance").value(100))
			.andExpect(jsonPath("$.description").value("부가설명1"))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 ID로 조회 실패: 존재하지 않는 고수 ID")
	void getExpertByIdFailTest_notFoundExpert() throws Exception {
		// given
		given(expertService.findById(eq(9999L))).willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_EXPERT));

		// when -> then
		mockMvc.perform(get("/api/v1/experts").param("expertId", "9999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("E001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 고수입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 수정 성공")
	void updateExpertSuccessTest() throws Exception {
		// given
		ExpertUpdateRequest updateRequest = new ExpertUpdateRequest("새로운 업체명", "새로운 위치", 150, "새로운 부가설명");
		ExpertResponse expertResponse = new ExpertResponse(1L, "새로운 업체명", "새로운 위치", 150, "새로운 부가설명", 0.0, 0, null);

		given(expertService.update(1L, updateRequest)).willReturn(1L);

		// when -> then
		mockMvc.perform(patch("/api/v1/experts").param("expertId", "1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value("1"))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 수정 실패: 존재하지 않는 고수 ID")
	void updateExpertFailTest_notFoundExpert() throws Exception {
		// given
		ExpertUpdateRequest updateRequest = new ExpertUpdateRequest("새로운 업체명", "새로운 위치", 150, "새로운 부가설명");
		given(expertService.update(eq(9999L), any(ExpertUpdateRequest.class)))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_EXPERT));

		// when -> then
		mockMvc.perform(patch("/api/v1/experts").param("expertId", "9999")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("E001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 고수입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 삭제 성공")
	void deleteExpertSuccessTest() throws Exception {
		// given
		doNothing().when(expertService).delete(1L);

		// when -> then
		mockMvc.perform(delete("/api/v1/experts").param("expertId", "1")).andExpect(status().isOk()).andDo(print());
	}

	@Test
	@DisplayName("고수 삭제 실패: 존재하지 않는 고수 ID")
	void deleteExpertFailTest_notFoundExpert() throws Exception {
		// given
		doThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_EXPERT)).when(expertService).delete(eq(9999L));

		// when -> then
		mockMvc.perform(delete("/api/v1/experts").param("expertId", "9999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("E001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 고수입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("이미지 업로드 성공")
	void uploadImageSuccessTest() throws Exception {
		// given
		MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg",
			"test image content".getBytes());

		ImagesResponse response = new ImagesResponse(List.of("test.jpg"));
		given(expertService.uploadImage(any(Long.class), any())).willReturn(response);

		// when -> then
		mockMvc.perform(multipart("/api/v1/experts/images")
				.file(multipartFile).param("expertId", "1"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.filenames[0]").value("test.jpg"))
			.andDo(print());
	}

	@Test
	@DisplayName("이미지 업로드 실패 - 비어있는 파일")
	void uploadImageFailureEmptyFileTest() throws Exception {
		// given
		MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

		given(expertService.uploadImage(any(Long.class), any())).willThrow(
			new InvalidValueException(ErrorCode.EMPTY_IMAGE));

		// when -> then
		mockMvc.perform(multipart("/api/v1/experts/images")
				.file(emptyFile).param("expertId", "1"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("F002"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("이미지를 업로드 바랍니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("이미지 업로드 실패 - Expert 없음")
	void uploadImageFailureNoExpertTest() throws Exception {
		// given
		MockMultipartFile validFile = new MockMultipartFile("file", "test.jpg", "image/jpeg",
			"test image content".getBytes());

		given(expertService.uploadImage(any(Long.class), any())).willThrow(
			new EntityNotFoundException(ErrorCode.NOT_FOUND_EXPERT));

		// when -> then
		mockMvc.perform(multipart("/api/v1/experts/images")
				.file(validFile).param("expertId", "3"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("E001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 고수입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("이미지 삭제 성공")
	void deleteImageSuccessTest() throws Exception {
		// given
		doNothing().when(expertService).deleteImage(anyLong(), anyString());

		// when -> then
		mockMvc.perform(delete("/api/v1/experts/images/test.jpg").param("expertId", "1"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@Test
	@DisplayName("이미지 삭제 실패: 이미지 없음")
	void deleteImageFailureNotFoundTest() throws Exception {
		// given
		doThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_IMAGE)).when(expertService)
			.deleteImage(anyLong(), anyString());

		// when -> then
		mockMvc.perform(delete("/api/v1/experts/images/test.jpg").param("expertId", "1"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("F001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 이미지 입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("모든 이미지 가져오기 성공")
	void getImagesSuccessTest() throws Exception {
		// given
		List<String> filenames = List.of("test1.jpg", "test2.jpg");
		ImageResponse response = new ImageResponse(filenames);
		given(expertService.getAllImages(1L)).willReturn(response);

		// when -> then
		mockMvc.perform(get("/api/v1/experts/images").param("expertId", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.filenames", hasSize(2)))
			.andExpect(jsonPath("$.filenames[0]").value("test1.jpg"))
			.andExpect(jsonPath("$.filenames[1]").value("test2.jpg"))
			.andDo(print());
	}

	@Test
	@DisplayName("모든 이미지 가져오기 실패: 전문가 아이디 없음")
	void getImagesFailureNotFoundTest() throws Exception {
		// given
		given(expertService.getAllImages(anyLong())).willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_EXPERT));

		// when -> then
		mockMvc.perform(get("/api/v1/experts/images").param("expertId", "1"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("E001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 고수입니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 서브 아이템 추가 성공")
	void addSubItemSuccessTest() throws Exception {
		//given
		ExpertSubItemRequest request = new ExpertSubItemRequest("세부 서비스 이름");
		Long expertId = 1L;

		given(expertService.addSubItem(eq(expertId), any(ExpertSubItemRequest.class)))
			.willReturn(expertId);

		//when -> then
		mockMvc.perform(post("/api/v1/experts/sub-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)).param("expertId", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value(expertId))
			.andDo(print());
	}

	@Test
	@DisplayName("고수 서브 아이템 추가 실패 - 존재하지 않는 고수 or 서비스")
	void addSubItemFailTest() throws Exception {
		//given
		ExpertSubItemRequest request = new ExpertSubItemRequest("잘못된 세부 서비스 이름");
		Long expertId = 1L;

		given(expertService.addSubItem(eq(expertId), any(ExpertSubItemRequest.class)))
			.willThrow(new BusinessException(ErrorCode.ALREADY_REGISTERED_BY_SUB_ITEM));

		//when -> then
		mockMvc.perform(post("/api/v1/experts/sub-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)).param("expertId", "1"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("E004"))
			.andExpect(jsonPath("$.message").value("해당 서비스로는 이미 등록되어있습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("고수찾기 성공")
	void searchExpertsSuccessTest() throws Exception {

		// given
		MemberImage memberImage = new MemberImage("filename.jpg");
		Member memberEx = new Member("이름", "이메일", "비밀번호", "010-1234-5678", ROLE_USER, memberImage);
		List<Expert> expertList = List.of(new Expert(memberEx, "업체명1", "위치1", 100, "부가설명1", 0.0, 0));
		SlicedExpertsResponse slicedExpertsResponse = SlicedExpertsResponse.from(new SliceImpl<>(expertList));
		given(expertService.findExperts(any(), any(), any())).willReturn(slicedExpertsResponse);

		// when -> then
		mockMvc.perform(get("/api/v1/experts/search")
				.param("subItem", "세부서비스")
				.param("location", "위치1")
				.param("sort", "reviewCount,desc"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.expertsResponse[0].storeName").value("업체명1"))
			.andExpect(jsonPath("$.expertsResponse[0].location").value("위치1"))
			.andExpect(jsonPath("$.expertsResponse[0].maxTravelDistance").value(100))
			.andExpect(jsonPath("$.expertsResponse[0].description").value("부가설명1"))
			.andExpect(jsonPath("$.expertsResponse[0].rating").value(0.0))
			.andExpect(jsonPath("$.expertsResponse[0].reviewCount").value(0))
			.andExpect(jsonPath("$.hasNext").isBoolean())
			.andDo(print());
	}

	@Test
	@DisplayName("고수 서브 아이템 삭제 성공")
	void removeSubItemSuccessTest() throws Exception {
		//given
		ExpertSubItemRequest request = new ExpertSubItemRequest("세부 서비스 이름");
		Long expertId = 1L;

		doNothing().when(expertService).removeSubItem(eq(expertId), any(ExpertSubItemRequest.class));

		//when
		mockMvc.perform(delete("/api/v1/experts/sub-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)).param("expertId", "1"))
			.andExpect(status().isNoContent())
			.andDo(print());
	}

	@Test
	@DisplayName("고수 서브 아이템 삭제 실패 - 등록하지 않은 서비스")
	void removeSubItemFailTest() throws Exception {
		//given
		ExpertSubItemRequest request = new ExpertSubItemRequest("등록하지 않은 세부 서비스 이름");
		Long expertId = 1L;

		doThrow(new BusinessException(ErrorCode.NOT_FOUND_EXPERT_ITEM))
			.when(expertService).removeSubItem(anyLong(), any(ExpertSubItemRequest.class));

		//when
		mockMvc.perform(delete("/api/v1/experts/sub-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)).param("expertId", "1"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("EI001"))
			.andExpect(jsonPath("$.message").value("해당 고수는 요청한 서비스를 등록하지 않았습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("해당 고수가 가진 세부 서비스 조회")
	void getSubItemsByExpertIdSuccessTest() throws Exception {

		//given
		Long id = 1L;
		SubItemsResponse subItemsResponse = new SubItemsResponse(
			List.of(new SubItemResponse(1L, "알바", "청소 알바", "청소 알바 설명")));

		given(expertService.getSubItemsByExpertId(id))
			.willReturn(subItemsResponse);

		//when -> then
		mockMvc.perform(get("/api/v1/experts/sub-items").param("expertId", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.subItemsResponse.[0].id").value(1))
			.andExpect(jsonPath("$.subItemsResponse.[0].name").value("청소 알바"))
			.andDo(print());
	}

	@Test
	@DisplayName("고수찾기 실패: 잘못된 정렬기준")
	void searchExpertsFailureNotFoundExpertSortTypeTest() throws Exception {
		// given
		given(expertService.findExperts(any(), any(), any()))
			.willThrow(new InvalidValueException(ErrorCode.NOT_FOUND_EXPERT_SORT_TYPE));

		// when -> then
		mockMvc.perform(get("/api/v1/experts/search")
				.param("subItem", "세부서비스")
				.param("location", "위치1")
				.param("sort", "money,desc"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("E005"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("존재하지 않는 고수 찾기 정렬 타입입니다."))
			.andDo(print());
	}
}
