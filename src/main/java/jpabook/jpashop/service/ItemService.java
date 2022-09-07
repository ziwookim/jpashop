package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional // @Transactional(readOnly = false)
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {

        // 가급적 메소드를 통해 작업 할 것.
        change(itemId, name, price, stockQuantity);

        // itemId로 실제 DB에 저장된 영속성 엔티티 findItem을 찾아옴.
//        Item findItem = itemRepository.findOne(itemId);
//        findItem.setName(name);
//        findItem.setPrice(price);
//        findItem.setStockQuantity(stockQuantity);

//        itemRepository.save(findItem); // save 메소드를 호출할 필요가 없다.

        /** 변경 감지(dirty checking) 기능 사용을 통한 준영속 엔티티를 수정하는 방법 ** (@Transactional 어노테이션으로 Commit/flush 기능 활용)
         * findItem 필드 값 변경 후, @Transactional에 의해서 Commit 작업이 일어나고 'flush' 작업이 일어나, 변경감지가 일어나 update 작업이 발생한다.
         *
         * 트랜잭션이 있는 서비스 계층에 식별자('id')와 변경할 데이터를 명화하게 전달할 것.(필드값 or Dto)
         * 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 setter를 통해 직접 변경할 것.
         * 트랜잭션 커밋 시점에 변경 감지가 실행 된다.
         */
    }

    @Transactional
    public void change(Long itemId, String name, int price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId);

        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
