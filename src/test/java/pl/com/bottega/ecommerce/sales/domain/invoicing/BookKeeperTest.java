package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    private ProductBuilder productBuilder;
    @BeforeEach
    void setUp() throws Exception {
        bookKeeper = new BookKeeper(factory);
        ProductBuilder productBuilder = new ProductBuilder();
    }

    @Test
    void requestInvoiceWithOneItemShouldReturnInvoiceWithOneItem(){
        Id sampleId = Id.generate();
        ClientData dummy = new ClientData(sampleId, SAMPLE_CLIENT_NAME);
        InvoiceRequest request = new InvoiceRequest(dummy);
        Invoice invoice = new Invoice(Id.generate(), dummy);
        Product product = productBuilder.withPrice(new Money(30)).withName("book").withProductType(ProductType.STANDARD).build();
        RequestItem requestItem = new RequestItem(product.generateSnapshot(),10, new Money(300, Money.DEFAULT_CURRENCY));
        request.add(requestItem);
        when(factory.create(dummy)).thenReturn(invoice);
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(300), "bookTax"));
        Invoice issuance = bookKeeper.issuance(request, taxPolicy);
        assertTrue(nonNull(issuance));
        assertEquals(invoice, issuance);
        assertEquals(1,invoice.getItems().size());
    }

}
