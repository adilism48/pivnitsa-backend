package kg.megalab.pivnitsabackend.payment;

import kg.megalab.pivnitsabackend.payment.dto.payments.PaymentWebhookEvent;
import kg.megalab.pivnitsabackend.payment.dto.payments.CreatePaymentCommand;
import kg.megalab.pivnitsabackend.payment.dto.payments.CreatePaymentResult;
import kg.megalab.pivnitsabackend.payment.dto.payments.ProviderPaymentResult;
import kg.megalab.pivnitsabackend.payment.dto.refund.RefundCommand;
import kg.megalab.pivnitsabackend.payment.dto.refund.RefundResult;

import java.util.Map;

public interface PaymentProvider {

    String getCode();

    CreatePaymentResult createPayment(CreatePaymentCommand command);

    ProviderPaymentResult getPayment(String providerPaymentId);

    RefundResult refund(RefundCommand command);

    PaymentWebhookEvent verifyAndParseWebhook(
            Map<String, String> headers,
            byte[] rawBody
    );
}
