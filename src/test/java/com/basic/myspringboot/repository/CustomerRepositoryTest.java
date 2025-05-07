package com.basic.myspringboot.repository;

import com.basic.myspringboot.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerRepositoryTest {
    @Autowired
    CustomerRepository customerRepository;

    @Test
    void testCreateCustomer() {
        //Given (준비 단계)
        Customer customer = new Customer();
        customer.setCustomerId("A001");
        customer.setCustomerName("스프링");
        //When (실행 단계)
        //Then (검증 단계)
    }
}