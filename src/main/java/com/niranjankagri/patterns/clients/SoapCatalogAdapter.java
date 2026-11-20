package com.niranjankagri.patterns.clients;

import com.niranjankagri.patterns.model.Product;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapts the shop's SOAP-style service ({@code POST /soap/catalog}) to {@link CatalogClient}: it wraps the
 * GetProducts operation in an envelope, reads the XML answer and translates its fields (code → id, title → name,
 * price in dollars → cents).
 */
public final class SoapCatalogAdapter implements CatalogClient {

    private static final String REQUEST = """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body><GetProducts/></soap:Body>
            </soap:Envelope>""";

    private final URI appUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    /** @param appUrl the shop's base URL */
    public SoapCatalogAdapter(URI appUrl) {
        this.appUrl = appUrl;
    }

    @Override
    public String name() {
        return "soap";
    }

    @Override
    public List<Product> products() {
        String xml = RestCatalogAdapter.Http.send(http, HttpRequest.newBuilder(URI.create(appUrl + "/soap/catalog"))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "GetProducts")
                .POST(HttpRequest.BodyPublishers.ofString(REQUEST)));
        NodeList nodes = parse(xml).getElementsByTagName("product");
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element p = (Element) nodes.item(i);
            products.add(new Product(text(p, "code"), text(p, "title"), cents(text(p, "price"))));
        }
        return List.copyOf(products);
    }

    /** "9.99" → 999, exactly (BigDecimal: no floating-point rounding). */
    static int cents(String dollars) {
        return new BigDecimal(dollars).movePointRight(2).intValueExact();
    }

    private static String text(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent().trim();
    }

    private static Document parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // No DTDs or external entities: an XML answer must never make the client read files or URLs (XXE)
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalStateException("The SOAP catalog answered invalid XML", e);
        }
    }
}
