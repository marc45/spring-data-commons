/*
 * Copyright 2015-2017 the original author or authors.
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
package org.springframework.data.web.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.data.web.XmlBeamHttpMessageConverter;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.instrument.classloading.ShadowingClassLoader;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

/**
 * @author Christoph Strobl
 */
public class SpringDataWebConfigurationUnitTests {

	@Test // DATACMNS-669R
	public void shouldNotAddQuerydslPredicateArgumentResolverWhenQuerydslNotPresent() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		ClassLoader classLoader = initClassLoader();

		Object config = classLoader.loadClass("org.springframework.data.web.config.SpringDataWebConfiguration")
				.newInstance();

		setField(config, "context",
				classLoader.loadClass("org.springframework.web.context.support.GenericWebApplicationContext").newInstance());
		setField(
				config,
				"conversionService",
				classLoader.loadClass(
						"org.springframework.data.web.config.SpringDataWebConfigurationUnitTests$ObjectFactoryImpl").newInstance());

		List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<HandlerMethodArgumentResolver>();

		invokeMethod(config, "addArgumentResolvers", argumentResolvers);

		assertThat(argumentResolvers,
				not(hasItem((Matcher) instanceWithClassName(QuerydslPredicateArgumentResolver.class))));
	}

	@Test // DATACOMNS-987
	public void shouldNotLoadJacksonConverterWhenJacksonNotPresent() {

	}

	@Test // DATACOMNS-987
	public void shouldNotLoadJacksonConverterWhenJawayNotPresent() {

	}

	@Test // DATACOMNS-987
	public void shouldNotLoadXBeamConverterWhenXBeamNotPresent() {

	}

	@Test // DATACOMNS-987
	public void shouldNotLoadAllConvertersWhenDependenciesArePresent() throws ClassNotFoundException,
			IllegalAccessException, InstantiationException {

		ClassLoader classLoader = initClassLoader();

		Object config = classLoader.loadClass("org.springframework.data.web.config.SpringDataWebConfiguration")
				.newInstance();

		setField(config, "context",
				classLoader.loadClass("org.springframework.web.context.support.GenericWebApplicationContext").newInstance());

		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();

		invokeMethod(config, "extendMessageConverters", converters);

		assertThat(converters, containsInAnyOrder( //
				instanceWithClassName(XmlBeamHttpMessageConverter.class), //
				instanceWithClassName(ProjectingJackson2HttpMessageConverter.class)));
	}

	/**
	 * creates a Matcher that check if an object is an instance of a class with the same name as the provided class.
	 *
	 * This is necessary since we are dealing with multiple classloaders which would make a simple instanceof fail all
	 * the time
	 *
	 * @param expectedClass
	 * @return
	 */
	private Matcher<Object> instanceWithClassName(Class<?> expectedClass) {
		return hasProperty("class", hasProperty("name", equalTo(expectedClass.getName())));
	}

	private ClassLoader initClassLoader() {

		ClassLoader classLoader = new ShadowingClassLoader(URLClassLoader.getSystemClassLoader()) {

			@Override
			public Class<?> loadClass(String name) throws ClassNotFoundException {

				if (name.startsWith("com.mysema")) {
					throw new ClassNotFoundException();
				}

				return super.loadClass(name);
			}

			@Override
			protected Class<?> findClass(String name) throws ClassNotFoundException {

				if (name.startsWith("com.mysema")) {
					throw new ClassNotFoundException();
				}

				return super.findClass(name);
			}
		};

		return classLoader;
	}

	public static class ObjectFactoryImpl implements ObjectFactory<ConversionService> {

		@Override
		public ConversionService getObject() throws BeansException {
			return null;
		}
	}
}
