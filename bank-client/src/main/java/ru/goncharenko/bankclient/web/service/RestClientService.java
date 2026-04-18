package ru.goncharenko.bankclient.web.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.goncharenko.bankclient.common.utils.BearerAuthResolver;

@Service
@ConditionalOnClass(RestClient.class)
public class RestClientService {
	private final RestClient loadBalancedRestClient;

	public RestClientService(@Qualifier("loadBalancedRestClient") RestClient loadBalancedRestClient) {
		this.loadBalancedRestClient = loadBalancedRestClient;
	}

	public <T> T getForObject(String url, Class<T> responseType) {
		return loadBalancedRestClient.get()
				.uri(url)
				.headers(BearerAuthResolver::setAuthHeader)
				.retrieve()
				.body(responseType);
	}

	public <T> T getForObject(String url, ParameterizedTypeReference<T> responseType) {
		return loadBalancedRestClient.get()
				.uri(url)
				.headers(BearerAuthResolver::setAuthHeader)
				.retrieve()
				.body(responseType);
	}

	public <T, R> T postForObject(String url, R requestBody, Class<T> responseType) {
		return loadBalancedRestClient.post()
				.uri(url)
				.headers(BearerAuthResolver::setAuthHeader)
				.body(requestBody)
				.retrieve()
				.body(responseType);
	}
}
