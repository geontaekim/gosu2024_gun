package com.foo.gosucatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class S3Config {
	
	
	/*
	@Value("${cloud.aws.credentials.accessKey}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secretKey}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;
	*/
	
	private final String accessKey = "YOUR_LOCAL_ACCESS_KEY";
    private final String secretKey = "YOUR_LOCAL_SECRET_KEY";
    private final String region = "YOUR_LOCAL_REGION";
    /*
	@Bean
	public AmazonS3 amazonS3() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		return AmazonS3ClientBuilder
			.standard()
			.withCredentials(new AWSStaticCredentialsProvider(credentials))
			.withRegion(region)
			.build();
	}
	*/

}
