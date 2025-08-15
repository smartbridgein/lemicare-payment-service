package com.lemicare.payment.service.config;

import com.cosmicdoc.common.repository.PaymentOrderRepository;
import com.cosmicdoc.common.repository.impl.PaymentOrderRepositoryImpl;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EntityConfiguration {

    @Bean
    PaymentOrderRepository paymentOrderRepository (Firestore firestore) {
        return new PaymentOrderRepositoryImpl(firestore);
    }

}
