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
import tz.go.moh.him.thscp.mediator.muse.domain.HealthCommodityFundingRequest;
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
 * Represents the Health Commodities Funding orchestrator.
 */
public class HealthCommoditiesFundingOrchestrator extends UntypedActor {
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
     * Represents data validation error messages, if any,that have been caught during payload data validation to be returned to the source system as response.
     */
    protected DataValidationResponse dataValidationResponse = null;

    /**
     * Initializes a new instance of the {@link HealthCommoditiesFundingOrchestrator} class.
     *
     * @param config The mediator configuration.
     */
    public HealthCommoditiesFundingOrchestrator(MediatorConfig config) {
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
     * Handles data validations
     *
     * @param indicators The object to be validated
     */
    protected void validateData(List<Indicator> indicators) {
        List<DataValidationResponse.DataValidationResultDetail> resultDetailsList = new ArrayList<>();

        if (indicators == null) {
            resultDetailsList.add(new DataValidationResponse.DataValidationResultDetail("", errorMessageResource.getString("ERROR_INVALID_PAYLOAD")));
        } else {
            resultDetailsList.addAll(validateRequiredFields(indicators));
        }

        if (resultDetailsList.size() != 0) {
            // Adding the validation results to the Error message object
            dataValidationResponse = new DataValidationResponse(resultDetailsList);
        }
    }

    /**
     * Validate Health Commodities Indicator data
     *
     * @param indicators to be validated
     * @return array list of validation results details for failed validations
     */
    public List<DataValidationResponse.DataValidationResultDetail> validateRequiredFields(List<Indicator> indicators) {
        List<DataValidationResponse.DataValidationResultDetail> resultDetailsList = new ArrayList<>();

        for (Indicator indicator : indicators) {
            if (StringUtils.isBlank(indicator.getFinancialYear()))
                resultDetailsList.add(new DataValidationResponse.DataValidationResultDetail(indicator.getUuid(), errorMessageResource.getString("FINANCIAL_YEAR_IS_BLANK")));

            if (StringUtils.isBlank(indicator.getGfsCode()))
                resultDetailsList.add(new DataValidationResponse.DataValidationResultDetail(indicator.getUuid(), errorMessageResource.getString("GFS_CODE_IS_BLANK")));

//            if (StringUtils.isBlank(indicator.getGfsDescription()))
//                resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("GFS_DESCRIPTION_IS_BLANK"), null));

            if (StringUtils.isBlank(indicator.getSource()))
                resultDetailsList.add(new DataValidationResponse.DataValidationResultDetail(indicator.getUuid(), errorMessageResource.getString("SOURCE_IS_BLANK")));

            if (StringUtils.isBlank(indicator.getFacilityId()))
                resultDetailsList.add(new DataValidationResponse.DataValidationResultDetail(indicator.getUuid(), errorMessageResource.getString("FACILITY_ID_IS_BLANK")));

            if (StringUtils.isBlank(indicator.getActivity()))
                resultDetailsList.add(new DataValidationResponse.DataValidationResultDetail(indicator.getUuid(), errorMessageResource.getString("ACTIVITY_IS_BLANK")));

            if (StringUtils.isBlank(indicator.getDate()))
                resultDetailsList.add(new DataValidationResponse.DataValidationResultDetail(indicator.getUuid(), errorMessageResource.getString("DATE_IS_BLANK")));
        }

        return resultDetailsList;
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

        if (config.getDynamicConfig().isEmpty()) {
            log.debug("Dynamic config is empty, using config from mediator.properties");
            publicKey = config.getProperty("source.publicKey");
            publicKeyAlias = config.getProperty("source.publicKeyAlias");
            publicKeyPassword = config.getProperty("source.publicKeyPassword");

            privateKey = config.getProperty("privateKey");
            privateKeyAlias = config.getProperty("privateKeyAlias");
            privateKeyPassword = config.getProperty("privateKeyPassword");
        } else {
            log.debug("Using dynamic config");

            JSONObject financeBusPublicKeyProperties = new JSONObject(config.getDynamicConfig()).getJSONObject("financeBusPublicKeyProperties");
            publicKey = financeBusPublicKeyProperties.getString("publicKey");
            publicKeyAlias = financeBusPublicKeyProperties.getString("publicKeyAlias");
            publicKeyPassword = financeBusPublicKeyProperties.getString("publicKeyPassword");

            JSONObject tanzaniaHimPrivateKeyProperties = new JSONObject(config.getDynamicConfig()).getJSONObject("tanzaniaHimPrivateKeyProperties");
            privateKey = tanzaniaHimPrivateKeyProperties.getString("privateKey");
            privateKeyAlias = tanzaniaHimPrivateKeyProperties.getString("privateKeyAlias");
            privateKeyPassword = tanzaniaHimPrivateKeyProperties.getString("privateKeyPassword");
        }

        if (msg instanceof MediatorHTTPRequest) {
            originalRequest = (MediatorHTTPRequest) msg;

            log.info("Received request: " + originalRequest.getHost() + " " + originalRequest.getMethod() + " " + originalRequest.getPath());

            HealthCommodityFundingRequest healthCommodityFundingRequest;
            try {
                healthCommodityFundingRequest = serializer.deserialize((originalRequest).getBody(), HealthCommodityFundingRequest.class);
                JSONObject payload = new JSONObject(((MediatorHTTPRequest) msg).getBody());
                boolean verifySignature = RSAUtils.verifyPayload(payload.getJSONArray("data").toString(), healthCommodityFundingRequest.getSignature(), publicKey, publicKeyAlias, publicKeyPassword);
                if (verifySignature) {
                    validateData(healthCommodityFundingRequest.getData());
                    if (dataValidationResponse != null) {
                        FinanceBusResponse.ResponseData responseData = new FinanceBusResponse.ResponseData();
                        responseData.setSuccess(false);
                        responseData.setCode(HttpStatus.SC_BAD_REQUEST);
                        responseData.setMessage("Data validations failed");
                        responseData.setData(dataValidationResponse);

                        FinishRequest finishRequest = new FinishRequest(new Gson().toJson(generateFinanceBusResponse(responseData, privateKey, privateKeyAlias, privateKeyPassword)), "text/json", HttpStatus.SC_BAD_REQUEST);
                        (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
                    } else {
                        sendDataToTargetSystem(new Gson().toJson(healthCommodityFundingRequest.getData()));
                    }
                } else {
                    FinanceBusResponse.ResponseData responseData = new FinanceBusResponse.ResponseData();
                    responseData.setSuccess(false);
                    responseData.setCode(HttpStatus.SC_UNAUTHORIZED);
                    responseData.setMessage("signature verification failed");

                    FinishRequest finishRequest = new FinishRequest(new Gson().toJson(generateFinanceBusResponse(responseData, privateKey, privateKeyAlias, privateKeyPassword)), "text/json", HttpStatus.SC_UNAUTHORIZED);
                    (originalRequest).getRequestHandler().tell(finishRequest, getSelf());

                }
            } catch (Exception e) {
                log.error(e.toString());

                FinanceBusResponse.ResponseData responseData = new FinanceBusResponse.ResponseData();
                responseData.setSuccess(false);
                responseData.setCode(HttpStatus.SC_BAD_REQUEST);
                responseData.setMessage("Bad Request");

                FinishRequest finishRequest = new FinishRequest(new Gson().toJson(generateFinanceBusResponse(responseData, privateKey, privateKeyAlias, privateKeyPassword)), "text/json", HttpStatus.SC_BAD_REQUEST);
                (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
            }
        } else if (msg instanceof MediatorHTTPResponse) { //respond
            log.info("Received response from target system");
            FinanceBusResponse.ResponseData responseData = serializer.deserialize(((MediatorHTTPResponse) msg).getBody(), FinanceBusResponse.ResponseData.class);

            FinishRequest finishRequest = new FinishRequest(new Gson().toJson(generateFinanceBusResponse(responseData, privateKey, privateKeyAlias, privateKeyPassword)), "text/json", ((MediatorHTTPResponse) msg).getStatusCode());
            (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
        } else {
            unhandled(msg);
        }
    }

    private FinanceBusResponse generateFinanceBusResponse(FinanceBusResponse.ResponseData responseData, String privateKey, String privateKeyAlias, String privateKeyPassword) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        String signature = null;
        if (privateKey != null) {
            try {
                signature = RSAUtils.signPayload(privateKey, serializer.serializeToString(responseData), privateKeyAlias, privateKeyPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new FinanceBusResponse(responseData, signature);

    }

    /**
     * Handle sending of data to THSCP
     *
     * @param msg to be sent
     */
    protected void sendDataToTargetSystem(String msg) {
        log.debug("Forwarding request to THSCP");

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");

        String host;
        int port;
        String path;
        String scheme;
        String username = "";
        String password = "";

        if (config.getDynamicConfig().isEmpty()) {
            log.debug("Dynamic config is empty, using config from mediator.properties");

            host = config.getProperty("destination.host");
            port = Integer.parseInt(config.getProperty("destination.api.port"));
            path = config.getProperty("destination.api.path");
            scheme = config.getProperty("destination.scheme");
        } else {
            log.debug("Using dynamic config");

            JSONObject connectionProperties = new JSONObject(config.getDynamicConfig()).getJSONObject("destinationConnectionProperties");

            host = connectionProperties.getString("destinationHost");
            port = connectionProperties.getInt("destinationPort");
            path = connectionProperties.getString("destinationPath");
            scheme = connectionProperties.getString("destinationScheme");

            if (connectionProperties.has("destinationUsername") && connectionProperties.has("destinationPassword")) {
                username = connectionProperties.getString("destinationUsername");
                password = connectionProperties.getString("destinationPassword");

                // if we have a username and a password
                // we want to add the username and password as the Basic Auth header in the HTTP request
                if (username != null && !"".equals(username) && password != null && !"".equals(password)) {
                    String auth = username + ":" + password;
                    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
                    String authHeader = "Basic " + new String(encodedAuth);
                    headers.put(HttpHeaders.AUTHORIZATION, authHeader);
                }
            }
        }

        List<Pair<String, String>> params = new ArrayList<>();

        host = scheme + "://" + host + ":" + port + path;

        MediatorHTTPRequest forwardToThscpRequest = new MediatorHTTPRequest(
                (originalRequest).getRequestHandler(), getSelf(), "Sending Health Commodities Funding Indicator to THSCP", "POST", host, msg, headers, params
        );

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(forwardToThscpRequest, getSelf());

        log.debug("Request forwarded to THSCP");
    }
}
