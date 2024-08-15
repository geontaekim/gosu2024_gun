package com.foo.gosucatcher.domain.item.application.dto.request.sub;

import com.foo.gosucatcher.domain.item.domain.MainItem;
import com.foo.gosucatcher.domain.item.domain.SubItem;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public record SubItemCreateRequest(
    @NotNull
    Long mainItemId,
    @NotBlank(message = "하위 서비스명은 필수 입력 입니다.")
    @Pattern(regexp = "^[가-힣0-9\\s]+$", message = "하위 서비스명은 한글과 숫자만 입력 가능합니다.")
    String name,
    @NotBlank(message = "해당 하위 서비스의 부가 설명을 적어주세요.")
    @Size(min = 6, message = "부가 설명은 6자 이상 작성해 주세요.")
    String description
) {

    public static SubItem toSubItem(MainItem mainItem, SubItemCreateRequest subItemCreateRequest) {
        return SubItem.builder()
            .mainItem(mainItem)
            .name(subItemCreateRequest.name)
            .description(subItemCreateRequest.description)
            .build();
    }
}
