/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DelegatingEntityLinks;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for {@link EnableHypermediaSupport}.
 * 
 * @author Oliver Gierke
 */
@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class EnableHypermediaSupportIntegrationTest {

	@Test
	@SuppressWarnings({ "unchecked" })
	public void bootstrapHalConfiguration() {

		ApplicationContext context = new AnnotationConfigApplicationContext(HalConfig.class);
		assertEntityLinksSetUp(context);
		assertThat(context.getBean(LinkDiscoverer.class), is(instanceOf(HalLinkDiscoverer.class)));
		assertThat(context.getBean(ObjectMapper.class), is(notNullValue()));

		RequestMappingHandlerAdapter rmha = context.getBean(RequestMappingHandlerAdapter.class);
		assertThat(rmha.getMessageConverters(), Matchers.<HttpMessageConverter<?>> hasItems(
				instanceOf(MappingJackson2HttpMessageConverter.class), instanceOf(MappingJacksonHttpMessageConverter.class)));

		AnnotationMethodHandlerAdapter amha = context.getBean(AnnotationMethodHandlerAdapter.class);
		assertThat(Arrays.asList(amha.getMessageConverters()), Matchers.<HttpMessageConverter<?>> hasItems(
				instanceOf(MappingJackson2HttpMessageConverter.class), instanceOf(MappingJacksonHttpMessageConverter.class)));
	}

	@Test
	public void registersLinkDiscoverers() {

		ApplicationContext context = new AnnotationConfigApplicationContext(HalConfig.class);
		LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

		assertThat(discoverers, is(notNullValue()));
		assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON), is(instanceOf(HalLinkDiscoverer.class)));
		assertRelProvidersSetUp(context);
	}

	private static void assertEntityLinksSetUp(ApplicationContext context) {

		Map<String, EntityLinks> discoverers = context.getBeansOfType(EntityLinks.class);
		assertThat(discoverers.values(), Matchers.<EntityLinks> hasItem(instanceOf(DelegatingEntityLinks.class)));
	}

	private static void assertRelProvidersSetUp(ApplicationContext context) {

		Map<String, RelProvider> discoverers = context.getBeansOfType(RelProvider.class);
		assertThat(discoverers.values(), Matchers.<RelProvider> hasItem(instanceOf(DelegatingRelProvider.class)));
	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class HalConfig {

		static int numberOfMessageConverters = 0;
		static int numberOfMessageConvertersLegacy = 0;

		@Bean
		public RequestMappingHandlerAdapter rmh() {
			RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
			numberOfMessageConverters = adapter.getMessageConverters().size();
			return adapter;
		}

		@Bean
		public AnnotationMethodHandlerAdapter amha() {
			AnnotationMethodHandlerAdapter adapter = new AnnotationMethodHandlerAdapter();
			numberOfMessageConvertersLegacy = adapter.getMessageConverters().length;
			return adapter;
		}
	}
}
