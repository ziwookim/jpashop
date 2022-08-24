package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToMany
    // 실무에서 @ManyToMany 관계 절대 사용하지 말 것. (크게 사용할 일도 없음.)
    @JoinTable(name = "category_item",
            joinColumns =  @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    // 중간 테이블이 있어야만 ManyToMany 관계 맵핑이 가능하다.
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    // == 연관관계 메서드 == //
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }
}
