package org.emergent.wauk.cli;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * @author Patrick Woodworth
 */
public class Main {

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    private static String url2 = "http://us.battle.net/api/wow/achievement/2144";
    private static String urlbase = "http://us.battle.net/api/wow/auction/data/";

    public static void main(String[] args) {
        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        try {
            String entryUrl = getServerAuctionInfo(client, "silvermoon");
            getAuctionInfoEntry(client, entryUrl);
        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getAuctionInfoEntry(HttpClient client, String url) throws IOException {

        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            InputStream responseBody = method.getResponseBodyAsStream();
            String charset = method.getResponseCharSet();

            StringWriter writer = new StringWriter();
            IOUtils.copy(responseBody, writer, charset);

            String json = writer.toString();

            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            Object foo = mapper.readValue(json, Object.class);
//            System.out.println(foo);
            System.out.println(mapper.writeValueAsString(foo));

//            Map result = (Map)foo;
//            List files = (List)result.get("files");
//
//            Map entry = (Map)files.get(0);
//
//            System.out.printf("entry class: %s\n", entry.getClass());
//
//            return (String)(entry.get("url"));

            return null;

        } finally {
            // Release the connection.
            method.releaseConnection();
        }
    }


    private static String getServerAuctionInfo(HttpClient client, String serverName) throws IOException {
        String url = urlbase + serverName;

        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            byte[] responseBody = method.getResponseBody();
            String charset = method.getResponseCharSet();

            String json = new String(responseBody, charset);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            Object foo = mapper.readValue(json, Object.class);
//            System.out.println(foo);
            System.out.println(mapper.writeValueAsString(foo));

            Map result = (Map)foo;
            List files = (List)result.get("files");

            Map entry = (Map)files.get(0);

            System.out.printf("entry class: %s\n", entry.getClass());

            return (String)(entry.get("url"));

        } finally {
            // Release the connection.
            method.releaseConnection();
        }
    }
}
