package com.brickintellect.webserver;

import java.util.Map;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.router.RouterNanoHTTPD;
import org.nanohttpd.router.RouterNanoHTTPD.DefaultRoutePrioritizer;
import org.nanohttpd.router.RouterNanoHTTPD.IRoutePrioritizer;
import org.nanohttpd.router.RouterNanoHTTPD.UriResource;
import org.nanohttpd.util.IHandler;

/*
This class is a clone of org.nanohttpd.router.RouterNanoHTTPD.UriRouter.
It is required because that class unnecessarily makes its addRoute() and
removeRoute() methods private.
*/
public class HttpRouter implements IHandler<IHTTPSession, Response> {

    private UriResource error404Url;

    private IRoutePrioritizer routePrioritizer = new DefaultRoutePrioritizer();

    /**
     * Search in the mappings if the given url matches some of the rules If
     * there are more than one marches returns the rule with less parameters
     * e.g. mapping 1 = /user/:id mapping 2 = /user/help if the incoming uri
     * is www.example.com/user/help - mapping 2 is returned if the incoming
     * uri is www.example.com/user/3232 - mapping 1 is returned
     * 
     * @param url
     * @return
     */
    public Response handle(IHTTPSession session) {
        String work = RouterNanoHTTPD.normalizeUri(session.getUri());
        Map<String, String> params = null;
        UriResource uriResource = error404Url;
        for (UriResource u : routePrioritizer.getPrioritizedRoutes()) {
            params = u.match(work);
            if (params != null) {
                uriResource = u;
                break;
            }
        }
        return uriResource.process(params, session);
    }

    public void addRoute(String url, int priority, Class<?> handler, Object... initParameter) {
        routePrioritizer.addRoute(url, priority, handler, initParameter);
    }

    public void removeRoute(String url) {
        routePrioritizer.removeRoute(url);
    }

    public void setNotFoundHandler(Class<?> handler) {
        error404Url = new UriResource(null, 100, handler);
    }

    public void setNotImplemented(Class<?> handler) {
        routePrioritizer.setNotImplemented(handler);
    }
}
