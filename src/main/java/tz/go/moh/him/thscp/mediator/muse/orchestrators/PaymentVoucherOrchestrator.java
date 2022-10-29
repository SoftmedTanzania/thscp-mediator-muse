package tz.go.moh.him.thscp.mediator.muse.orchestrators;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.codehaus.plexus.util.StringUtils;
import org.json.JSONObject;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import tz.go.moh.him.mediator.core.serialization.JsonSerializer;
import tz.go.moh.him.thscp.mediator.muse.domain.DataValidationResponse;
import tz.go.moh.him.thscp.mediator.muse.domain.FinanceBusResponse;
import tz.go.moh.him.thscp.mediator.muse.domain.MmamaMuseRequest;
import tz.go.moh.him.thscp.mediator.muse.domain.Indicator;
import tz.go.moh.him.thscp.mediator.muse.utils.RSAUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the Payment Voucher Orchestrator.
 */
public class PaymentVoucherOrchestrator extends UntypedActor {
    /**
     * The serializer.
     */
    private static final JsonSerializer serializer = new JsonSerializer();

    /**
     * The logger instance.
     */
    protected final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    /**
     * The mediator configuration.
     */
    protected final MediatorConfig config;

    /**
     * Represents the original mediator request.
     */
    protected MediatorHTTPRequest originalRequest;

    /**
     * Represents an Error Messages Definition Resource Object defined in <a href="file:../resources/error-messages.json">/resources/error-messages.json</a>.
     */
    protected JSONObject errorMessageResource;

    /**
     * Initializes a new instance of the {@link PaymentVoucherOrchestrator} class.
     *
     * @param config The mediator configuration.
     */
    public PaymentVoucherOrchestrator(MediatorConfig config) {
        this.config = config;

        InputStream stream = getClass().getClassLoader().getResourceAsStream("error-messages.json");
        try {
            if (stream != null) {
                errorMessageResource = new JSONObject(IOUtils.toString(stream)).getJSONObject("FUND_COMMODITIES_FUNDING_ERROR_MESSAGES");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles receiving OpenHIM Core messages into the mediator
     *
     * @param msg to be received
     * @throws Exception
     */
    @Override
    public void onReceive(Object msg) throws Exception {
        String publicKey;
        String privateKey;
        String publicKeyAlias;
        String publicKeyPassword;
        String privateKeyAlias;
        String privateKeyPassword;


        JSONObject financeBusProperties = new JSONObject(config.getDynamicConfig()).getJSONObject("financeBusProperties");
        publicKey = financeBusProperties.getString("publicKey");
        publicKeyAlias = financeBusProperties.getString("publicKeyAlias");
        publicKeyPassword = financeBusProperties.getString("publicKeyPassword");

        JSONObject tanzaniaHimPrivateKeyProperties = new JSONObject(config.getDynamicConfig()).getJSONObject("tanzaniaHimPrivateKeyProperties");
        privateKey = tanzaniaHimPrivateKeyProperties.getString("privateKey");
        privateKeyAlias = tanzaniaHimPrivateKeyProperties.getString("privateKeyAlias");
        privateKeyPassword = tanzaniaHimPrivateKeyProperties.getString("privateKeyPassword");


        if (msg instanceof MediatorHTTPRequest) {
            originalRequest = (MediatorHTTPRequest) msg;

            log.info("Received request: " + originalRequest.getHost() + " " + originalRequest.getMethod() + " " + originalRequest.getPath());

            MmamaMuseRequest.Message mmamaMuseRequestMessage;
            try {
                mmamaMuseRequestMessage = serializer.deserialize((originalRequest).getBody(), MmamaMuseRequest.Message.class);

                //Necessary evil. Don't remove this. it affects the ordering of elements in the json object to be signed
                JSONObject responseJsonObject = new JSONObject(new Gson().toJson(generateSignedMessage(mmamaMuseRequestMessage, privateKey, privateKeyAlias, privateKeyPassword)));

                sendDataToTargetSystem(responseJsonObject);

            } catch (Exception e) {
                log.error(e.toString());
                FinishRequest finishRequest = new FinishRequest("Bad Request", "text/json", HttpStatus.SC_BAD_REQUEST);
                (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
            }
        } else if (msg instanceof MediatorHTTPResponse) {
            //respond
            log.info("Received response from MUSE");

            JSONObject payload = new JSONObject(((MediatorHTTPResponse) msg).getBody());

            boolean verifySignature = RSAUtils.verifyPayload(payload.getJSONObject("message").toString(), payload.getString("digitalSignature"), publicKey, publicKeyAlias, publicKeyPassword);
            FinishRequest finishRequest;
            if (verifySignature) {
                finishRequest = ((MediatorHTTPResponse) msg).toFinishRequest();
            } else {
                finishRequest = new FinishRequest(payload.toString(), "text/json", HttpStatus.SC_FORBIDDEN);
            }
            (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
        } else {
            unhandled(msg);
        }
    }

    private MmamaMuseRequest generateSignedMessage(MmamaMuseRequest.Message message, String privateKey, String privateKeyAlias, String privateKeyPassword) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        String signature = null;
        if (privateKey != null) {
            try {
                //Necessary evil. Don't remove this. it affects the ordering of elements in the json object to be signed
                JSONObject messageJson = new JSONObject(serializer.serializeToString(message));

                signature = RSAUtils.signPayload(privateKey, messageJson.toString(), privateKeyAlias, privateKeyPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new MmamaMuseRequest(message, signature);
    }

    /**
     * Handle sending of data to MUSE
     *
     * @param msg to be sent
     */
    protected void sendDataToTargetSystem(JSONObject msg) {
        log.debug("Forwarding request to  via Finance Bus");

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.put("service-code", "SVC046");

        String busHost;
        int busPort;
        String busPath;
        String busScheme;


        log.debug("Using dynamic config");
        JSONObject connectionProperties = new JSONObject(config.getDynamicConfig()).getJSONObject("financeBusProperties");

        busHost = connectionProperties.getString("busHost");
        busPort = connectionProperties.getInt("busPort");
        busPath = connectionProperties.getString("busPath");
        busScheme = connectionProperties.getString("busScheme");


        List<Pair<String, String>> params = new ArrayList<>();

        busHost = busScheme + "://" + busHost + ":" + busPort + busPath;

        MediatorHTTPRequest request = new MediatorHTTPRequest(
                (originalRequest).getRequestHandler(), getSelf(), "Sending Data to MUSE", "POST", busHost, msg.toString(), headers, params
        );

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(request, getSelf());

        log.debug("Request forwarded to MUSE");
    }
}
