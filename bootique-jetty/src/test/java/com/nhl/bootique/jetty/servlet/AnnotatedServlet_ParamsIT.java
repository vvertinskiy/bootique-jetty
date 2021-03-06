package com.nhl.bootique.jetty.servlet;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.nhl.bootique.jetty.JettyModule;
import com.nhl.bootique.jetty.unit.JettyApp;

public class AnnotatedServlet_ParamsIT {

	@Rule
	public JettyApp app = new JettyApp();

	@Test
	public void testAnnotationParams() throws Exception {
		app.startServer(new ServletModule());

		WebTarget base = ClientBuilder.newClient().target("http://localhost:8080");

		Response r = base.path("/b").request().get();
		assertEquals("p1_v1_p2_v2", r.readEntity(String.class));
	}

	@Test
	public void testConfig_Override() throws Exception {

		app.startServer(new ServletModule(),
				"--config=classpath:com/nhl/bootique/jetty/servlet/AnnotatedServletIT2.yml");

		WebTarget base = ClientBuilder.newClient().target("http://localhost:8080");

		Response r = base.path("/b").request().get();
		assertEquals("p1_v3_p2_v4", r.readEntity(String.class));
	}

	class ServletModule implements Module {

		@Override
		public void configure(Binder binder) {
			JettyModule.contributeServlets(binder).addBinding().to(AnnotatedServlet.class);
		}

		@Provides
		AnnotatedServlet createAnnotatedServlet() {
			return new AnnotatedServlet();
		}

		@WebServlet(name = "s1", urlPatterns = "/b/*", initParams = { @WebInitParam(name = "p1", value = "v1"),
				@WebInitParam(name = "p2", value = "v2") })
		class AnnotatedServlet extends HttpServlet {

			private static final long serialVersionUID = -8896839263652092254L;

			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp)
					throws ServletException, IOException {
				resp.getWriter().append("p1_" + getInitParameter("p1") + "_p2_" + getInitParameter("p2"));
			}
		}
	}

}
