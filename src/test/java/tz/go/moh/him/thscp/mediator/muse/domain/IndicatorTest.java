package tz.go.moh.him.thscp.mediator.muse.domain;

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

        indicator.setAllocatedFund(200000);
        indicator.setBudgetedFund(100000);
        indicator.setFacilityId("0T100000");
        indicator.setFinancialYear("2020/2021");
        indicator.setGfsCode("22010253");
        indicator.setSource("GOT");
        indicator.setActivity("D01S02");
        indicator.setDate("2020-12-03");

        Gson gson = new Gson();
        String actual = gson.toJson(indicator, Indicator.class);

        assertTrue(actual.contains(indicator.getFacilityId()));
        assertTrue(actual.contains(indicator.getFinancialYear()));
        assertTrue(actual.contains(indicator.getGfsCode()));
        assertTrue(actual.contains(indicator.getSource()));
        assertTrue(actual.contains(indicator.getActivity()));
    }

    /**
     * Performs an indicator data deserialization test
     */
    @Test
    public void testIndicatorDeserialization() throws Exception {
        InputStream stream = IndicatorTest.class.getClassLoader().getResourceAsStream("indicator.json");
        Assert.assertNotNull(stream);

        Indicator indicator = new Gson().fromJson(IOUtils.toString(stream), Indicator.class);

        assertEquals("0T100000", indicator.getFacilityId());
        assertEquals("2020/2021", indicator.getFinancialYear());
        assertEquals("22010253", indicator.getGfsCode());
        assertEquals("0GT", indicator.getSource());
        assertEquals("D01S02", indicator.getActivity());
        assertEquals("2021-10-09", indicator.getDate());
    }
}
