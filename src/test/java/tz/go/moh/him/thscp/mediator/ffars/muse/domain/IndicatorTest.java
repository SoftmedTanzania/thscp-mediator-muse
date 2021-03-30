package tz.go.moh.him.thscp.mediator.ffars.muse.domain;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Represents unit tests for the {@link Indicator} class.
 */
public class IndicatorTest {
    /**
     * Performs an indicator data serialization test
     */
    @Test
    public void testIndicatorSerialization() throws Exception {
        Indicator indicator = new Indicator();

        indicator.setUuid("3d378375-6a0c-4974-b737-4160c293774d");
        indicator.setAllocatedFund(200000);
        indicator.setDisbursedFund(100000);
        indicator.setFacilityId("123456");
        indicator.setProductCode("10020035MD");
        indicator.setProgram("COVID-19");
        indicator.setSource("GOT");
        indicator.setStartDate("2020-12-03");
        indicator.setEndDate("2020-12-03");

        Gson gson = new Gson();
        String actual = gson.toJson(indicator, Indicator.class);

        assertTrue(actual.contains(indicator.getUuid()));
        assertTrue(actual.contains(indicator.getFacilityId()));
        assertTrue(actual.contains(indicator.getProductCode()));
        assertTrue(actual.contains(indicator.getProgram()));
        assertTrue(actual.contains(indicator.getSource()));
        assertTrue(actual.contains(indicator.getStartDate()));
    }

    /**
     * Performs an indicator data deserialization test
     */
    @Test
    public void testIndicatorDeserialization() throws Exception {
        InputStream stream = IndicatorTest.class.getClassLoader().getResourceAsStream("indicator.json");
        Assert.assertNotNull(stream);

        Indicator indicator = new Gson().fromJson(IOUtils.toString(stream), Indicator.class);

        assertEquals("3d378375-6a0c-4974-b737-4160c293774d", indicator.getUuid());
        assertEquals("123456", indicator.getFacilityId());
        assertEquals("10020035MD", indicator.getProductCode());
        assertEquals("COVID-19", indicator.getProgram());
        assertEquals("GOT", indicator.getSource());
        assertEquals("2020-12-03", indicator.getStartDate());
        assertEquals("2020-12-03", indicator.getEndDate());
    }
}
