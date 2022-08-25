package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

// Memory, DB 영역까지 모두 테스트하는 목적 - Spring Integration 테스트
@RunWith(SpringRunner.class) // jUnit 실행할 때, Spring 같이 엮어서 실행하겠다.
@SpringBootTest // SpringBoot 띄운 상태에서 test 해야하는 경우, Container -> @Autowired
@Transactional // rollback 가능 처리 // default: rollback // ** TEST 에서만 사용한다.
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
    @Rollback(false) // commit 처리 (insert 처리)
    public void 회원가입() throws Exception {
        //given <- 이런 상황이 주어졌을 때
        Member member = new Member();
        member.setName("kim");

        //when <- 이렇게 하면
        Long saveId = memberService.join(member);

        //then <- 이렇게 된다. (결과)
//        em.flush(); // ** insert 처리 // 그렇지만, @Transactional rollback=true이면 다시 Rolled Back 된다.
        assertEquals(member, memberRepository.findOne(saveId));
        // @Transactional 어노테이션 사용했기 때문에, 동일한 transaction 에서 pk 값이 같은 객체는 하나로 관리가 되기 때문에 가능하다.

    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        memberService.join(member2); // except EXCEPTION !!! (예외 발생 해야 한다.)
//        try {
//            memberService.join(member2); // except EXCEPTION !!! (예외 발생 해야 한다.)
//        } catch(IllegalStateException e) {
//            return;
//        }
        // try - catch -> @Test(excepted = Exception.class)으로 대체 가능.

        //then
        fail("예외가 발생해야 한다."); // 'then까지 오면 안될 때.'
    }
}