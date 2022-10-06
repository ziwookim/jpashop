package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue
    @Column(name="order_id")
    private Long id;

    /** FetchType.LAZY: 지연 로딩
     *  DB에서 긁어오지 않는다.
     *  Order 데이터만 가져온다.
     *  대신 Proxy Member 객체를 생성해서 가져온다. -> ByteBuddy(Interceptor)
     *  jackson(json 관련) 라이브러리가 읽어들일 수 없음.
     *  (proxy 기술)
     * **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    // foreignKey (연관관계 주인)
    private Member member;
    // FetchType.LAZY 일 경우, 실제 DB -> private Member member = new ByteBuddyInterceptor();

    /**
     * 지연로딩 최적화
     * 1. application.yml -> hibernate.default_batch_fetch_size: 개수 입력 <- 전체 최적화
     * 2. @BatchSize <- 개별 최적화
     *
     * 이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회한다.
     * 100 ~ 1000 개로 제한할 수 있도록 한다.
     * 1000으로 잡으면 DB에 순간 부하가 증가할 수 있다.
     * 하지만 100이든 1000이든 결국 전체 데이터를 로딩해야 하므로 메모리 사용량이 같다.
     *
     * WAS, DB가 버틸 수 있다면 1000으로,
     * 아니라면 100개씩 늘려가면서 성능을 확인해보면서 설정하는 것이 가장 바람직하다.
     */
    @BatchSize(size = 1000)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; // 주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태 [ORDER, CANCEL]

    // == 연관관계 편의 메서드 == // ** 양방향 관계
    // Member를 설정을 할 때, Member의 Order 데이터까지 한번에 설정 되도록.
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    /**
     * ** 참고 **
     * 엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것을 '도메인 모델 패턴' 이라 한다.
     * (서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할만 한다.)
     *
     * 반대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 대부분의 비즈니스 로직을 처리하는 것을 '트랜잭션 스크립트 패턴' 이라 한다.
     *
     * ** 이 프로젝트에서는 '도메인 모델 패턴'을 이용한다. **
     *
     * ** 정답은 없다. 기능과 문맥에 더 적합한 것을 이용하면 된다. 한 프로젝트 내에서 복합적으로 사용되기도 한다.
     */
    //== 생성 메서드 ==//
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        // 주문시간 현재시간으로 지정
        return order;
    }

    //== 비즈니스 로직 ==//
    /**
     * 주문 취소
     */
    public void cancel() {
        if(delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for(OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //== 조회 로직 ==//
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
//        return orderItems.stream()
//                .mapToInt(OrderItem::getTotalPrice)
//                .sum();
    }
}
