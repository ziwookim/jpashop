package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        // Collection 데이터를 제외하고 가져옴.
        List<OrderQueryDto> result = findOrders(); // 1번 -> N개

        // Collection 데이터 forEach로 호출함.
        result.forEach(o -> { // N번
            List<OrderItemQueryDto> orderItems = findOrdersItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result;
    }

    public List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderItemQueryDto> findOrdersItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, oi.item.name, oi.orderPrice, oi.count)  " +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization() {
        // Query: 루트 1번 (order, member, delivery)
        List<OrderQueryDto> result = findOrders();

        // orderItem 뽑을 orderId 값 담기
//        List<Long> orderIds = toOrderIds(result);

        // where orderId in ~ orderIds
        // Query: 컬렉션 1번 (orderItems)
//        List<OrderItemQueryDto> orderItems = findOrderItemMap(orderIds);
        List<OrderItemQueryDto> orderItems = findOrderItemMap(toOrderIds(result));

        /**
         * 쿼리 총 2번 실행
         */

        // optimization to Map
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private List<OrderItemQueryDto> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, oi.item.name, oi.orderPrice, oi.count)  " +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
        return orderItems;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }
}
