package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        /**
         * validation 체크 활용할 수 있기 때문에 비어있는 객체를 전달한다.
         */
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm memberForm, BindingResult result) {
        // ** MemberForm 클래스와 Member 클래스의 분리 **
        // 화면용 데이터와 domain 데이터 구분할 것. (정확하게 일치하지 않음.)

        // @Valid <- javax.validation 기능 사용
        // BindingResult <- Validate에 대한 오류 발생시 오류가 담겨서 해당 코드가 실행이 된다.
        if(result.hasErrors()) {
            return "members/createMemberForm";
            // MemberForm 객체가 유지 되기 때문에 에러 발생하더라도 다른 필드 입력된 값 유지 가능한 이유
        }

        Address address = new Address(memberForm.getCity(), memberForm.getStreet(), memberForm.getZipcode());

        Member member = new Member();
        member.setName(memberForm.getName());
        member.setAddress(address);

        memberService.join(member);

        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model) {
        /**
         * Entity와 폼(화면용 데이터)을 구분할 것.
         * 아주 단순한 데이터 외에는 엔티티 데이터를 그대로 유지하는 것이 최우선이기 때문에,
         * 화면 출력에 종속적인 것들을 엔티티 자체에 더하지 않고, 폼 객체(DTO)를 따로 생성하는 것이 바람직하다.
         */

        /**
         * API 작성시에는 이유를 불문하고 Entity 자체를 외부로 반환해서는 안된다.
         * 개인정보 유출될 가능성
         * Entity에 로직을 추가했는데 API spec이 변해버리는 등 문제 발생 가능성
         * 불안전 API 스펙
         */

        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);

        return "members/memberList";
    }
}
