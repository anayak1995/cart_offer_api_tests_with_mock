package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.OfferRequest;
import com.springboot.controller.SegmentResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferApplicationTests {


    @Test
    public void doSetupOffers() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");

        OfferRequest offer101 = new OfferRequest(101, "FLATX", 10, segments);
        boolean result101 = addOffer(offer101);
        Assert.assertTrue(result101);

        OfferRequest offer102 = new OfferRequest(102, "PERCENTAGE", 20, segments);
        boolean result102 = addOffer(offer102);
        Assert.assertTrue(result102);

        OfferRequest flat107 = new OfferRequest(107, "FLATX", 10, segments);
        boolean result107Flat = addOffer(flat107);
        Assert.assertTrue(result107Flat);

        OfferRequest percent107 = new OfferRequest(107, "PERCENTAGE", 10, segments);
        boolean result107Percent = addOffer(percent107);
        Assert.assertTrue(result107Percent);

        OfferRequest offer114 = new OfferRequest(114, "PERCENTAGE", (int) 33.5, segments);
        boolean result114 = addOffer(offer114);
        Assert.assertTrue(result114);
    }

    @Test
    public void TC_001_applyFlatAmountOffer() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 200,\n" +
                "  \"user_id\": 1,\n" +
                "  \"restaurant_id\": 101\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_001 FAILED: Apply flat amount offer - expected cart_value=190",
                response.contains("\"cart_value\":190"));
    }

    @Test
    public void TC_002_applyFlatPercentageOffer() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 200,\n" +
                "  \"user_id\": 1,\n" +
                "  \"restaurant_id\": 102\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_002 FAILED: Apply flat percentage offer - expected cart_value=160",
                response.contains("\"cart_value\":160"));
    }

    @Test
    public void TC_003_noOfferAvailableForUser() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 200,\n" +
                "  \"user_id\": 1,\n" +
                "  \"restaurant_id\": 1\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_003 FAILED: No offer available for user - expected cart_value=200",
                response.contains("\"cart_value\":200"));
    }

    @Test
    public void TC_004_offerValueGreaterThanCart() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 5,\n" +
                "  \"user_id\": 1,\n" +
                "  \"restaurant_id\": 101\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_004 FAILED: Offer value greater than cart value - expected cart_value=-5",
                response.contains("\"cart_value\":-5"));
    }

    @Test
    public void TC_005_zeroCartValueFlatOffer() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 0,\n" +
                "  \"user_id\": 1,\n" +
                "  \"restaurant_id\": 101\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_005 FAILED: Zero cart value with flat amount offer - expected cart_value=-10",
                response.contains("\"cart_value\":-10"));
    }

    @Test
    public void TC_006_zeroCartValuePercentageOffer() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 0,\n" +
                "  \"user_id\": 1,\n" +
                "  \"restaurant_id\": 101\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_006 FAILED: Zero cart value with percentage offer - expected cart_value=-10",
                response.contains("\"cart_value\":-10"));
    }

    @Test
    public void TC_007_multipleOffersForSameSegment() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 200,\n" +
                "  \"user_id\": 1,\n" +
                "  \"restaurant_id\": 107\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_007 FAILED: Apply offer when multiple offers exist for same segment - expected first offer applied, cart_value=190",
                response.contains("\"cart_value\":190"));
    }

    @Test
    public void TC_009_fractionalPercentageOffer() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 100,\n" +
                "  \"restaurant_id\": 114,\n" +
                "  \"user_id\": 1\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_009 FAILED: Apply percentage offer with fractional discount - expected cart_value=67",
                response.contains("\"cart_value\":67"));
    }

    @Test
    public void TC_010_restaurantWithoutOffers() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 250,\n" +
                "  \"user_id\": 1,\n" +
                "  \"restaurant_id\": 999\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_010 FAILED: Restaurant without offers configured - expected cart_value=250",
                response.contains("\"cart_value\":250"));
    }

    @Test
    public void TC_011_invalidRestaurantId() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 100,\n" +
                "  \"user_id\": 1,\n" +
                "  \"restaurant_id\": 1011\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_011 FAILED: Invalid restaurant id - no discount applied, expected cart_value=100",
                response.contains("\"cart_value\":100"));
    }

    @Test
    public void TC_012_invalidUserId() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 200,\n" +
                "  \"user_id\": 999,\n" +
                "  \"restaurant_id\": 1\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_012 FAILED: Invalid user id - no discount applied, expected cart_value=200",
                response.contains("\"cart_value\":200"));
    }

    @Test
    public void TC_013_onlyUserIdPassed() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 100,\n" +
                "  \"user_id\": 1\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_013 FAILED: Only user_id passed - expected cart_value=100",
                response.contains("\"cart_value\":100"));
    }

    @Test
    public void TC_014_onlyRestaurantIdPassed() throws Exception {
        String requestJson = "{\n" +
                "  \"cart_value\": 100,\n" +
                "  \"restaurant_id\": 114\n" +
                "}";
        String response = applyOffer(requestJson);
        Assert.assertTrue("TC_014 FAILED: Only restaurant_id passed - expected cart_value=100",
                response.contains("\"cart_value\":100"));
    }



    public String applyOffer(String requestJson) throws Exception {
        String urlString = "http://localhost:9001/api/v1/cart/apply_offer";
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = con.getOutputStream()) {
            os.write(requestJson.getBytes());
            os.flush();
        }

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        System.out.println("ApplyOffer Response: " + response);
        return response.toString();
    }


    public boolean addOffer(OfferRequest offerRequest) throws Exception {
		String urlString = "http://localhost:9001/api/v1/offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();

		String POST_PARAMS = mapper.writeValueAsString(offerRequest);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();
		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("POST request did not work.");
		}
		return true;
	}

}
