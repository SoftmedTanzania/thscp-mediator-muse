package tz.go.moh.him.thscp.mediator.ffars.muse.orchestrators;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import tz.go.moh.him.mediator.core.domain.ErrorMessage;
import tz.go.moh.him.mediator.core.domain.ResultDetail;
import tz.go.moh.him.thscp.mediator.ffars.muse.domain.Indicator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Represents the Health Commodities Funding orchestrator.
 */
public class HealthCommoditiesFundingOrchestrator extends UntypedActor {
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
     * Represents a list of error messages, if any,that have been caught during payload data validation to be returned to the source system as response.
     */
    protected List<ErrorMessage> errorMessages = new ArrayList<>();

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
        List<ResultDetail> resultDetailsList = new ArrayList<>();

        if (indicators == null) {
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_INVALID_PAYLOAD"), null));
        } else {
            resultDetailsList.addAll(validateRequiredFields(indicators));
        }

        if (resultDetailsList.size() != 0) {
            // Adding the validation results to the Error message object
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setSource(new Gson().toJson(indicators));
            errorMessage.setResultsDetails(resultDetailsList);
            errorMessages.add(errorMessage);
        }
    }

    /**
     * Validate Health Commodities Indicator data
     *
     * @param indicators to be validated
     * @return array list of validation results details for failed validations
     */
    public List<ResultDetail> validateRequiredFields(List<Indicator> indicators) {
        List<ResultDetail> resultDetailsList = new ArrayList<>();

        for (Indicator indicator: indicators
             ) {
            if (StringUtils.isBlank(indicator.getUuid()))
                resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("UUID_IS_BLANK"), null));

            if (StringUtils.isBlank(indicator.getProductCode()))
                resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("PRODUCT_CODE_IS_BLANK"), null));

            if (StringUtils.isBlank(indicator.getProgram()))
                resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("PROGRAM_IS_BLANK"), null));

            if (StringUtils.isBlank(indicator.getSource()))
                resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("SOURCE_IS_BLANK"), null));

            if (StringUtils.isBlank(indicator.getFacilityId()))
                resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("FACILITY_ID_IS_BLANK"), null));

            if (StringUtils.isBlank(indicator.getStartDate()))
                resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("START_DATE_ID_IS_BLANK"), null));

            if (StringUtils.isBlank(indicator.getEndDate()))
                resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("END_DATE_ID_IS_BLANK"), null));
        }

        return resultDetailsList;
    }

    /**
     * Handles receiving OpenHIM Core messages into the mediator
     * @param msg to be received
     *
     * @throws Exception
     */
    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) {
            originalRequest = (MediatorHTTPRequest) msg;

            log.info("Received request: " + originalRequest.getHost() + " " + originalRequest.getMethod() + " " + originalRequest.getPath());

            List<Indicator> indicators = null;
            try {
                Type domainType = new TypeToken<List<Indicator>>() {}.getType();
                indicators = new Gson().fromJson((originalRequest).getBody(), domainType);
            } catch (com.google.gson.JsonSyntaxException ex) {
                errorMessages.add(new ErrorMessage(originalRequest.getBody(), Arrays.asList(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_INVALID_PAYLOAD"), null))));
            }

            validateData(indicators);

            if (!errorMessages.isEmpty()) {
                FinishRequest finishRequest = new FinishRequest(new Gson().toJson(errorMessages), "text/json", HttpStatus.SC_BAD_REQUEST);
                (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
            } else {
                sendDataToTargetSystem(originalRequest.getBody());
            }
        } else if (msg instanceof MediatorHTTPResponse) { //respond
            log.info("Received response from target system");
            (originalRequest).getRequestHandler().tell(((MediatorHTTPResponse) msg).toFinishRequest(), getSelf());
        } else {
            unhandled(msg);
        }
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
