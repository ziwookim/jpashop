package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

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

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        /**
         * 'N + 1 문제' 발생
         *  orders 데이터 개수(rows count: N)만큼 연관 테이블에 쿼리 쏜다.
         *  ex) 1 + N + N번 쿼리 실행 -> (ORDER)1 + Member(N) + Delivery(N)
         *
         * 해결방법 ? -> fetch = EAGER 로 변경하면 ? 안된다.
         * 양방향 연관관계에서 쿼리 전달 예측이 어렵다.
         * EAGER 사용 절대 하지 말 것.
         *
         * 모든 연관관계 데이터는 모두 fetch = LAZY로 설정할 것.
         * 쿼리 최적화로 해결할 수 있도록 한다.
         *
         * * 지연로딩(LAZY)은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우에는 쿼리를 생략한다.
         *
         */
        List<SimpleOrderDto> result =  orders.stream()
                .map(o -> new SimpleOrderDto(o))
//                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    /**
     * v3 vs v4 ?
     * 조회 빈도수를 고려해서 고르는 것이 바람직하다.
     */

    /**
     * api 스펙을 명확하게 규제
     */
    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address; // value Object

        /**
         * Dto가 Entity를 파라미터로 갖는 것은 문제가 되지 않는다.
         * 중요하지 않은 Dto가 중요한 Entity에게 의존하는 상황이기 때문.
         */
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }

    /**
     * ** 쿼리 방식 선택 권장 순서
     * 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다. -> (v2)
     * 2. 필요하면 fetch join으로 성능을 최적화 한다. -> 대부분의 성능 이슈 해결 된다. (v3)
     * 3. 그래도 안된다면, DTO로 직접 조회하는 방법을 사용한다. -> (v4)
     * 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
     */
}
