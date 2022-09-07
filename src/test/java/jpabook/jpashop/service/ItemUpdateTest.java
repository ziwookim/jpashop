package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em;

    @Test
    public void updateTest() throws  Exception {
        Book book = em.find(Book.class, 1L);

        // TX (한 Transaction 내에서)
        book.setName("asdfasdf");

        // 변경감지 == dirty checking // JPA Entity 데이터 업데이트
        // TX commit
    }
}
