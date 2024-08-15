package com.foo.gosucatcher.domain.item.presentation;

import static com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes.ARRAY;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foo.gosucatcher.domain.item.application.MainItemService;
import com.foo.gosucatcher.domain.item.application.dto.request.main.MainItemCreateRequest;
import com.foo.gosucatcher.domain.item.application.dto.request.main.MainItemUpdateRequest;
import com.foo.gosucatcher.domain.item.application.dto.response.main.MainItemResponse;
import com.foo.gosucatcher.domain.item.application.dto.response.main.MainItemsResponse;
import com.foo.gosucatcher.global.error.ErrorCode;
import com.foo.gosucatcher.global.error.exception.BusinessException;
import com.foo.gosucatcher.global.error.exception.EntityNotFoundException;

@WebMvcTest(value = {MainItemController.class}, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureRestDocs
class MainItemControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	MainItemService mainItemService;

	@Test
	@DisplayName("메인 서비스 등록 성공")
	void createMainItemSuccessTest() throws Exception {

		//given
		MainItemCreateRequest mainItemCreateRequest = new MainItemCreateRequest("알바", "설명을적습니다.");
		MainItemResponse mainItemResponse = new MainItemResponse(1L, "알바", "설명을적습니다.");

		given(mainItemService.create(any()))
			.willReturn(mainItemResponse);

		//when -> then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/main-items")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mainItemCreateRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("알바"))
			.andExpect(jsonPath("$.description").value("설명을적습니다."))
			.andDo(print())
			.andDo(document("mainItem-create",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("name").type(STRING).description("메인 서비스 이름"),
					fieldWithPath("description").type(STRING).description("설명")),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("메인 서비스ID"),
					fieldWithPath("name").type(STRING).description("메인 서비스 이름"),
					fieldWithPath("description").type(STRING).description("설명"))
			));
	}

	@Test
	@DisplayName("메인 서비스 등록 실패 - 중복된 이름")
	void createMainItemFailTest_duplicatedName() throws Exception {

		//given
		MainItemCreateRequest mainItemCreateRequest = new MainItemCreateRequest("알바", "내용변경입니다.");

		given(mainItemService.create(any()))
			.willThrow(new BusinessException(ErrorCode.DUPLICATED_MAIN_ITEM_NAME));

		//when -> then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/main-items")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mainItemCreateRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("MI002"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("메인 서비스 이름이 중복될 수 없습니다."))
			.andDo(document("mainItem-create-fail-duplicated-name",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("name").type(STRING).description("중복된 메인 서비스 이름"),
					fieldWithPath("description").type(STRING).description("설명")),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("오류 코드"),
					fieldWithPath("errors").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@Test
	@DisplayName("메인 서비스 등록 실패 - 잘못된 값을 요청")
	void createMainItemFailTest_invalidValue() throws Exception {

		//given
		MainItemCreateRequest mainItemCreateRequest = new MainItemCreateRequest(" ", "설명을적습니다.");

		//when -> then
		mockMvc.perform(post("/api/v1/main-items")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mainItemCreateRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("C001"))
			.andExpect(jsonPath("$.errors[0].value").value(" "))
			.andExpect(jsonPath("$.errors[0].reason").value("서비스명은 필수 입력 입니다."))
			.andExpect(jsonPath("$.message").value("잘못된 값을 입력하셨습니다."))
			.andDo(print())
			.andDo(document("mainItem-create-fail-invalid-value",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("name").type(STRING).description("중복된 메인 서비스 이름"),
					fieldWithPath("description").type(STRING).description("설명")),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("오류 코드"),
					fieldWithPath("errors").type(ARRAY).description("오류 목록").optional(),
					fieldWithPath("errors[].field").type(STRING).description("오류 필드"),
					fieldWithPath("errors[].value").type(STRING).description("오류 값"),
					fieldWithPath("errors[].reason").type(STRING).description("오류 이유"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@Test
	@DisplayName("메인 서비스 전체 조회")
	void findAllSuccessTest() throws Exception {

		//given
		MainItemsResponse mainItemsResponse = new MainItemsResponse(List.of(new MainItemResponse(1L, "알바", "설명")));
		given(mainItemService.findAll()).willReturn(mainItemsResponse);

		//when -> then
		mockMvc.perform(get("/api/v1/main-items")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.mainItemsResponse[0].id").value(1L))
			.andExpect(jsonPath("$.mainItemsResponse[0].name").value("알바"))
			.andExpect(jsonPath("$.mainItemsResponse[0].description").value("설명"))
			.andDo(print())
			.andDo(document("mainItem-find-all",
				preprocessResponse(prettyPrint()),
				responseFields(
					fieldWithPath("mainItemsResponse[].id").type(NUMBER).description("메인 서비스 ID"),
					fieldWithPath("mainItemsResponse[].name").type(STRING).description("메인 서비스 이름"),
					fieldWithPath("mainItemsResponse[].description").type(STRING).description("설명")
				)));
		;
	}

	@Test
	@DisplayName("메인 서비스 ID로 조회")
	void findMainItemByIdSuccessTest() throws Exception {

		//given
		MainItemResponse mainItemResponse = new MainItemResponse(1L, "알바", "내용");
		given(mainItemService.findById(anyLong())).willReturn(mainItemResponse);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/main-items/{id}", mainItemResponse.id())
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.name").value("알바"))
			.andExpect(jsonPath("$.description").value("내용"))
			.andDo(print())
			.andDo(document("mainItem-get-one-by-id",
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("id").description("메인 서비스id")),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("메인 서비스id"),
					fieldWithPath("name").type(STRING).description("메인 서비스 이름"),
					fieldWithPath("description").type(STRING).description("설명")
				)));
		;
	}

	@Test
	@DisplayName("메인 서비스 ID로 조회 실패 - 존재하지 않는 서비스")
	void findMainItemByIdFailTest_notFoundEntity() throws Exception {

		//given
		Long invalidId = 100L;
		given(mainItemService.findById(anyLong()))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_MAIN_ITEM));

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/main-items/{id}", invalidId)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("MI001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("메인 서비스를 찾을 수 없습니다."))
			.andDo(print())
			.andDo(document("mainItem-get-one-by-id-fail-not-found",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("id").description("메인 서비스id")),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("오류 코드"),
					fieldWithPath("errors").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
		;
	}

	@Test
	@DisplayName("메인 서비스 수정 성공")
	void updateMainItemSuccessTest() throws Exception {

		//given
		MainItemUpdateRequest mainItemUpdateRequest = new MainItemUpdateRequest("레슨", "내용변경입니다.");
		MainItemResponse mainItemResponse = new MainItemResponse(1L, "레슨", "내용변경입니다.");
		given(mainItemService.update(anyLong(), any()))
			.willReturn(mainItemResponse);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/v1/main-items/{id}", 1L, mainItemUpdateRequest)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mainItemUpdateRequest)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("mainItem-update",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("id").description("메인 서비스id")),
				requestFields(
					fieldWithPath("name").type(STRING).description("변경할 서비스 이름"),
					fieldWithPath("description").type(STRING).description("변경할 설명")),
				responseFields(
					fieldWithPath("id").type(NUMBER).description("메인 서비스 ID"),
					fieldWithPath("name").type(STRING).description("메인 서비스 이름"),
					fieldWithPath("description").type(STRING).description("설명"))
			));
	}
}
