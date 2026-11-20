package com.niranjankagri.patterns.model;

/**
 * A product as the tests see it, whatever format it came from (prices always in cents, so no rounding).
 *
 * @param id         the shop's product code, for example P-100
 * @param name       the display name, for example "Bike Light"
 * @param priceCents the price in cents, for example 999
 */
public record Product(String id, String name, int priceCents) {

    /** The price as the shop shows it, for example "$9.99". */
    public String displayPrice() {
        return "$%d.%02d".formatted(priceCents / 100, priceCents % 100);
    }
}
