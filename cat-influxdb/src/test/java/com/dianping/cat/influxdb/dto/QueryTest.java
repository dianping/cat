package com.dianping.cat.influxdb.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/**
 * Test for the Query DTO.
 *
 * @author jord [at] moz.com
 *
 */
public class QueryTest {

	/**
	 * Test that equals does what it is supposed to do.
	 */
	@Test
	public void testEqualsAndHashCode() {
		String stringA0 = "thesame";
		String stringA1 = "thesame";
		String stringB0 = "notthesame";

		Query queryA0 = new Query(stringA0, stringA0);
		Query queryA1 = new Query(stringA1, stringA1);
		Query queryB0 = new Query(stringA0, stringB0);
		Query queryC0 = new Query(stringB0, stringA0);

		assertThat(queryA0).isEqualTo(queryA1);
		assertThat(queryA0).isNotEqualTo(queryB0);
		assertThat(queryB0).isNotEqualTo(queryC0);

		assertThat(queryA0.hashCode()).isEqualTo(queryA1.hashCode());
		assertThat(queryA0.hashCode()).isNotEqualTo(queryB0.hashCode());
	}
}
