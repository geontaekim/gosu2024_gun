package com.foo.gosucatcher.global.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.foo.gosucatcher.global.security.CustomUserDetails;
import com.foo.gosucatcher.global.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Aspect
@Component
public class CurrentMemberEmailAop {

	private static final String MEMBER_ID = "memberEmail";
	private final JwtTokenProvider jwtTokenProvider;

	@Around("@annotation(currentMemberEmail)")
	public Object getCurrentMemberEmail(ProceedingJoinPoint proceedingJoinPoint,
		CurrentMemberEmail currentMemberEmail) throws
		Throwable {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();

		String AuthorizationHeaderValue = request.getHeader("Authorization");

		String token = jwtTokenProvider.removeBearer(AuthorizationHeaderValue);

		Authentication authentication = jwtTokenProvider.getAccessTokenAuthentication(token);
		CustomUserDetails principal = (CustomUserDetails)authentication.getPrincipal();
		String memberEmail = principal.getMember().getEmail();

		Object[] modifiedArgs = modifyArgsWithMemberEmail(memberEmail, proceedingJoinPoint);

		return proceedingJoinPoint.proceed(modifiedArgs);
	}

	private Object[] modifyArgsWithMemberEmail(String memberEmail, ProceedingJoinPoint proceedingJoinPoint) {
	    Object[] parameters = proceedingJoinPoint.getArgs();

	    MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
	    Method method = signature.getMethod();
	    Parameter[] methodParameters = method.getParameters();

	    for (int i = 0; i < methodParameters.length; i++) {
	        if (methodParameters[i].getType().equals(String.class)) {  // String 타입 파라미터를 찾음
	            parameters[i] = memberEmail;
	            break;  // 첫 번째 String 타입 파라미터를 찾았으므로 반복문을 종료
	        }
	    }

	    return parameters;
	}

}
