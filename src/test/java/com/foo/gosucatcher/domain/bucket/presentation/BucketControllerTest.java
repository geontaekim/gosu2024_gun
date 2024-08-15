package com.foo.gosucatcher.domain.bucket.presentation;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foo.gosucatcher.domain.bucket.application.BucketService;
import com.foo.gosucatcher.domain.bucket.dto.request.BucketRequest;
import com.foo.gosucatcher.domain.bucket.dto.response.BucketResponse;
import com.foo.gosucatcher.domain.bucket.dto.response.BucketsResponse;

@WebMvcTest(value = {BucketController.class}, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class BucketControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private BucketService bucketService;

	@Test
	@DisplayName("찜 내역을 모두 조회할 수 있다")
	void findAll() throws Exception {
		// given
		BucketsResponse bucketsResponse = new BucketsResponse(List.of(new BucketResponse(1L, 2L, 3L)),
			false);
		given(bucketService.findAll(any(PageRequest.class)))
			.willReturn(bucketsResponse);

		// when
		// then
		mockMvc.perform(get("/api/v1/buckets")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.buckets[0].id").value(1L))
			.andExpect(jsonPath("$.buckets[0].expertId").value(2L))
			.andExpect(jsonPath("$.buckets[0].memberId").value(3L));
	}

	@Test
	@DisplayName("사용자는 고수를 찜할 수 있다")
	void like() throws Exception {

		// given
		BucketRequest bucketRequest = new BucketRequest(1L, 2L);
		BucketResponse bucketResponse = new BucketResponse(0L, 1L, 2L);
		given(bucketService.create(any(BucketRequest.class)))
			.willReturn(bucketResponse);

		// when
		// then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/buckets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bucketRequest)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$..id").value(0))
			.andExpect(jsonPath("$..expertId").value(1))
			.andExpect(jsonPath("$..memberId").value(2));
	}

	@Test
	@DisplayName("사용자가 특정 고수를 찜했는지 여부를 조회할 수 있다")
	void checkLikedYN() throws Exception {

		// given
		BucketRequest bucketRequest = new BucketRequest(1L, 2L);
		bucketService.create(bucketRequest);

		String expertId = "1";
		String memberId = "2";
		given(bucketService.checkStatus(any(Long.class), any(Long.class)))
			.willReturn(Boolean.TRUE);

		// when
		// then
		mockMvc.perform(get("/api/v1/buckets/status")
				.contentType(MediaType.APPLICATION_JSON)
				.param("expertId", expertId)
				.param("memberId", memberId)
				.content(objectMapper.writeValueAsString(bucketRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value("true"));
	}

	@Test
	@DisplayName("사용자는 찜을 취소할 수 있다")
	void delete() throws Exception {

		// given
		long id = 0L;
		doNothing()
			.when(bucketService)
			.deleteById(id);

		// when
		// then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/buckets/{id}", id)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	@Test
	@DisplayName("사용자가 찜한 고수의 목록을 알 수 있다")
	void findAllByMemberId() throws Exception {

		// given
		BucketsResponse bucketsResponse = new BucketsResponse(
			List.of(
				new BucketResponse(1L, 2L, 3L),
				new BucketResponse(2L, 1L, 3L)
			),
			false);

		given(bucketService.findAllByMemberId(any(Long.class), any(PageRequest.class)))
			.willReturn(bucketsResponse);

		// when
		// then
		mockMvc.perform(get("/api/v1/buckets/members")
				.param("memberId", "3")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.buckets[0].id").value(1L))
			.andExpect(jsonPath("$.buckets[0].expertId").value(2L))
			.andExpect(
				jsonPath("$.buckets[0].memberId").value(3L))
			.andExpect(jsonPath("$.buckets[1].id").value(2L))
			.andExpect(jsonPath("$.buckets[1].expertId").value(1L))
			.andExpect(
				jsonPath("$.buckets[1].memberId").value(3L));
	}

}
