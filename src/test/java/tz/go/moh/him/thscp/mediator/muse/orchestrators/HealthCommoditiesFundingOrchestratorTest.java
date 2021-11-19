package tz.go.moh.him.thscp.mediator.muse.orchestrators;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.testing.MockHTTPConnector;
import org.openhim.mediator.engine.testing.MockLauncher;
import org.openhim.mediator.engine.testing.TestingUtils;
import tz.go.moh.him.thscp.mediator.muse.domain.HealthCommodityFundingRequest;
import tz.go.moh.him.thscp.mediator.muse.domain.Indicator;
import tz.go.moh.him.thscp.mediator.muse.utils.RSAUtils;

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
     * Adds dynamic configs to the mediator.
     *
     * @param mediatorConfig The mediator config.
     */
    public static void addDynamicConfigs(MediatorConfig mediatorConfig, String publicKey, String privateKey, String keystorePassword, String alias) {
        JSONObject publicKeyProperties = new JSONObject();
        publicKeyProperties.put("publicKey", publicKey);
        publicKeyProperties.put("publicKeyAlias", alias);
        publicKeyProperties.put("publicKeyPassword", keystorePassword);


        JSONObject privateKeyProperties = new JSONObject();
        privateKeyProperties.put("privateKey", privateKey);
        privateKeyProperties.put("privateKeyAlias", alias);
        privateKeyProperties.put("privateKeyPassword", keystorePassword);

        mediatorConfig.getDynamicConfig().put("financeBusPublicKeyProperties", publicKeyProperties);
        mediatorConfig.getDynamicConfig().put("tanzaniaHimPrivateKeyProperties", privateKeyProperties);
        mediatorConfig.getDynamicConfig().put("destinationConnectionProperties", new JSONObject("{\n" +
                "    \"destinationHost\": \"localhost\",\n" +
                "    \"destinationPort\": \"3000\",\n" +
                "    \"destinationPath\": \"/test\",\n" +
                "    \"accessTokenUri\":\"tokenUri\",\n" +
                "    \"destinationUsername\":\"username\",\n" +
                "    \"destinationPassword\":\"password\",\n" +
                "    \"destinationScheme\":\"http\"\n" +
                "  }"));

    }

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
            InputStream stream = HealthCommoditiesFundingOrchestratorTest.class.getClassLoader().getResourceAsStream("health_commodities_funding_request.json");
            assertNotNull(stream);

            //test .pfx keys, using alis 'test' and password 'test2021'
            String base64PrivateKey = "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";
            String base64PublicKey = "MIIEngIBAzCCBFcGCSqGSIb3DQEHAaCCBEgEggREMIIEQDCCBDwGCSqGSIb3DQEHBqCCBC0wggQpAgEAMIIEIgYJKoZIhvcNAQcBMCkGCiqGSIb3DQEMAQYwGwQUh28nHcAySdt/IL4mnVGXjsOAtaYCAwDDUICCA+h/pRu8DrueUQOASOhqqF5cZrs1t6Fk7LiqbumX8I3hkwqLFtlWdoSFEuj5x8cKyun7FIv4rGhSaF9JyuUxeboD9K95enzTjViNL4mlOHJj7gXVea+7PTirGOF5RHZLsourhA1Fw94DtIYiDTm2M1bt8UQrB6B8OCbfjwmj38znoI7Uq82PmaKlPcnm/omvr8beWuTQT/NI2mX433l+TWBqHtLbW1KZsW2xzpdwiCqbWa5Qe7JJMqotqbkLXzZTksRjSXRoVKTvQwW1/0lWY9SE3snE82Jj5e3zjAMtCD2U7mTfR3S9wZA8EW7qOniT8hO0EaWP7Vu0VALBxa+QR1rhZ2dNcZfJJQK5CeAKdqYkbpssbXXl9WaO66G2NfJ4LFzyylbhqqV9DsIlZPvyrM1VnLJzDIww1w1E0oit8EW3DFwZ1yWYZVAMnvM701hmxdYbe/OfDT9LbqqaHTOzz09xaauKsr8/hslM8yZEZ404PW9jm/iHogTcFt1e4JZdjYTZJN/Au/JJxdK7MhzLVIhs4Qxn4g6uR8jzsXFhTxH37jybjVSD0pvjfRW/J60tB+5ShBbBa9eSv0AHeS/1L0RwZNWz/e5VWbb9sE2OPbqof1sZJnyEFtuhLvy3clREmcfXCzMYPHk9Wucqr1sQtUk2uANRca1GO0bkfNvByGUdB2293CQ0Yve/M+Ut3KHQGquoj+rbZruxd3fY7DQKLwzcMvfxdzu3/hUplVvJzF6lTGuOAseJSiIx6OslQVe/OsSJzCrI/tT1sX+PRM1g030UV6tUDxhnjetTpmDo57PfnSVmnYd4+RbHB0dcXEwDd6OdvuZPmX3E4jBvRAidiW1DqrDDPTWMHc4M+Kj9R/W3ypPFZegT3xEJr7YEW23kJnQSb42Bcf5eJzqKYaCPgJoVKZWpGQlMo90Xg8DtYjn9XpnOobu2Jg2KP3BewZ/njyZvYv+ozlHH9AuTiY3SDCKdJa+FBuw7Z61yx6Fpz1ZHnIHCzuOXIrY/2DdL10pPY8hFbjcuCkmaPAAcuDcY9seHRFRI4QMJvE73eQeZuq4J0GCcquGoubOwN9cefIPseymBiUjg0wKl32cCifLQL2C6q/4CyC1weD8MGTojex+mSwZyCDS7KG23aPG869xd0RDAB+SQ58KOAnG2dIMTcOAbivsEYwWRGAwwhT7H31fIXpAl+RT7M15yfMcECuOxSTX9Fn2rALQXD84UFTdFd/T3gaAgK1FWqrQZNeuIw82edDc94l9CDz0mjaeIq/AZfOmRCmqZnzJjNIOkuK9DNwXF2DIjED+WTtxDWS+Jc5/qfmP38TWFbLC1MD4wITAJBgUrDgMCGgUABBRq9kJTEOoNwl8p5U0bqhJsudxlDQQU4onQFUREEQ03WqbYbQzrCjdFC4ICAwGGoA==";

            addDynamicConfigs(configuration, base64PublicKey, base64PrivateKey, "test2021", "test");

            JSONObject payload = new JSONObject(IOUtils.toString(stream));

            String signature = RSAUtils.signPayload(base64PrivateKey, payload.getJSONArray("data").toString(), "test", "test2021");
            payload.put("signature", signature);

            MediatorHTTPRequest request = new MediatorHTTPRequest(
                    getRef(),
                    getRef(),
                    "unit-test",
                    "POST",
                    "http",
                    null,
                    null,
                    configuration.getProperty("destination.api.path"),
                    payload.toString(),
                    Collections.singletonMap("Content-Type", "application/json"),
                    Collections.emptyList()
            );

            final ActorRef orchestrator = system.actorOf(Props.create(HealthCommoditiesFundingOrchestrator.class, configuration));
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
            return "{\n" +
                    "   \"code\": 200,\n" +
                    "   \"success\": true,\n" +
                    "   \"message\": \"success\",\n" +
                    "   \"data\": {\n" +
                    "       \"created\": 1,\n" +
                    "       \"updated\": 0,\n" +
                    "       \"error\": 0,\n" +
                    "       \"response\": [\n" +
                    "           {\n" +
                    "               \"message\": \"Created\",\n" +
                    "               \"uuid\": \"3d378375-6a0c-4974-b737-4160c293774d\",\n" +
                    "               \"code\": 1,\n" +
                    "               \"errors\": null\n" +
                    "           }\n" +
                    "       ]\n" +
                    "   }\n" +
                    "}\n";
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

            Type domainType = new TypeToken<List<Indicator>>() {
            }.getType();

            List<Indicator> expected;

            try {

                expected = (gson.fromJson(IOUtils.toString(stream), HealthCommodityFundingRequest.class)).getData();
            } catch (IOException e) {
                // TODO: handle this issue
                return;
            }

            List<Indicator> actual = gson.fromJson(msg.getBody(), domainType);
            assertEquals(expected.get(0).getAllocatedFund(), actual.get(0).getAllocatedFund(), 0.0);
            assertEquals(expected.get(0).getBudgetedFund(), actual.get(0).getBudgetedFund(), 0.0);
            assertEquals(expected.get(0).getGfsCode(), actual.get(0).getGfsCode());
            assertEquals(expected.get(0).getInstitutionCode(), actual.get(0).getInstitutionCode());
            assertEquals(expected.get(0).getSource(), actual.get(0).getSource());
            assertEquals(expected.get(0).getFinancialYear(), actual.get(0).getFinancialYear());
            assertEquals(expected.get(0).getActivity(), actual.get(0).getActivity());
            assertEquals(expected.get(0).getDate(), actual.get(0).getDate());
        }
    }
}
