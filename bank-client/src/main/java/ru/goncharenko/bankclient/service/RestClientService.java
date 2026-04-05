package ru.goncharenko.bankclient.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnClass(RestClient.class)
@RequiredArgsConstructor
public class RestClientService {
	private final RestClient restClient;

	public <T> T getForObject(String url, Class<T> responseType) {
		return restClient.get()
				.uri(url)
				.retrieve()
				.body(responseType);
	}

	public <T> T getForObject(String url, ParameterizedTypeReference<T> responseType) {
		return restClient.get()
				.uri(url)
				.retrieve()
				.body(responseType);
	}

	public <T, R> T postForObject(String url, R requestBody, Class<T> responseType) {
		return restClient.post()
				.uri(url)
				.body(requestBody)
				.retrieve()
				.body(responseType);
	}
}
