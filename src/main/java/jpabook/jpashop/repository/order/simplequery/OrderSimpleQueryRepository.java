package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {
    /**
     * DTO 전용 repository를 따로 분리하는 것이 유지보수에 훨씬 더 유리하다.
     */

    private final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos() {
        /**
         * 장점: api에 필요한 데이터만 가져옴. -> 애플리케이션 네트워크 용량 최적(생각보다 미비함.)
         * 단점:
         * repository 재사용성이 떨어짐.
         * API 스펙에 맞춘 코드가 repository에 들어감.
         * 가독성 떨어짐.
         */
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
