package com.yas.inventory.service;

import com.yas.inventory.config.ServiceUrlConfig;
import com.yas.inventory.model.enumeration.FilterExistInWHSelection;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final WebClient webClient;
    private final ServiceUrlConfig serviceUrlConfig;

    public ProductService(WebClient webClient, ServiceUrlConfig serviceUrlConfig) {
        this.webClient = webClient;
        this.serviceUrlConfig = serviceUrlConfig;
    }

    public ProductInfoVm getProduct(Long id) {
        final URI url = UriComponentsBuilder
                .fromHttpUrl(serviceUrlConfig.product())
                .path("/backoffice/products/" + id)
                .build()
                .toUri();
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(ProductInfoVm.class)
                .single()
                .block();
    }

    public List<ProductInfoVm> filterProducts(String productName, String productSku,
                                          List<Long> productIds, FilterExistInWHSelection selection) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("name", productName);
        params.add("sku", productSku);
        params.add("selection", selection.name());
        if (!CollectionUtils.isEmpty(productIds)) {
            params.add("productIds", productIds.stream().map(Object::toString).collect(Collectors.joining(",")));
        }

        final URI url = UriComponentsBuilder
                .fromHttpUrl(serviceUrlConfig.product())
                .path("/backoffice/products/for-warehouse")
                .queryParams(params)
                .build()
                .toUri();
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(ProductInfoVm.class)
                .collectList()
                .block();
    }
}