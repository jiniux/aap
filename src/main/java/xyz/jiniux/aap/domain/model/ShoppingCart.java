package xyz.jiniux.aap.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import xyz.jiniux.aap.domain.cart.exceptions.CannotRemoveMoreThanOriginalQuantityException;

import java.io.Serializable;
import java.util.*;


@Entity
public class ShoppingCart {
    @Id
    @GeneratedValue
    @Getter
    @Setter
    private Long id;

    @Setter
    @Getter
    @Column(nullable = false, unique = true)
    private String username;

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"isbn", "stockFormat", "stockQuality", "quantity"})
    public static class Item implements Serializable {
        private final String isbn;
        private final StockFormat stockFormat;
        private final StockQuality stockQuality;

        @Setter
        private long quantity;

        public void addQuantity(long additionalQuantity) {
            quantity += additionalQuantity;
        }

        ItemKey createItemKey() {
            return ItemKey.builder().isbn(getIsbn())
                .stockFormat(getStockFormat())
                .stockQuality(getStockQuality())
                .build();
        }
    }

    @Builder
    public record ItemKey(String isbn, StockFormat stockFormat, StockQuality stockQuality) { }

    @Transient
    private final Map<ItemKey, Item> itemsMap;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    @Getter
    @Access(AccessType.PROPERTY)
    private List<Item> items;

    private void addItem(Item item) {
        ItemKey key = item.createItemKey();
        Item originalItem = itemsMap.get(key);

        if (originalItem == null) {
            itemsMap.put(key, item);
        } else {
            originalItem.addQuantity(item.getQuantity());
        }
    }

    public List<ShoppingCart.Item> removeAllItem(List<ItemKey> keys) {
        List<ShoppingCart.Item> removedItems = new ArrayList<>();

        for (ItemKey key: keys)
            removedItems.add(itemsMap.remove(key));

        items.clear();
        items.addAll(this.itemsMap.values());

        return removedItems;
    }

    public void setItems(List<Item> items) {
        this.itemsMap.clear();
        for (Item item: items) {
            addItem(item);
        }

        items.clear();
        items.addAll(this.itemsMap.values());

        this.items = items;
    }

    public ShoppingCart() {
        this.itemsMap = new HashMap<>();
        this.items = new ArrayList<>();
    }

    public static ShoppingCart createFor(String username) {
        ShoppingCart cart = new ShoppingCart();
        cart.setUsername(username);

        return cart;
    }

    @Version
    @Getter
    @Setter
    private long version;
}
