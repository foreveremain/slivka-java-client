package uk.ac.dundee.compbio.slivkaclient;

import javajs.http.HttpClientFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class SlivkaServiceRetrievalTest {

	private SlivkaClient client = null;
	private final HttpClientStub httpClient = new HttpClientStub();
	private static final URI slivkaUrl = URI.create("http://example.org/slivka/");
	private HttpClientStub.HttpRequestMock req = null;
	private HttpClientStub.HttpResponseMock rep = null;

	@BeforeClass
	public void setUpClass() {
		HttpClientFactory.setDefaultClassName(HttpClientStub.class.getName());
		client = SlivkaClient.newInstance(slivkaUrl);
		client.setHttpClient(httpClient);
	}

	@BeforeMethod
	public void setUp() {
		req = new HttpClientStub.HttpRequestMock();
		rep = new HttpClientStub.HttpResponseMock();
		rep.setStatusCode(200);
		rep.setHeader("Content-Type", "application/json");
		rep.setContentLocation("/response/services-list-body.json");
		req.setResponse(rep);
		httpClient.setRequests(List.of(req));
	}

	@AfterMethod
	public void tearDown() {
		try {
			rep.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Test(priority = -1)
	public void testServiceListRequest() {
		try {
			HttpClientStub.HttpRequestMock ourreq = req;
			synchronized (ourreq) {
				client.getServices();
				assertEquals(ourreq.getUri(), slivkaUrl.resolve("api/services"));
				assertEquals(ourreq.getMethod(), "GET");
			}

		} catch (IOException ioe) {
			fail("connection error", ioe);
		}
	}

	@Test
	public void testGetServicesList() {
		try {
			var services = client.getServices();
			var actual = services.stream().map(SlivkaService::getName).toArray();
			var expected = new String[] { "Example Service", "MUSCLE", "GlobPlot", "Jronn", "AACon", "TCoffee", "MAFFT",
					"ClustalO", "MSAProbs", "DisEMBL", "RNAalifold", "ProbCons", "JPred", "IUPred", "ClustalW" };
			assertEquals(actual, expected);
		} catch (IOException ioe) {
			fail("connection error", ioe);
		}
	}

	@Test
	public void testServiceLabel() {
		try {
			var service = client.getService("example");
			assertEquals(service.getId(), "example");
		} catch (IOException ioe) {
			fail("connection error", ioe);
		}
	}

	@Test
	public void testServiceClassifiers() {
		try {
			var service = client.getService("example");
			assertEquals(service.getClassifiers(), List.of("Topic=Example", "Category=Tutorials", "Show=False"));
		} catch (IOException ioe) {
			fail("connection error", ioe);
		}
	}

	@Test
	public void testServiceUrl() {
		try {
			var service = client.getService("example");
			assertEquals(service.getUrl(), slivkaUrl.resolve("api/services/example"));
		} catch (IOException ioe) {
			fail("connection error", ioe);
		}
	}

}
