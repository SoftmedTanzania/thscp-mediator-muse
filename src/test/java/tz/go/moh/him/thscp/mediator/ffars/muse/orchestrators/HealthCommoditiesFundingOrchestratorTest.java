package tz.go.moh.him.thscp.mediator.ffars.muse.orchestrators;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.google.gson.Gson;
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

            Indicator expected;

            try {
                expected = gson.fromJson(IOUtils.toString(stream), Indicator.class);
            } catch (IOException e) {
                // TODO: handle this issue
                return;
            }

            Indicator actual = gson.fromJson(msg.getBody(), Indicator.class);

            assertEquals(expected.getUuid(), actual.getUuid());
            assertEquals(expected.getAllocatedFund(), actual.getAllocatedFund());
            assertEquals(expected.getDisbursedFund(), actual.getDisbursedFund());
            assertEquals(expected.getProgram(), actual.getProgram());
            assertEquals(expected.getFacilityId(), actual.getFacilityId());
            assertEquals(expected.getSource(), actual.getSource());
            assertEquals(expected.getProductCode(), actual.getProductCode());
            assertEquals(expected.getStartDate(), actual.getStartDate());
            assertEquals(expected.getEndDate(), actual.getEndDate());
        }
    }
}
