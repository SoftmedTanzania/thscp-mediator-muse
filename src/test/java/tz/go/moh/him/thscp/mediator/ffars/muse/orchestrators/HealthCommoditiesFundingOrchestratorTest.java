package tz.go.moh.him.thscp.mediator.ffars.muse.orchestrators;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.testing.MockHTTPConnector;
import org.openhim.mediator.engine.testing.MockLauncher;
import org.openhim.mediator.engine.testing.TestingUtils;
import tz.go.moh.him.thscp.mediator.ffars.muse.domain.Indicator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Represents unit tests for the {@link HealthCommoditiesFundingOrchestrator} class.
 */
public class HealthCommoditiesFundingOrchestratorTest extends BaseOrchestratorTest {

    /**
     * Runs initialization before test execution.
     */
    @Before
    public void before() throws Exception {
        super.before();

        List<MockLauncher.ActorToLaunch> toLaunch = new LinkedList<>();
        toLaunch.add(new MockLauncher.ActorToLaunch("http-connector", MockThscp.class));
        TestingUtils.launchActors(system, configuration.getName(), toLaunch);
    }

    /**
     * Performs health commodities funding HTTP request test
     */
    @Test
    public void testHealthCommoditiesFundingHTTPRequest() throws Exception {
        assertNotNull(configuration);

        new JavaTestKit(system) {{
            final ActorRef orchestrator = system.actorOf(Props.create(HealthCommoditiesFundingOrchestrator.class, configuration));

            InputStream stream = HealthCommoditiesFundingOrchestratorTest.class.getClassLoader().getResourceAsStream("health_commodities_funding_request.json");

            assertNotNull(stream);

            MediatorHTTPRequest request = new MediatorHTTPRequest(
                    getRef(),
                    getRef(),
                    "unit-test",
                    "POST",
                    "http",
                    null,
                    null,
                    configuration.getProperty("destination.api.path"),
                    IOUtils.toString(stream),
                    Collections.singletonMap("Content-Type", "application/json"),
                    Collections.emptyList()
            );

            orchestrator.tell(request, getRef());

            final Object[] out = new ReceiveWhile<Object>(Object.class, duration("3 seconds")) {
                @Override
                protected Object match(Object msg) {
                    if (msg instanceof FinishRequest) {
                        return msg;
                    }
                    throw noMatch();
                }
            }.get();

            Assert.assertTrue(Arrays.stream(out).anyMatch(c -> c instanceof FinishRequest));
        }};
    }

    /**
     * Performs an invalid health commodities funding HTTP request test
     */
    @Test
    public void testInvalidHealthCommoditiesFundingHTTPRequest() throws Exception {
        assertNotNull(configuration);

        new JavaTestKit(system) {{
            final ActorRef orchestrator = system.actorOf(Props.create(HealthCommoditiesFundingOrchestrator.class, configuration));

            InputStream stream = HealthCommoditiesFundingOrchestratorTest.class.getClassLoader().getResourceAsStream("health_commodities_funding_invalid_request.json");

            assertNotNull(stream);

            MediatorHTTPRequest request = new MediatorHTTPRequest(
                    getRef(),
                    getRef(),
                    "unit-test",
                    "POST",
                    "http",
                    null,
                    null,
                    configuration.getProperty("destination.api.path"),
                    IOUtils.toString(stream),
                    Collections.singletonMap("Content-Type", "application/json"),
                    Collections.emptyList()
            );

            orchestrator.tell(request, getRef());

            final Object[] out = new ReceiveWhile<Object>(Object.class, duration("3 seconds")) {
                @Override
                protected Object match(Object msg) {
                    if (msg instanceof FinishRequest) {
                        return msg;
                    }
                    throw noMatch();
                }
            }.get();

            Assert.assertTrue(Arrays.stream(out).anyMatch(c -> c instanceof FinishRequest));
        }};
    }

    /**
     * Represents a mock class for the THSCP system.
     */
    private static class MockThscp extends MockHTTPConnector {
        /**
         * Gets the response.
         *
         * @return Returns the response.
         */
        @Override
        public String getResponse() {
            return null;
        }

        /**
         * Gets the status code.
         *
         * @return Returns the status code.
         */
        @Override
        public Integer getStatus() {
            return 200;
        }

        /**
         * Gets the HTTP headers.
         *
         * @return Returns the HTTP headers.
         */
        @Override
        public Map<String, String> getHeaders() {
            return Collections.emptyMap();
        }

        /**
         * Handles the message.
         *
         * @param msg The message.
         */
        @Override
        public void executeOnReceive(MediatorHTTPRequest msg) {
            InputStream stream = HealthCommoditiesFundingOrchestrator.class.getClassLoader().getResourceAsStream("health_commodities_funding_request.json");

            assertNotNull(stream);

            Gson gson = new Gson();

            Type domainType = new TypeToken<List<Indicator>>() {}.getType();

            List<Indicator> expected;

            try {

                expected = gson.fromJson(IOUtils.toString(stream), domainType);
            } catch (IOException e) {
                // TODO: handle this issue
                return;
            }

            List<Indicator> actual = gson.fromJson(msg.getBody(), domainType);

            assertEquals(expected.get(0).getUuid(), actual.get(0).getUuid());
            assertEquals(expected.get(0).getAllocatedFund(), actual.get(0).getAllocatedFund());
            assertEquals(expected.get(0).getDisbursedFund(), actual.get(0).getDisbursedFund());
            assertEquals(expected.get(0).getProgram(), actual.get(0).getProgram());
            assertEquals(expected.get(0).getFacilityId(), actual.get(0).getFacilityId());
            assertEquals(expected.get(0).getSource(), actual.get(0).getSource());
            assertEquals(expected.get(0).getProductCode(), actual.get(0).getProductCode());
            assertEquals(expected.get(0).getStartDate(), actual.get(0).getStartDate());
            assertEquals(expected.get(0).getEndDate(), actual.get(0).getEndDate());
        }
    }
}
