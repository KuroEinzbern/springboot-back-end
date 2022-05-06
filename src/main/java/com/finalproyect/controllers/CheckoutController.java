package com.finalproyect.controllers;


import com.finalproyect.entities.Checkout;
import com.finalproyect.entities.User;
import com.finalproyect.exceptions.CheckoutNotFoundException;
import com.finalproyect.model.dtos.CheckoutDto;
import com.finalproyect.model.dtos.ProductDto;
import com.finalproyect.model.dtos.UserDto;
import com.finalproyect.services.CheckoutService;
import com.finalproyect.services.PaymentService;
import com.finalproyect.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;

@RestController
public class CheckoutController {

    @Autowired
    CheckoutService checkoutService;

    @Autowired
    UserService userService;

    @Autowired
    PaymentService paymentService;


    //@RolesAllowed("user")
    @PostMapping("/checkout/newcheckout")
    public ResponseEntity<CheckoutDto> createCheckout(@RequestBody CheckoutDto checkoutDto){
        User user= this.userService.retrieveUserById(checkoutDto.getId());
        Checkout newCheckout=checkoutService.createCheckout(checkoutDto,user);
        return new ResponseEntity<>(new CheckoutDto(newCheckout), HttpStatus.CREATED);
    }

    @RolesAllowed("user")
    @PatchMapping("/checkout/updatecheckoutinfo/{id}")
    public ResponseEntity<CheckoutDto> updateCheckout(@PathVariable("id") Long userId, @RequestBody CheckoutDto checkoutDto){
        User user= this.userService.retrieveUserById(userId);
        Checkout updatedCheckout=this.checkoutService.updateCheckout(checkoutDto, user.getCheckout());
        return new ResponseEntity<>(new CheckoutDto(updatedCheckout),HttpStatus.OK);
    }

    @RolesAllowed("user")
    @PatchMapping("/checkout/addproducts/{id}")
    public ResponseEntity<CheckoutDto> addProduct(@PathVariable("id") Long userId, @RequestBody ProductDto productDto){
        User user= this.userService.retrieveUserById(userId);
        Checkout currentCheckout= user.getCheckout();
        if(currentCheckout!=null) {
            this.checkoutService.addProduct(productDto, currentCheckout);
            return new ResponseEntity<>(new CheckoutDto(currentCheckout), HttpStatus.OK);
        }
        else {
            throw new CheckoutNotFoundException("el usuario" + user.getEmail() + "con id" + userId+ "no tiene ninguna orden a su nombre");
        }
    }

    @RolesAllowed("user")
    @GetMapping("/checkout/printcheckout/{id}")
    public ResponseEntity<UserDto> printCheckout(@PathVariable("id") Long userId){
        User user= this.userService.retrieveUserById(userId);
        return new ResponseEntity<>(new UserDto(user), HttpStatus.OK);
    }

    @RolesAllowed("user")
    @GetMapping("/saludar")
    public String printCheckout(){
        return "hola";
    }
}