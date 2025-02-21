package xyz.jiniux.aap.domain.cart;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

public sealed class ShoppingCartUpdate {
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static final class AddItem extends ShoppingCartUpdate {
        private final String isbn;
        private final StockFormat stockFormat;
        private final StockQuality stockQuality;
        private final long quantity;

        public AddItem(String isbn, StockFormat stockFormat, StockQuality stockQuality, long quantity) {
            this.isbn = isbn;
            this.stockFormat = stockFormat;
            this.stockQuality = stockQuality;

            if (quantity <= 0)
                throw new IllegalArgumentException("Quantity must be greater than 0");

            this.quantity = quantity;
        }

        public ShoppingCart.Item toItem() {
            ShoppingCart.Item item = new ShoppingCart.Item(isbn, stockFormat, stockQuality);
            item.setQuantity(quantity);

            return item;
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static final class UpdateItem extends ShoppingCartUpdate {
        private final String isbn;
        private final StockFormat stockFormat;
        private final StockQuality stockQuality;
        private final long quantity;

        public UpdateItem(String isbn, StockFormat stockFormat, StockQuality stockQuality, long quantity) {
            this.isbn = isbn;
            this.stockFormat = stockFormat;
            this.stockQuality = stockQuality;

            if (quantity <= 0)
                throw new IllegalArgumentException("Quantity must be greater than 0");

            this.quantity = quantity;
        }

        public ShoppingCart.Item toItem() {
            ShoppingCart.Item item = new ShoppingCart.Item(isbn, stockFormat, stockQuality);
            item.setQuantity(quantity);

            return item;
        }
    }

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static final class RemoveItem extends ShoppingCartUpdate {
        private String isbn;
        private StockFormat stockFormat;
        private StockQuality stockQuality;

        public ShoppingCart.ItemKey toItemKey() {
            return new ShoppingCart.ItemKey(isbn, stockFormat, stockQuality);
        }
    }
}
