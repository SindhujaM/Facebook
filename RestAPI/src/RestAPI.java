import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Base64;
import org.vertx.java.platform.Verticle;

public class RestAPI extends Verticle 
{
	
	 private static void addJsonHeader(HttpServerRequest req, String json){
	        req.response().putHeader("Content-Type", "application/.json");
	        req.response().end(json);
	    }
	    private static void addCORSHeader(HttpServerRequest req){
	        req.response().putHeader("Access-Control-Allow-Origin", "*");
	      
	    }
	    public void start() {
	        final RouteMatcher matcher = new RouteMatcher();
	        final HashMap<String, Map<String, Object>> dataMap = new HashMap<>();
	        matcher.get("/data", new Handler<HttpServerRequest>() {
	            @Override
	            public void handle(final HttpServerRequest req) {
	                addJsonHeader(req,new JsonArray(dataMap.keySet().toArray()).encode());
	            }
	        });
	        matcher.get("/data/:id", new Handler<HttpServerRequest>() {
	            @Override
	            public void handle(final HttpServerRequest req) {
	                try {
	                    String id = req.params().get("id");
	                    addJsonHeader(req,new JsonObject(dataMap.get(id)).encode());
	                } catch (NullPointerException e) {
	                    req.response().setStatusCode(400)
	                            .setStatusMessage("Bad Request").end();
	                }
	            }
	        });
	        matcher.post("/data", new Handler<HttpServerRequest>() {
	            @Override
	            public void handle(final HttpServerRequest req) {
	                req.bodyHandler(new Handler<Buffer>() {
	                    @Override
	                    public void handle(Buffer buffer) {
	                        try {
	                            Map<String, Object> newResource = new JsonObject(buffer.toString()).toMap();
	                            String id = newResource.get("id").toString();
	                            if (dataMap.containsKey(id))
	                                throw new Exception();
	                            dataMap.put(id, newResource);
	                            req.response().headers().add("Location", "/data/" + id);
	                            req.response().setStatusCode(201);
	                            req.response().setStatusMessage("Created");
	                        } catch (ClassCastException | NullPointerException e) {
	                            req.response().setStatusCode(400).setStatusMessage("Bad Request");
	                        } catch (Exception e) {
	                            req.response().setStatusCode(409).setStatusMessage("Conflict");
	                        } finally {
	                            req.response().end();
	                        }
	                    }
	                });
	            }
	        });
	        matcher.put("/data/:id", new Handler<HttpServerRequest>() {
	            @Override
	            public void handle(final HttpServerRequest req) {
	                req.bodyHandler(new Handler<Buffer>() {
	                    @Override
	                    public void handle(Buffer buffer) {
	                        Map<String, Object> newResource = new JsonObject(buffer.toString()).toMap();
	                        String id = req.params().get("id");
	                        if (!dataMap.containsKey(id)) {
	                            req.response().headers().add("Location", "/data/" + id);
	                            req.response().setStatusCode(201).setStatusMessage("Created").end();
	                        }
	                        dataMap.put(id, newResource);
	                        req.response().end();
	                    }
	                });
	            }
	        });
	        matcher.delete("/data/:id", new Handler<HttpServerRequest>() {
	            @Override
	            public void handle(final HttpServerRequest req) {
	                String id = req.params().get("id");
	                if (dataMap.containsKey(id)) {
	                    dataMap.remove(id);
	                    req.response().end();
	                } else{
	                    req.response().setStatusCode(404).setStatusMessage("Not Found").end();
	                }
	               
	            }  
	            
	        });
	        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {

				@Override
				public void handle(HttpServerRequest req) {
					// TODO Auto-generated method stub
					try{
						String encodestring=req.headers().get("Authorization");
						String[] encode=encodestring.split("\\s");
						byte[] decode=(byte[]) Base64.decode(encode[1]);
						String getdecodestring=new String(decode);
						String[] decodestring=getdecodestring.split(":");
						if(!decodestring[0].equals("admin")&&!decodestring[1].equals("admin")){
								throw new Exception();
						}
						  addCORSHeader(req);
						  matcher.handle(req);
						 // return  req.response().notify();
						  
						}
						catch(Exception e){
							req.response().setStatusCode(401).setStatusMessage("Unauthorized").end();
						}
					
				}
	        	
	        }).listen(8080);
	            }     
}


