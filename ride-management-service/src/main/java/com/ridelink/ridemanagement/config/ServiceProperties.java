/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package com.ridelink.ridemanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="services")
public class ServiceProperties {
    private ServiceEndpoint account = new ServiceEndpoint("http://localhost:8081", 3000, 5000);
    private ServiceEndpoint driver = new ServiceEndpoint("http://localhost:8082", 3000, 5000);
    private ServiceEndpoint payment = new ServiceEndpoint("http://localhost:8084", 3000, 5000);

    public ServiceEndpoint getAccount() {
        return this.account;
    }

    public void setAccount(ServiceEndpoint account) {
        this.account = account;
    }

    public ServiceEndpoint getDriver() {
        return this.driver;
    }

    public void setDriver(ServiceEndpoint driver) {
        this.driver = driver;
    }

    public ServiceEndpoint getPayment() {
        return this.payment;
    }

    public void setPayment(ServiceEndpoint payment) {
        this.payment = payment;
    }

    public static class ServiceEndpoint {
        private String url;
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 5000;

        public ServiceEndpoint() {
        }

        public ServiceEndpoint(String url, int connectTimeoutMs, int readTimeoutMs) {
            this.url = url;
            this.connectTimeoutMs = connectTimeoutMs;
            this.readTimeoutMs = readTimeoutMs;
        }

        public String getUrl() {
            return this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getConnectTimeoutMs() {
            return this.connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return this.readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }
}

