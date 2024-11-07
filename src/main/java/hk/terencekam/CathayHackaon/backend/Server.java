package hk.terencekam.CathayHackaon.backend;


import com.sun.net.httpserver.HttpServer;
import hk.terencekam.CathayHackaon.backend.SQL.Query;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static java.net.HttpURLConnection.*;

public class Server {
    private static AtomicLong lastID = new AtomicLong(1);


    public static enum PERMISSION{
        ADMIN, CLIENT
    }

    public static Query query = new Query();

    public static long getNextID() {
        return lastID.getAndIncrement();
    }

    public static void main(String[] args) {

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8081),
                    0);

            server.createContext("/", exchange -> {
                String reqParameters = exchange.getRequestURI().toString();
                System.out.printf("%s %s %s%n",
                        exchange.getRemoteAddress(),
                        exchange.getRequestMethod(),
                        reqParameters);
                String requestMethod = exchange.getRequestMethod();

                String data = "";
                String response = "";
                if (requestMethod.equals("GET")) {
                    data = reqParameters.substring(
                            reqParameters.indexOf("?") + 1);
                } else if (requestMethod.equals("POST")) {
                    data = new String(exchange.getRequestBody().readAllBytes());
                }

                System.out.println("Body data: " + data);

                Map<String,String> parameters = parseParameters(data);
                System.out.println(parameters);


                PERMISSION permission = null;
                String ClientID;
                Response verify = query.verify(exchange.getRequestHeaders().get("ClientID").getFirst(), exchange.getRequestHeaders().get("ClientSecret").getFirst());
                System.out.println(verify);
                if(verify.code()==HTTP_OK){
                    if(verify.jsonObject().getString("Type").equalsIgnoreCase("Admin")){
                        permission = PERMISSION.ADMIN;
                    }else{
                        permission = PERMISSION.CLIENT;
                    }
                    ClientID = verify.jsonObject().getString("ClientUserID");
                }else{
                    if(exchange.getRequestURI().getPath().equals("/createAccount")){
                        query.createAccount(parameters.get("Password"), parameters.get("Username"), parameters.get("FirstName"), parameters.get("LastName"), parameters.get("CompanyName"), parameters.get("AvtarUrl"), parameters.get("Email"), parameters.get("PhoneNo"), parameters.get("CompanyPhoneNo"), parameters.get("AddressOne"), parameters.get("AddressTwo"), parameters.get("Type"), false);
                        exchange.sendResponseHeaders(HTTP_OK, 0);
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.close();
                    }
                    ClientID = "";
                    exchange.sendResponseHeaders(verify.code(), 0);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.close();
                }
                switch (exchange.getRequestURI().getPath()){
                    case "/passwordReset":
                    {
                        if(permission == PERMISSION.ADMIN){
                            if(query.getUser(parameters.get("clientUserID")).code() == HTTP_ACCEPTED && parameters.get("newPassword") != null){
                                query.passwordReset(parameters.get("clientUserID") , parameters.get("newPassword"));
                            }else{
                                exchange.sendResponseHeaders(HTTP_BAD_REQUEST, 0);
                                exchange.getResponseBody().write(response.getBytes());
                                exchange.close();
                            }
                        }
                        if(query.getUser(ClientID).code() == HTTP_ACCEPTED && parameters.get("newPassword") != null && parameters.get("oldPassword") != null){
                            query.passwordReset(ClientID, parameters.get("oldPassword"), parameters.get("newPassword"));
                        }else{
                            exchange.sendResponseHeaders(HTTP_BAD_REQUEST, 0);
                            exchange.getResponseBody().write(response.getBytes());
                            exchange.close();
                        }
                        break;
                    }
                    case "/modifyUser":
                    {

                        if(permission == PERMISSION.ADMIN){
                            if(query.getUser(parameters.get("clientUserID")).code() == HTTP_ACCEPTED) {
                                var userId = parameters.get("clientUserID");
                                parameters.remove("clientUserID");
                                parameters.forEach((j, k) -> query.modifyUser(userId, j, k));
                            }else{
                                exchange.sendResponseHeaders(HTTP_BAD_REQUEST, 0);
                                exchange.getResponseBody().write(response.getBytes());
                                exchange.close();
                            }

                        }
                        parameters.remove("clientUserID");
                        parameters.forEach((j, k) -> query.modifyUser(ClientID, j, k));
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.close();
                        break;
                    }
                    case "/getFullUserList":
                    {
                        if(permission == PERMISSION.ADMIN){
                            Response response1 = query.getFullUserList();
                            exchange.sendResponseHeaders( response1.code(), response1.jsonObject().toString().length());
                            exchange.getResponseBody().write(response1.jsonObject().toString().getBytes());
                            exchange.close();
                        }
                        exchange.sendResponseHeaders(HTTP_UNAUTHORIZED, 0);
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.close();
                        break;
                    }
                    case "/getUserInfo":
                    {
                        if(permission == PERMISSION.ADMIN){
                            if(query.getUser(parameters.get("clientUserID")).code() == HTTP_ACCEPTED) {
                                Response response1 = query.getUser(parameters.get("clientUserID"));
                                exchange.sendResponseHeaders(HTTP_ACCEPTED, response1.jsonObject().toString().length());
                                exchange.getResponseBody().write(response1.jsonObject().toString().getBytes());
                            }else{
                                exchange.sendResponseHeaders(HTTP_BAD_REQUEST, 0);
                                exchange.getResponseBody().write(response.getBytes());
                                exchange.close();
                            }
                        }else{
                            Response response1 = query.getUser(ClientID);
                            exchange.sendResponseHeaders(HTTP_ACCEPTED, response1.jsonObject().toString().length());
                            exchange.getResponseBody().write(response1.jsonObject().toString().getBytes());
                        }
                        break;
                    }
                    case "/createAccount":
                    {
                        if(permission == PERMISSION.ADMIN){
                            query.createAccount(parameters.get("Password"), parameters.get("Username"), parameters.get("FirstName"), parameters.get("LastName"), parameters.get("CompanyName"), parameters.get("AvtarUrl"), parameters.get("Email"), parameters.get("PhoneNo"), parameters.get("CompanyPhoneNo"), parameters.get("AddressOne"), parameters.get("AddressTwo"), parameters.get("Type"), Boolean.parseBoolean(parameters.get("Activate")));
                        }
                        break;
                    }


                    default:
                    {
                        var bytes = response.getBytes();
                        exchange.sendResponseHeaders(verify.code(), bytes.length);
                        exchange.getResponseBody().write(bytes);
                        exchange.close();
                    }
                }
                exchange.sendResponseHeaders(HTTP_BAD_REQUEST, 0);
                exchange.getResponseBody().write(response.getBytes());
                exchange.close();


//                if (parameters.size() == 2) {
//
//                    LocalDateTime now = LocalDateTime.now();
//                    LocalDateTime delivery = now.plusDays(3);
//                    response = """
//                       { "order":
//                           {
//                               "orderId":"%010d",
//                               "product":"%s",
//                               "amount":%s,
//                               "orderReceived":"%s",
//                               "orderDeliveryDate":"%s"
//                           }
//                       }
//                       """.formatted(getNextID(),
//                                    parameters.get("product"),
//                                    parameters.get("amount"),
//                                    now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
//                                    delivery.format(DateTimeFormatter.ISO_LOCAL_DATE))
//                            .replaceAll("\\s", "");
//                    System.out.println(response);
//
//                } else {
//                    response = "{\"result\":\"Bad Data sent\"}";
//                    responseCode = HTTP_BAD_REQUEST;
//                }


            });

            server.start();
            System.out.println("Server is listening on port 8080...");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> parseParameters(String requestBody) {

        Map<String, String> parameters = new HashMap<>();
        String[] pairs = requestBody.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                parameters.put(keyValue[0], keyValue[1]);
            }
        }
        return parameters;
    }
}
