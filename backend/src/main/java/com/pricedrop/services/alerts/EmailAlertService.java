package com.pricedrop.services.alerts;

import io.vertx.core.Future;
import io.vertx.core.Promise;

public interface EmailAlertService {
    Future<Void> sendEmail(String subject, String to, String body);
}
