package com.nhl.bootique.jetty;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.nhl.bootique.jetty.unit.JettyApp;

public class FilterInitParametersIT {

	@Rule
	public JettyApp app = new JettyApp();

	private MappedServlet endOfChainServlet;

	@Before
	public void before() {
		Servlet mockServlet = mock(Servlet.class);
		endOfChainServlet = new MappedServlet(mockServlet, new HashSet<>(Arrays.asList("/*")));
	}

	@Test
	public void testInitParametersPassed() {

		Map<String, String> params = new HashMap<>();
		params.put("a", "af1");
		params.put("b", "bf2");

		MappedFilter mf = new MappedFilter(new TestFilter(), Collections.singleton("/*"), "f1", 5);

		app.startServer(binder -> {
			JettyModule.contributeMappedFilters(binder).addBinding().toInstance(mf);
			JettyModule.contributeMappedServlets(binder).addBinding().toInstance(endOfChainServlet);
		} , "--config=src/test/resources/com/nhl/bootique/jetty/FilterInitParametersIT.yml");

		WebTarget base = ClientBuilder.newClient().target("http://localhost:8080");

		Response r1 = base.path("/").request().get();
		assertEquals(Status.OK.getStatusCode(), r1.getStatus());

		assertEquals("f1_af1_bf2", r1.readEntity(String.class));
	}

	static class TestFilter implements Filter {

		private FilterConfig config;

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			this.config = filterConfig;
		}

		@Override
		public void destroy() {
			// do nothing...
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {

			HttpServletResponse resp = (HttpServletResponse) response;

			resp.setContentType("text/plain");

			resp.getWriter().print(config.getFilterName());
			resp.getWriter().print("_" + config.getInitParameter("a"));
			resp.getWriter().print("_" + config.getInitParameter("b"));
		}
	}

}
