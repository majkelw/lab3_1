package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookKeeperTest {

    private static final String SAMPLE_CLIENT_NAME = "Kowalski";
    @Mock
    private InvoiceFactory factory;
    @Mock
    private TaxPolicy taxPolicy;
    private BookKeeper bookKeeper;
    private ClientData dummy;
    private ProductBuilder productBuilder;
    private RequestItemBuilder requestItemBuilder;

    @BeforeEach
    void setUp() {
        bookKeeper = new BookKeeper(factory);
        dummy = new ClientData(Id.generate(), SAMPLE_CLIENT_NAME);
        productBuilder = new ProductBuilder();
        requestItemBuilder = new RequestItemBuilder();
    }

    //condition tests
    @Test
    void requestInvoiceWithOneItemShouldReturnInvoiceWithOneItem() {
        InvoiceRequest request = new InvoiceRequest(dummy);
        Invoice invoice = new Invoice(Id.generate(), dummy);
        Product product = productBuilder.withPrice(new Money(30)).withName("book").withProductType(ProductType.STANDARD).build();
        RequestItem requestItem = requestItemBuilder
                .withProductData(product.generateSnapshot())
                .withQuantity(10)
                .withTotalCost(new Money(300, Money.DEFAULT_CURRENCY))
                .build();

        request.add(requestItem);
        when(factory.create(dummy)).thenReturn(invoice);
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(300), "tax"));
        bookKeeper.issuance(request, taxPolicy);
        assertEquals(1, invoice.getItems().size());
    }

    @Test
    void requestInvoiceWithZeroItemsShouldReturnInvoiceWithZeroItems() {
        InvoiceRequest request = new InvoiceRequest(dummy);
        Invoice invoice = new Invoice(Id.generate(), dummy);
        when(factory.create(dummy)).thenReturn(invoice);
        bookKeeper.issuance(request, taxPolicy);
        assertEquals(0, invoice.getItems().size());
    }

    @Test
    void requestInvoiceWithHundredItemsShouldReturnInvoiceWithHundredItems() {
        InvoiceRequest request = new InvoiceRequest(dummy);
        Invoice invoice = new Invoice(Id.generate(), dummy);
        for (int i = 0; i < 100; i++) {
            Product product = productBuilder.withPrice(new Money(i)).withName("chicken").withProductType(ProductType.FOOD).build();
            RequestItem requestItem = requestItemBuilder
                    .withProductData(product.generateSnapshot())
                    .withQuantity(i)
                    .withTotalCost(new Money(i, Money.DEFAULT_CURRENCY))
                    .build();
            request.add(requestItem);
        }
        when(factory.create(dummy)).thenReturn(invoice);
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(30), "tax"));
        bookKeeper.issuance(request, taxPolicy);
        assertEquals(100, invoice.getItems().size());
    }

    //behavior tests
    @Test
    void requestInvoiceWithTwoItemsShouldCallCalculateTaxTwice() {
        Invoice invoice = new Invoice(Id.generate(), dummy);
        when(factory.create(dummy)).thenReturn(invoice);
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(300), "tax"));
        InvoiceRequest request = new InvoiceRequest(dummy);
        ProductBuilder productBuilder = new ProductBuilder();

        Product product = productBuilder.withPrice(new Money(30)).withName("book").build();
        Product product2 = productBuilder.withPrice(new Money(10)).withName("cake").withProductType(ProductType.FOOD).build();

        RequestItem requestItem = requestItemBuilder
                .withProductData(product.generateSnapshot())
                .withTotalCost(new Money(30))
                .build();
        RequestItem requestItem2 = requestItemBuilder
                .withProductData(product2.generateSnapshot())
                .withQuantity(10)
                .withTotalCost(new Money(100))
                .build();

        request.add(requestItem);
        request.add(requestItem2);
        bookKeeper.issuance(request, taxPolicy);
        verify(taxPolicy, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    void requestInvoiceWithZeroItemsShouldNotCallCalculateTax() {
        Invoice invoice = new Invoice(Id.generate(), dummy);
        when(factory.create(dummy)).thenReturn(invoice);
        InvoiceRequest request = new InvoiceRequest(dummy);
        bookKeeper.issuance(request, taxPolicy);
        verify(taxPolicy, times(0)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    void requestInvoiceWithHundredItemsShouldCallCalculateTaxHundredTimes() {
        Invoice invoice = new Invoice(Id.generate(), dummy);
        when(factory.create(dummy)).thenReturn(invoice);
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(300), "tax"));
        InvoiceRequest request = new InvoiceRequest(dummy);
        ProductBuilder productBuilder = new ProductBuilder();
        Product product = productBuilder.withPrice(new Money(30)).withName("book").build();
        Product product2 = productBuilder.withPrice(new Money(10)).withName("cake").withProductType(ProductType.FOOD).build();
        RequestItem requestItem = requestItemBuilder
                .withProductData(product.generateSnapshot())
                .withTotalCost(new Money(30))
                .build();
        RequestItem requestItem2 = requestItemBuilder
                .withProductData(product2.generateSnapshot())
                .withQuantity(10)
                .withTotalCost(new Money(100))
                .build();
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0)
                request.add(requestItem);
            else
                request.add(requestItem2);
        }
        bookKeeper.issuance(request, taxPolicy);
        verify(taxPolicy, times(100)).calculateTax(any(ProductType.class), any(Money.class));
    }

}
