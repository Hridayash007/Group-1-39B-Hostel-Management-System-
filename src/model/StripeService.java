package model;


import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import org.springframework.stereotype.Service;


@Service
public class StripeService {


public String createPayment(double amount)
throws Exception{


SessionCreateParams params =
SessionCreateParams.builder()

.setMode(
SessionCreateParams.Mode.PAYMENT
)


.setSuccessUrl(
"http://localhost:8080/success"
)


.setCancelUrl(
"http://localhost:8080/cancel"
)


.addLineItem(

SessionCreateParams.LineItem.builder()

.setQuantity(1L)


.setPriceData(

SessionCreateParams.LineItem.PriceData.builder()

.setCurrency("usd")

.setUnitAmount(
(long)(amount*100)
)


.setProductData(

SessionCreateParams.LineItem.PriceData.ProductData.builder()

.setName("Hostel Fee")

.build()

)

.build()

)

.build()

)

.build();



Session session =
Session.create(params);



return session.getUrl();

}


}