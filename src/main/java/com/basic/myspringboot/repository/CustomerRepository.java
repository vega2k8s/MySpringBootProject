package com.basic.myspringboot.repository;

import com.basic.myspringboot.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
}
