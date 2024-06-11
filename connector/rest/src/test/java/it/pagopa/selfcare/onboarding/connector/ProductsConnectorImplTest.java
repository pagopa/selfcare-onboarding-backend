package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.product.entity.ContractStorage;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductStatus;
import it.pagopa.selfcare.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ProductsConnectorImplTest {
    @InjectMocks
    private ProductsConnectorImpl productConnector;
    @Mock
    private ProductService productService;

    @Test
    void getProductByInstitutionType() {
        final Product product = dummyProduct();
        ContractStorage contractStorage = new ContractStorage();
        contractStorage.setContractTemplateUpdatedAt(Instant.now());
        contractStorage.setContractTemplatePath("contractTemplatePath");
        contractStorage.setContractTemplateVersion("contractTemplateVersion");
        final Map<InstitutionType, ContractStorage> contractStorageMap = Map.of(InstitutionType.PA,contractStorage);
        product.setInstitutionContractMappings(contractStorageMap);
        final String productId = "productId";
        when(productService.getProduct(anyString())).thenReturn(product);

        Product result = productConnector.getProduct(productId, InstitutionType.PA);

        assertEquals(product, result);

        verify(productService, times(1)).getProduct(productId);

    }

    @Test
    void getProduct_institutionTypeNotPresent(){
        final Product product = dummyProduct();
        ContractStorage contractStorage = new ContractStorage();
        contractStorage.setContractTemplateUpdatedAt(Instant.now());
        contractStorage.setContractTemplatePath("contractTemplatePath");
        contractStorage.setContractTemplateVersion("contractTemplateVersion");
        final Map<InstitutionType, ContractStorage> contractStorageMap = Map.of(InstitutionType.PA,contractStorage);
        product.setInstitutionContractMappings(contractStorageMap);
        final String productId = "productId";
        when(productService.getProduct(anyString())).thenReturn(product);

        Product result = productConnector.getProduct(productId, InstitutionType.SA);

        assertEquals(product, result);

        verify(productService, times(1)).getProduct(productId);

    }

    @Test
    void testGetProductValid() {
        Product product = dummyProduct();
        when(productService.getProductIsValid(any())).thenReturn(product);
        assertSame(product, productConnector.getProductValid("42"));
        verify(productService).getProductIsValid(any());
    }

    private Product dummyProduct(){
        Product product = new Product();
        product.setContractTemplatePath("Contract Template Path");
        product.setContractTemplateVersion("1.0.2");
        product.setId("42");
        product.setParentId("42");
        product.setRoleMappings(null);
        product.setStatus(ProductStatus.ACTIVE);
        product.setTitle("Dr");
        return product;
    }
}
