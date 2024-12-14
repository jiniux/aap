package xyz.jiniux.aap.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

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

    private void addItem(Item item) {
        ItemKey key = item.createItemKey();
        Item originalItem = itemsMap.get(key);

        if (originalItem == null) {
            itemsMap.put(key, item);
        } else {
            originalItem.addQuantity(item.getQuantity());
        }
    }

    public List<ShoppingCart.Item> removeAllItems(List<ItemKey> keys) {
        List<ShoppingCart.Item> removedItems = new ArrayList<>();

        for (ItemKey key: keys)
            removedItems.add(itemsMap.remove(key));

        return removedItems;
    }

    public void setItems(List<Item> items) {
        itemsMap.clear();

        for (Item item: items) {
            addItem(item);
        }
    }

    public void merge(ShoppingCart newShoppingCart) {
        for (Item item: newShoppingCart.getItems()) {
            ItemKey key = item.createItemKey();

            if (!itemsMap.containsKey(key)) {
                addItem(item);
            } else {
                Item existingItem = itemsMap.get(key);
                existingItem.setQuantity(item.getQuantity());
            }
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
    @Getter
    @Setter
    private long version;
}
