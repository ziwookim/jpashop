package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {

        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }


    /**
     * JPA Criteria
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건

        return query.getResultList();
    }

    /**
     * Querydsl 적용
     */
    public List<Order> findAll(OrderSearch orderSearch) {
        QOrder order = QOrder.order;
        QMember member = QMember.member;

        JPAQueryFactory query = new JPAQueryFactory(em);

        return query
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if(statusCond == null) {
            return null;
        }
        return QOrder.order.status.eq(statusCond);
    }

    private BooleanExpression nameLike(String nameCond) {
        if(!StringUtils.hasText(nameCond)) {
            return null;
        }
        return QMember.member.name.like(nameCond);
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        /**
         * fetch join
         * fetch 데이터를 한 쿼리 데이터에서 모두 가져오게 함.
         * 'LAZY'로 설정 후, fetch join을 이용하면 대부분의 성능 문제를 해결할 수 있다.
         */

        /**
         * Order : Member
         * Order : Delivery 와 같은 toOne 관계일 경우,
         * 모두 fetch join을 이용한다.
         */
        return em.createQuery(
//                        "select o from Order o ", Order.class)
                "select o from Order o " +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Order> findAllWithItem() {
        /**
         * JPA distinct != DB distinct
         * select distinct o from Order o <- 이런 경우,
         * 중복되는 Order Entity의 중복을 후 데이터를 가져온다.
         *
         * fetch join으로 SQL 1번만 실행됨.
         * 'distinct'를 사용한 이유는 1대 다 조인이 있으므로, 데이터베이스 row가 증가한다.
         * 그 결과, 같은 order 엔티티의 조회수도 증가하게 된다. JPA의 distinct는 SQL에 distinct를 추가하고, 더해서 같은 엔티티가 조회되면, 애플리케이션에서 중복을 걸러준다.
         * 이 예에서 order가 컬렉션 페치 조인 때문에 중복 조회 되는 것을 막아준다.
         *
         * ** 단점: 페이징 불가능(distinct 사용 시, order 순으로 데이터를 뽑아오는 것 자체가 말이 안되게 된다.)
         * firstResult/maxResults 설정 경우, 모든 데이터를 memory에 퍼올린다. -> out of memory 위험 부담이 너무 높다.
         * <<로그 내용>>
         * firstResult/maxResults specified with collection fetch; applying in memory!
         *
         * ** 1대 다 fetch join에서는 페이징을 사용하지 말 것!
         *
         * 컬렉션 fetch join은 1개만 사용할 수 있다. -> (1대 다 관계에 대한 조인은 한번만 사용한다.)
         * 컬렉션 둘 이상에 페치 조인을 사용하면 안된다.
         * 데이터가 부정합하게 조회될 수 있다.
         */
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi", Order.class)
//                .setFirstResult(1) // start numbered from 0
//                .setMaxResults(100)
                .getResultList();
    }
}