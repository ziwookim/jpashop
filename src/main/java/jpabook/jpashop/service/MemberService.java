package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 조회하는 곳에서 성능을 최적화한다.
//@AllArgsConstructor // 모든 필드 변수 생성자를 자동 생성한다.
@RequiredArgsConstructor // final인 필드 변수 생성자를 자동 생성한다.
public class MemberService {

//    @Autowired // @Autowired 단점: field 값 변경 어려움, Access 어려움.
//    private MemberRepository memberRepository;

    private final MemberRepository memberRepository;

//    @Autowired // @Autowired 해결방안 -> setter injection
//               // 단점: RunTime 시점에 누군가가 변경 가능한 위험 부담.
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

//    @Autowired // setter injection 해결방안 -> '생성자 injection'
    // **최신 버전 Spring 사용시, 생성자가 오직 한개뿐인 경우, @Autowired 어노테이션 생략해도 자동으로 injection 처리를 해준다.
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }
    // '@RequiredArgsConstructor' 로 대체

    /**
    * 회원 가입
     */
    @Transactional // readOnly = false
    public Long join(Member member) {
        // 중복 회원 검증
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    public void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        // 검증 조건인 MEMBER 테이블의 NAME 컬럽은 'UNIQUE' 제약 조건을 추가해놓는 것이 안전하다.

        if(!findMembers.isEmpty()) {
//      if(findMembers.size() > 0)
            // EXCEPTION
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
