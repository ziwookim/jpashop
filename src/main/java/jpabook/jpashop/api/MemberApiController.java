package jpabook.jpashop.api;


import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

//@Controller @ResponseBody
@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
public class MemberApiController {

    private  final MemberService memberService;

    /**
     * API to enroll a member .ver1
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        /**
         * API 통신을 위한 별도의 DTO 생성 필수 !
         * 외부 Entity 그대로 받아서 사용하면 안된다.
         * @Valid annotation 활용 -> DTO 클래스에서 valid할 컬럼에 @NotEmpty 붙일 것.
         * --> .ver2
         */

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        /**
         * ver2. Entity를 변경해도 API 스펙에 영향을 주지 않는다.
         */
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        // only process to update
        Member findMember = memberService.findOne(id);
        // only process to find entity
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {

        @NotEmpty
        private String name;
    }

    @Data
    static class CreateMemberResponse{
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

}
