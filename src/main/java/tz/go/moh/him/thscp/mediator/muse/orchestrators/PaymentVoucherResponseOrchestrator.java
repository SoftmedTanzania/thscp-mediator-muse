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
import org.json.JSONObject;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import tz.go.moh.him.mediator.core.serialization.JsonSerializer;
import tz.go.moh.him.thscp.mediator.muse.domain.MmamaMuseRequest;
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
 * Represents the Payment Voucher Response Orchestrator.
 */
public class PaymentVoucherResponseOrchestrator extends UntypedActor {
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
     * Initializes a new instance of the {@link PaymentVoucherResponseOrchestrator} class.
     *
     * @param config The mediator configuration.
     */
    public PaymentVoucherResponseOrchestrator(MediatorConfig config) {
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


        JSONObject financeBusPublicKeyProperties = new JSONObject(config.getDynamicConfig()).getJSONObject("financeBusProperties");
        publicKey = financeBusPublicKeyProperties.getString("publicKey");
        publicKeyAlias = financeBusPublicKeyProperties.getString("publicKeyAlias");
        publicKeyPassword = financeBusPublicKeyProperties.getString("publicKeyPassword");

        JSONObject tanzaniaHimPrivateKeyProperties = new JSONObject(config.getDynamicConfig()).getJSONObject("tanzaniaHimPrivateKeyProperties");
        privateKey = tanzaniaHimPrivateKeyProperties.getString("privateKey");
        privateKeyAlias = tanzaniaHimPrivateKeyProperties.getString("privateKeyAlias");
        privateKeyPassword = tanzaniaHimPrivateKeyProperties.getString("privateKeyPassword");


        if (msg instanceof MediatorHTTPRequest) {
            originalRequest = (MediatorHTTPRequest) msg;

            //respond
            log.info("Received response from MUSE");

            JSONObject payload = new JSONObject(((MediatorHTTPRequest) msg).getBody());

            boolean verifySignature = RSAUtils.verifyPayload(payload.getJSONObject("message").toString(), payload.getString("digitalSignature"), publicKey, publicKeyAlias, publicKeyPassword);

            if (verifySignature) {
                sendDataToTargetSystem(payload);
            } else {
                JSONObject messageResponse = payload.getJSONObject("message");
                messageResponse.getJSONObject("messageSummary").put("responseStatus", "REJECTED");
                messageResponse.getJSONObject("messageSummary").put("responseStatusDesc", "Signature Verification Failed");

                FinishRequest finishRequest = new FinishRequest(generateSignedMessage(messageResponse, privateKey, privateKeyAlias, privateKeyPassword), "text/json", HttpStatus.SC_FORBIDDEN);
                (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
            }

        } else if (msg instanceof MediatorHTTPResponse) {
            //respond
            FinishRequest finishRequest = new FinishRequest("RECEIVED", "text/json", HttpStatus.SC_OK);
            (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
        } else {
            unhandled(msg);
        }
    }

    private String generateSignedMessage(JSONObject jsonObject, String privateKey, String privateKeyAlias, String privateKeyPassword) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        String signature = null;
        if (privateKey != null) {
            try {
                signature = RSAUtils.signPayload(privateKey, jsonObject.toString(), privateKeyAlias, privateKeyPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        JSONObject signedResponse = new JSONObject();
        signedResponse.put("message", jsonObject);
        signedResponse.put("digitalSignature", signature);

        return signedResponse.toString();
    }


    /**
     * Handle sending of data to Mmama
     *
     * @param msg to be sent
     */
    protected void sendDataToTargetSystem(JSONObject msg) {
        log.debug("Forwarding request to Mmama");

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");

        String mmamaHost;
        int mmamaPort;
        String mmamaPath;
        String mmamaScheme;
        String mmamausername;
        String mmamaPassword;


        JSONObject connectionProperties = new JSONObject(config.getDynamicConfig()).getJSONObject("mmamaConnectionProperties");

        mmamaHost = connectionProperties.getString("mmamaHost");
        mmamaPort = connectionProperties.getInt("mmamaPort");
        mmamaPath = connectionProperties.getString("mmamaPath");
        mmamaScheme = connectionProperties.getString("mmamaScheme");

        if (connectionProperties.has("mmamaUsername") && connectionProperties.has("mmamaPassword")) {
            mmamausername = connectionProperties.getString("mmamaUsername");
            mmamaPassword = connectionProperties.getString("mmamaPassword");

            // if we have a username and a password
            // we want to add the username and password as the Basic Auth header in the HTTP request
            if (mmamausername != null && !"".equals(mmamausername) && mmamaPassword != null && !"".equals(mmamaPassword)) {
                String auth = mmamausername + ":" + mmamaPassword;
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
                String authHeader = "Basic " + new String(encodedAuth);
                headers.put(HttpHeaders.AUTHORIZATION, authHeader);
            }
        }


        List<Pair<String, String>> params = new ArrayList<>();
        mmamaHost = mmamaScheme + "://" + mmamaHost + ":" + mmamaPort + mmamaPath;

        MediatorHTTPRequest request = new MediatorHTTPRequest(
                (originalRequest).getRequestHandler(), getSelf(), "Sending Data to Mmama", "POST", mmamaHost, msg.toString(), headers, params
        );

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(request, getSelf());

        log.debug("Request forwarded to Mmama");
    }
}
