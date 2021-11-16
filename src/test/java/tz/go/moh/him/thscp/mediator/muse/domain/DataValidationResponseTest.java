package tz.go.moh.him.thscp.mediator.muse.domain;

import com.google.gson.Gson;
import org.junit.Test;
import tz.go.moh.him.mediator.core.serialization.JsonSerializer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class DataValidationResponseTest {
    /**
     * Performs an Data Validation Response data serialization test
     */
    @Test
    public void testDataValidationResponseSerialization() throws Exception {
        List<DataValidationResponse.DataValidationResultDetail> dataValidationResultDetails =  new ArrayList<>();

        DataValidationResponse.DataValidationResultDetail dataValidationResultDetail = new DataValidationResponse.DataValidationResultDetail("sample_uuid","uuid is blank");
        dataValidationResultDetails.add(dataValidationResultDetail);


        DataValidationResponse dataValidationResponse = new DataValidationResponse(dataValidationResultDetails);

        JsonSerializer serializer = new JsonSerializer();

        String actual = serializer.serializeToString(dataValidationResponse);

        assertTrue(actual.contains("sample_uuid"));
        assertTrue(actual.contains("uuid is blank"));
        assertTrue(actual.contains("response"));
    }
}