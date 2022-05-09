package com.finalproyect;

import com.finalproyect.controllers.CheckoutController;
import com.finalproyect.controllers.OrderController;
import com.finalproyect.entities.*;
import com.finalproyect.model.dtos.*;
import com.finalproyect.model.exceptions.BadOrderException;
import com.finalproyect.model.exceptions.CheckoutNotFoundException;
import com.finalproyect.model.exceptions.LackOfStockException;
import com.finalproyect.model.exceptions.ProductNotFoundException;
import com.finalproyect.model.patterns.PaymentStrategiesEnum;
import com.finalproyect.repositories.*;
import com.finalproyect.services.KeycloakContextService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@SpringBootTest
class DemoApplicationTests {

    @MockBean
    KeycloakContextService keycloakContextService;

    @Autowired
    CheckoutController controllerApi;

    @Autowired
    OrderController orderController;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public ProductRepository productRepository;

    @Autowired
    public ShopppingCartRepository shopppingCartRepository;

    @Autowired
    public CountryRepostory countryRepostory;

    @Autowired
    public CityRepository cityRepository;

    @Autowired
    ShippingAddressRepository shippingAddressRepository;

    @Autowired
    public CheckoutRepository checkoutRepository;

    public ShippingAddress shippingAddress;

    public ShoppingCart shopppingCart;

    public Users usersRicardo;

    public Users usersWithCheckout;


    @BeforeEach
    public void setUp() {


        Country country = this.countryRepostory.save(new Country(null, "Argentina", "iso-3434"));

        City city = this.cityRepository.save(new City(null, "bs as", country));

        this.shippingAddress =this.shippingAddressRepository.save(new ShippingAddress(null, city, 1313));


        shopppingCart = this.shopppingCartRepository.save( new ShoppingCart());


        Checkout checkout = checkoutRepository.save(new Checkout(null, shopppingCart, shippingAddress, PaymentStrategiesEnum.PAYPAL));

        usersRicardo = new Users(null, "ricardo", "mail@mail.com", null,"yyy-zzz");

        usersWithCheckout = new Users(null, "juan", "otromail@mail.com", checkout,"jjj-zzz");

    }

    @AfterEach
    public void postTest(){
        this.userRepository.deleteAll();
        this.checkoutRepository.deleteAll();
    }


