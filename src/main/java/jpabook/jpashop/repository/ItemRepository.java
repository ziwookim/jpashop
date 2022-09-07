package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if(item.getId() == null) {
            // 저장(flush) 전까지 @GeneratedValue인 id값은 null
            // 즉, 저장되지 않은 item
            em.persist(item);
        } else {
            // 이미 저장된 이력이 있는 Item인 경우,
            // merge(*병합) : update와 유사
            em.merge(item); // .merge(parameter <- 머지할 값을 가진 객체는 영속성 객체로 변하지 않는다.)
            /**
             * merge(병합) 과정
             * 1. 파라미터로 DB에 저장된 실제 데이터 엔티티 찾기
             * 2. 찾아온 엔티티에 변경된 필드 값 모두 밀어 넣기. (병합하기 / 값 채우기)
             * 3. 반환
             *
             * Item merged = em.merge(item);
             * .merge(item) <- 머지할 값을 가진 'item' 객체는 영속성 객체로 변하지 않는다.
             * 반환된 'merged' 객체만 영속성 객체로 JPA가 관리하는 대상이다.
             */
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
