package kg.megalab.pivnitsabackend.payment;

import kg.megalab.pivnitsabackend.exception.paymentexception.PaymentProviderException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaymentProviderRegistry {

    private final Map<String, PaymentProvider> providers;

    public PaymentProviderRegistry(List<PaymentProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        provider -> normalize(provider.getCode()),
                        Function.identity()
                ));
    }

    public PaymentProvider getRequired(String code) {
        PaymentProvider provider = providers.get(normalize(code));

        if (provider == null) {
            throw new PaymentProviderException(
                    "Unsupported payment provider: " + code,
                    code,
                    "PROVIDER_NOT_CONFIGURED"
            );
        }

        return provider;
    }

    private static String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new PaymentProviderException(
                    "Payment provider is required",
                    null,
                    "PROVIDER_REQUIRED"
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }
}