    @Test
    void test_successful_CreateCheckout() throws Exception {
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("yyy-zzz","ricardo",null,"mail@mail.com"));
        Users persistedUsers = this.userRepository.save(usersRicardo);
        CheckoutDto checkout = new CheckoutDto(shopppingCart, shippingAddress, PaymentStrategiesEnum.PAYPAL);
        ResponseEntity<CheckoutDto> responseEntity = controllerApi.createCheckout(checkout);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(responseEntity.getBody().getPaymentStrategiesEnum(), is(PaymentStrategiesEnum.PAYPAL));
    }

    @Test
    void test_successful_UpdateCheckout() throws Exception{
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("jjj-zzz","juan",null,"email@Random.com"));
        usersWithCheckout.setEmail("email@Random.com");
        Users persistedUsersWithCheckout = this.userRepository.save(usersWithCheckout);
        CheckoutDto checkoutDto=new CheckoutDto(null,null,PaymentStrategiesEnum.PAYONEER);
        ResponseEntity<CheckoutDto> responseEntity=this.controllerApi.updateCheckout(checkoutDto);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getPaymentStrategiesEnum(),is(PaymentStrategiesEnum.PAYONEER));
    }

    @Test
    void test_successful_addProductToTheCheckout(){
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("jjj-zzz","juan",null,"enserio@hotmail.com"));
        usersWithCheckout.setEmail("enserio@hotmail.com");
        Users persistedUsersWithCheckout =this.userRepository.save(usersWithCheckout);
        assertThat(persistedUsersWithCheckout.getCheckout().getShoppingCart().getProductsInShoppingCart().isEmpty(), is(true));
        Product productShoes =productRepository.save(new Product(null, "zapatos", "c123", 10,20D));
        ProductForPrucharase productForPrucharase= new ProductForPrucharase();
        productForPrucharase.setProductCode(productShoes.getProductCode());
        ProductDto productDto=new ProductDto(productShoes);
        ResponseEntity<CheckoutDto> responseEntity=this.controllerApi.addProduct(productDto);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getShoppingCart().getProductsInShoppingCart().contains(productForPrucharase), is(true));
    }

    @Test
    void test_successful_modifyQuantityOfProductInShoppingCart(){
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("jjj-zzz","juan",null,"funciona@hotmail.com"));
        this.userRepository.save(usersWithCheckout);
        Product productCoat =productRepository.save(new Product(null, "abrigo", "p222", 100,10D));
        ProductDto productForReserve=new ProductDto(productCoat);
        productForReserve.setQuantity(20);
        this.controllerApi.addProduct(productForReserve);
        productForReserve.setQuantity(10);
        ResponseEntity<CheckoutDto> responseEntity=this.controllerApi.addProduct(productForReserve);
        assertThat(responseEntity.getBody().getShoppingCart().getProductsInShoppingCart(), hasSize(1));
        assertThat(responseEntity.getBody().getShoppingCart().getProductsInShoppingCart().get(0).getQuantity(),is(30));
    }

    @Test
    void test_successful_printPrintCheckout(){
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("jjj-zzz","juan",null,"funciona@hotmail.com"));
        usersWithCheckout.setEmail("funciona@hotmail.com");
        Users users = this.userRepository.save(usersWithCheckout);
        ResponseEntity<UserDto> responseEntity=this.controllerApi.printCheckout();
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getCheckoutDto().getPaymentStrategiesEnum(), is(PaymentStrategiesEnum.PAYPAL));
    }

    @Test
    void test_successful_generateOrder(){
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("jjj-zzz","juan",null,"funciona@hotmail.com"));
        this.userRepository.save(usersWithCheckout);
        Product productShoes =productRepository.save(new Product(null, "zapatos", "a222", 10,10D));
        Product productHat =productRepository.save(new Product(null, "sombrero", "s222", 10,2D));
        this.controllerApi.addProduct(new ProductDto(productShoes));
        this.controllerApi.addProduct(new ProductDto(productHat));
        ResponseEntity<OrderDto> responseEntity=this.orderController.generateOrder();
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(responseEntity.getBody().getShoppingCart().getProductsInShoppingCart(),hasSize(2));
        assertThat(responseEntity.getBody().getTotalCost(), is(120D));
    }

    @Test
    void test_fail_addProduct_lackOfStock(){
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("jjj-zzz","juan",null,"yes@mail.com"));
        usersWithCheckout.setEmail("yes@mail.com");
        Users persistedUsersWithCheckout =this.userRepository.save(usersWithCheckout);
        assertThat(persistedUsersWithCheckout.getCheckout().getShoppingCart().getProductsInShoppingCart().isEmpty(), is(true));
        Product product =productRepository.save(new Product(null, "gorra", "h30", 10,20D));
        ProductDto productDto=new ProductDto(product);
        productDto.setQuantity(11);
        Assertions.assertThrows(LackOfStockException.class,()->this.controllerApi.addProduct(productDto));
    }

   /* @Test
    void test_fail_printCheckout_userDontExistInDB(){
        Assertions.assertThrows(UserNotFoundException.class,()->this.controllerApi.printCheckout());
    }
*/
    @Test
    void test_fail_addProduct_checkoutNotFound(){
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("jjj-zzz","ricardo",null,"mail@diferente.com"));
        usersRicardo.setEmail("mail@diferente.com");
        Product product =productRepository.save(new Product(null, "computadora", "atr123", 10,1D));
        ProductDto productDto= new ProductDto(product);
        Assertions.assertThrows(CheckoutNotFoundException.class,()->this.controllerApi.addProduct(productDto));
    }

    @Test
    void test_fail_addProduct_productNotFound(){
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("jjj-zzz","juan",null,"mailForTesting@mail"));
        this.usersWithCheckout.setEmail("mailForTesting@mail");
        Users persistedUsersWithCheckout =this.userRepository.save(usersWithCheckout);
        ProductDto productDto=new ProductDto();
        productDto.setProductCode("thisDoesNotExist");
        productDto.setQuantity(1);
        productDto.setName("zapatos");
        Assertions.assertThrows(ProductNotFoundException.class,()->this.controllerApi.addProduct(productDto));

    }

    @Test
    void test_fail_generateOrder_emptyShoppingCart(){
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("jjj-zzz","juan",null,"funciona@hotmail.com"));
        this.userRepository.save(usersWithCheckout);
        Assertions.assertThrows(BadOrderException.class,()->this.orderController.generateOrder());
    }

    @Test
    void fail_generateOrder_nullShippingAddress(){
        when(keycloakContextService.contextData()).thenReturn(new KeycloakUserDataDto("jjj-zzz","juan",null,"funciona@hotmail.com"));
        usersWithCheckout.getCheckout().setShippingAddress(null);
        this.checkoutRepository.save(usersWithCheckout.getCheckout());
        this.userRepository.save(usersWithCheckout);
        Product productShoes =productRepository.save(new Product(null, "zapatos", "a222", 10,10D));
        Product productHat =productRepository.save(new Product(null, "sombrero", "s222", 10,2D));
        this.controllerApi.addProduct(new ProductDto(productShoes));
        this.controllerApi.addProduct(new ProductDto(productHat));
        Assertions.assertThrows(BadOrderException.class,()->this.orderController.generateOrder());
    }



    @Test
    void test_successful_preloadData(){
        Optional<Users> optionalUser=this.userRepository.findById(12L);
        assertThat(optionalUser.isPresent(), is(true));
        Users users =optionalUser.get();
        assertThat(users.getName(), is("juan"));
        assertThat(users.getEmail(), is("miMail@hotmail"));
        assertThat(users.getCheckout().getPaymentStrategy(), is(PaymentStrategiesEnum.PAYPAL));
    }




}


