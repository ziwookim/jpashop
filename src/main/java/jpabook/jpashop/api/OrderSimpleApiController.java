package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xToOne (ManyToOne, OneToOne)
 * 관계 성능 최적화
 *
 * Order
 * Order -> Member
 * Order -> Delivery
 * OrderItem -> Order
 *
 * 해결방안
 * 1) 양방향 연관관계에 있는 데이터 컬럼에 대해 @JsonIgnore 처리 <- 양쪽을 서로 호출하면서 무한 루프 발생.
 * 2) Hibernate5Module로 proxy 객체 처리
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private  final OrderRepository orderRepository;

    /**
     * Entity 직접 노출 절대 절대 안된다.
     * 연관관계 데이터에 대한 fetch 여부 및 처리 필요,
     * 필요하지 않은 데이터까지 모두 조회하는 쿼리문 실행 -> 성능 저하
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all) {
            /**
             * order.getMember()
             * order.getDelivery()
             * order.객체타입() <- 여기까지 proxy 객체
             */
            order.getMember().getName(); // LAZY 강제 초기화 -> 이 라인에서 Member 조회 쿼리를 날려서 데이터를 가져온다.
            order.getDelivery().getAddress(); // LAZY 강제 초기화
        }
        return all;
    }
}
