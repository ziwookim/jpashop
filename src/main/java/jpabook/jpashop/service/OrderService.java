package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // memberId만 받았기 떄문에 MemberRepository 필요
        // itemId만 받았기 떄문에 ItemRepository 필요
        // 엔티티 조회
//        Member member = memberRepository.findOne(memberId);
        Member member = memberRepository.findById(memberId).get();
        Item item = itemRepository.findOne(itemId);

        // 배송정보 생성
        Delivery delivery = new Delivery();
        // 회원 정보의 주문 정보를 배송 정보로 설정
        delivery.setAddress(member.getAddress());

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
//        OrderItem orderItem1 = new OrderItem(); // 생성자 Access protected로 제한해서 제약을 줄 것 -> 유지보수 효율 높이는 방법

        // 주문 생성 (일단 단일 상품만 주문 되도록 지정)
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        orderRepository.save(order);
        // delivery, orderItem은 cascade = CascadeType.ALL 옵션 설정으로 Order save 실행할 때 자동으로 save 실행 된다.
        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        // 주문 취소
        order.cancel();
        /**
         * 도메일 모델 패턴의 장점
         * 엔티티에 대해 (단위) 테스트 작성 가능 (핵심 비즈니스 로직이 대부분 엔티티에 포함 되어 있기 때문.)
         */

        /**
         * *** JPA 사용의 최대 장점 ***
         * entity 데이터만 변경하면 변경 내역 감지가 일어나면서 변경 내역을 찾아 데이터 베이스에 update 쿼리가 자동으로 전달 된다.
         */
    }

    // 검색
    public List<Order> findOrders(OrderSearch orderSearch) {
//        return orderRepository.findAllByString(orderSearch);
        return orderRepository.findAllByCriteria(orderSearch);
    }
}