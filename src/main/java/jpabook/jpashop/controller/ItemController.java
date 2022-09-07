package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        Book book = new Book();
        // * setter를 줄이는게 좋은 코딩
        // * 생성자 파라미터로 처리하거나 메소드 파라미터로 처리하는 것이 바람직하다.

        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);

        return "items/itemList";
    }

    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);
        // 예제 단순화를 위해 Book으로 형변환 한다.

        BookForm form = new BookForm();
        // update data
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());
        //intelliJ multi line select 기능 참고

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    @PostMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, @ModelAttribute("form") BookForm form) {

        // 수정 내용 받은 Web에서만 쓰기로한 BookForm

        Book book = new Book();
        /**
         * setter를 통한 update가 아닌, 의미 있는 메소드를 생성, 호출 해서 필드 변경이 실제로 일
         * 추적과 코드 재활용/가독성 향상
         */
        book.setId(form.getId()); // <- 데이터베이스에 이미 갔다온(DB에 저장된 후 호출된) 식별자가 존재하는 데이터를 *'준영속' 상태의 객체라고 한다.
                                  // <- 데이터 베이스가 식별할 수 있는 데이터
        /**
         * 준영속 엔티티?
         * 영속성 컨텍스트가 더는 JPA가 관리하지 않는 엔티티를 말한다. ( <- 문제점 / 변경 감지(dirty checking) 안함. / 데이터에 변경된 값 설정만으로 DB 수정 작업 일어나지 않음.)
         * 이미 DB에 한번 저장 되어서 식별자가 존재한다. 이렇게 임의로 만들어낸 엔티티도 기존 식별자를 갖고 있으면 준영속 엔티티로 볼 수 있다.
         *
         * 준영속 엔티티를 수정하는 2가지 방법
         * 1) 변경 감지(dirty checking) 기능 사용 <- 실무에서는 변경 감지만 사용할 수 있도록 한다.
         * 2) 병합(merge) 사용
         *
         * ! 주의: 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만,
         *        병합을 사용하면 모든 속성이 변경된다. 병합시 값이 없으면 null로 업데이트 할 위험도 있다.
         *        (* 병합은 모든 필드를 교체한다. 누락된 필드가 하나라도 있는 경우, 병합이 일어날 때 null로 변경 된다.)
         */

        // * itemId URL로 노출 되어 데이터 수정하려는 사용자가 해당 데이터 수정 권한 있는지 확인하는 절차 반드시 필요 !

//        Book book = new Book();
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());
//        itemService.saveItem(book);
        /**
         * 컨트롤러에서 어설프게 엔티티를 생성하지 말 것!
         * 수정할 필드 값만 넘겨 받아 넘길 것.
         * 업데이트할 필드가 많다면 서비스 계층에 DTO를 따로 만들 것.
         */
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());

        return "redirect:/items";
    }
}
