package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

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
    void setUp() throws Exception {
        bookKeeper = new BookKeeper(factory);
        dummy = new ClientData( Id.generate(), SAMPLE_CLIENT_NAME);
        productBuilder = new ProductBuilder();
        requestItemBuilder = new RequestItemBuilder();
    }

    @Test
    void requestInvoiceWithOneItemShouldReturnInvoiceWithOneItem(){
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
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(300), "bookTax"));
        bookKeeper.issuance(request, taxPolicy);
        assertEquals(1,invoice.getItems().size());
    }

    @Test
    void requestInvoiceWithZeroItemsShouldReturnInvoiceWithZeroItems(){
        InvoiceRequest request = new InvoiceRequest(dummy);
        Invoice invoice = new Invoice(Id.generate(), dummy);
        when(factory.create(dummy)).thenReturn(invoice);
        bookKeeper.issuance(request, taxPolicy);
        assertEquals(0, invoice.getItems().size());
    }

    @Test
    void requestInvoiceWithHundredItemsShouldReturnInvoiceWithHundredItems(){
        InvoiceRequest request = new InvoiceRequest(dummy);
        Invoice invoice = new Invoice(Id.generate(), dummy);
        for(int i = 0; i<100; i++) {
            Product product = productBuilder.withPrice(new Money(i)).withName("chicken").withProductType(ProductType.FOOD).build();
            RequestItem requestItem = requestItemBuilder
                    .withProductData(product.generateSnapshot())
                    .withQuantity(i)
                    .withTotalCost(new Money(i, Money.DEFAULT_CURRENCY))
                    .build();
            request.add(requestItem);
        }
        when(factory.create(dummy)).thenReturn(invoice);
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(30), "bookTax"));
        bookKeeper.issuance(request, taxPolicy);
        assertEquals(100, invoice.getItems().size());
    }

}
