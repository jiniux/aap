package xyz.jiniux.aap.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import xyz.jiniux.aap.domain.cart.exceptions.ItemNotFoundInCartException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


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
    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"isbn", "stockFormat", "stockQuality", "quantity", "priceEur"})
    public static class Item implements Serializable {
        private final String isbn;
        private final StockFormat stockFormat;
        private final StockQuality stockQuality;

        @Setter
        private long quantity;

        @Setter
        private BigDecimal priceEur;

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
    @Access(AccessType.PROPERTY)
    private List<Item> items;

    public List<Item> getItems() {
        return itemsMap.values().stream().sorted(Comparator.comparing(Item::getIsbn)).collect(Collectors.toList());
    }

    public Item addItem(Item item) {
        ItemKey key = item.createItemKey();
        Item originalItem = itemsMap.get(key);

        if (originalItem == null) {
            itemsMap.put(key, item);
            return item;
        } else {
            originalItem.addQuantity(item.getQuantity());
            return originalItem;
        }
    }

    public Item updateItem(Item item) throws ItemNotFoundInCartException {
        ItemKey key = item.createItemKey();
        Item originalItem = itemsMap.get(key);

        if (originalItem == null) {
            throw new IllegalArgumentException("Item not found");
        } else {
            originalItem.setQuantity(item.getQuantity());
            return originalItem;
        }
    }

    public void removeItem(ItemKey key) {
        itemsMap.remove(key);
    }

    public void clear() {
        itemsMap.clear();
    }

    public List<ShoppingCart.Item> removeAllItems(List<ItemKey> keys) {
        List<ShoppingCart.Item> removedItems = new ArrayList<>();

        for (ItemKey key: keys) {
            Item item = itemsMap.remove(key);
            if (item != null) {
                removedItems.add(item);
            }
        }

        return removedItems;
    }

    public void setItems(List<Item> items) {
        itemsMap.clear();

        for (Item item: items) {
            addItem(item);
        }
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
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    @Getter
    public long version;
}
