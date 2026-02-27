/*
 * Copyright 2002-2017 the original author or authors.
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

package com.dianping.cat.apiguardian.api;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * {@code @API} is used to annotate public types, methods, constructors, and
 * fields within a framework or application in order to publish their
 * {@link #status} and level of stability and to indicate how they are intended
 * to be used by {@link #consumers} of the API.
 *
 * <p>If {@code @API} is present on a type, it is considered to hold for all
 * public members of the type as well. However, a member of such an annotated
 * type is allowed to declare a {@link Status} of lower stability. For example,
 * a class annotated with {@code @API(status = STABLE)} may declare a constructor
 * for internal usage that is annotated with {@code @API(status = INTERNAL)}.
 *
 * <p>If {@code @API} is present on a package, it is considered to hold for all
 * public types in its package. The same rules for lowered stability apply as
 * if they were specified on a type.
 *
 * @since 1.0
 */
@Target({ TYPE, METHOD, CONSTRUCTOR, FIELD, PACKAGE })
@Retention(RUNTIME)
@Documented
public @interface API {

	/**
	 * The current {@linkplain Status status} of the API.
	 */
	Status status();

	/**
	 * The version of the API when the {@link #status} was last changed.
	 *
	 * <p>Defaults to an empty string, signifying that the <em>since</em>
	 * version is unknown.
	 */
	String since() default "";

	/**
	 * List of packages belonging to intended consumers.
	 *
	 * <p>The supplied packages can be fully qualified package names or
	 * patterns containing asterisks that will be used as wildcards.
	 *
	 * <p>Defaults to {@code "*"}, signifying that the API is intended to be
	 * consumed by any package.
	 */
	String[] consumers() default "*";

	/**
	 * Indicates the status of an API element and therefore its level of
	 * stability as well.
	 */
	enum Status {

		/**
		 * Must not be used by any external code. Might be removed without prior
		 * notice.
		 */
		INTERNAL,

		/**
		 * Should no longer be used. Might disappear in the next minor release.
		 *
		 * <p>This status is usually used in combination with the standard annotation
		 * {@link Deprecated @Deprecated} because that annotation is recognized by
		 * IDEs and the compiler. However, there are also cases where this status
		 * can be used on its own, for example when transitioning a {@link #MAINTAINED}
		 * feature to an {@link #INTERNAL} one.
		 */
		DEPRECATED,

		/**
		 * Intended for new, experimental features where the publisher of the
		 * API is looking for feedback.
		 *
		 * <p>Use with caution. Might be promoted to {@link #MAINTAINED} or
		 * {@link #STABLE} in the future, but might also be removed without
		 * prior notice.
		 */
		EXPERIMENTAL,

		/**
		 * Intended for features that will not be changed in a backwards-incompatible
		 * way for at least the next minor release of the current major version.
		 * If scheduled for removal, such a feature will be demoted to
		 * {@link #DEPRECATED} first.
		 */
		MAINTAINED,

		/**
		 * Intended for features that will not be changed in a backwards-incompatible
		 * way in the current major version.
		 */
		STABLE;

	}

}
