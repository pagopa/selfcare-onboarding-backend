package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductsConnector productsConnectorMock;


    @Test
    void getProduct_nullProductId() {
        //given
        String productId = null;
        InstitutionType institutionType = InstitutionType.PA;
        //when
        Executable executable = () -> productService.getProduct(productId, institutionType);
        //then
        Exception e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("ProductId is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getProduct_InstitutionTypeNotNull() {
        //given
        String productId = "productId";
        InstitutionType institutionType = InstitutionType.PA;
        Product productMock = Mockito.mock(Product.class);
        Mockito.when(productsConnectorMock.getProduct(productId, institutionType))
                .thenReturn(productMock);
        //when
        Product product = productService.getProduct(productId, institutionType);
        //then
        Assertions.assertSame(productMock, product);
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(productId, institutionType);
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
    }

    @Test
    void getProduct_institutionTypeNull() {
        //given
        String productId = "productId";
        InstitutionType institutionType = null;
        Product productMock = Mockito.mock(Product.class);
        Mockito.when(productsConnectorMock.getProduct(productId, institutionType))
                .thenReturn(productMock);
        //when
        Product product = productService.getProduct(productId, institutionType);
        //then
        Assertions.assertSame(productMock, product);
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(productId, institutionType);
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
    }

}
