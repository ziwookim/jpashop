package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

//    @PersistenceContext // 원래 EntityManager 객체는 @PersistenceContext 어노테이션 존재해야하지만,
//    @Autowired          // Spring DATA JPA가 지원해줘서 서 @PersistenceContext -> @Autowired 와 동일하게 적용가능
// **최신 버전 Spring 사용시, 생성자가 오직 한개뿐인 경우, @Autowired 어노테이션 생략해도 자동으로 injection 처리를 해준다.
    private final EntityManager em;

//    public MemberRepository(EntityManager em) {
//        this.em = em;
//    }

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        // JPQL 쿼리문은 Entity 객체 대상을 쿼리를 실행
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
