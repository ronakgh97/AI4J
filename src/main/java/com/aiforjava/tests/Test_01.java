package com.aiforjava.tests;

import com.aiforjava.demo.Welcome;
import com.aiforjava.exception.LLMServiceException;

public class Test_01 extends Thread{
    public static void main(String[] args) throws LLMServiceException {
        System.out.print(Welcome.generateWelcomeMessage());
    }
}
