package com.nsysmon.measure.scalar;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class RESTEasyClientGet {

    public static void main(String[] args) {


        try {
            ResteasyClient client = new ResteasyClientBuilder().build();

            ResteasyWebTarget target = client
                    .target("http://jsonplaceholder.typicode.com/posts");

            Long timestamp = 11000L;

//            Response response = target.request().post(Entity.entity(timestamp, "application/json"));
            Response response = target.request().get();

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }

            System.out.println("Server response : \n");
            System.out.println(response.readEntity(String.class));

            response.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }
}

