package com.basic.myspringboot.runner;

import com.basic.myspringboot.config.CustomerVO;
import com.basic.myspringboot.property.MyBootProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MyRunner implements ApplicationRunner {
    @Value("${myboot.name}")
    private String name;

    @Value("${myboot.age}")
    private int age;

    @Autowired
    private Environment environment;

    @Autowired
    private MyBootProperties properties;

    @Autowired
    private CustomerVO customerVO;

    private Logger logger = LoggerFactory.getLogger(MyRunner.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {

        System.out.println("${myboot.name} = " + name);
        System.out.println("${myboot.age} = " + age);
        System.out.println("${myboot.fullName} = " + environment.getProperty("myboot.fullName"));

        System.out.println("MyBootProperties getName() " + properties.getName());
        System.out.println("MyBootProperties getAge() " + properties.getAge());
        System.out.println("MyBootProperties getFullName() " + properties.getFullName());
        System.out.println("설정된 Port 번호 " + environment.getProperty("local.server.port") );

        System.out.println("현재 활성화된 CustomerVO Bean " + customerVO);

        // foo 라는 VM 아규먼트 있는지 확인
        System.out.println("VM 아규먼트 foo : " + args.containsOption("foo"));
        // bar 라는 Program 아규먼트 있는지 확인
        System.out.println("Program 아규먼트 bar : " + args.containsOption("bar"));

        /*
            Iterable forEach(Consumer)
            Consumer 는 함수형 인터페이스 void accept(T t)
            Consumer 의 추상메서드를 오버라이딩 하는 구문을 람다식으로 작성
         */
        // Program 아규먼트 목록 출력
        args.getOptionNames()  //Set<String>
                .forEach(name -> System.out.println(name));

    }//run
}//class
