VQEmbWeb ReadMe
---------------
VQEmbWeb is a very minimal embedded web server for Java (5 or greater). I created it out of desperation, because none of the alleged "tiny" web servers seemed very tiny to me at all. Its only 3rd party dependency is Log4j.
Download the VQEmbWeb jar and if you want it the source.
To use it, just use the EmbeddedServer.createInstance() method, passing in the port to open the server on and an instance of HttpRequestHandler to handle the requests. Your HttpRequestHandler has to return a Response object.
HelloWorld looks like this:

EmbeddedServer.createInstance(
        8090, new HttpRequestHandler(){
          public Response handleRequest(Type type, String url, Map<String, String> parameters) {
            Response response = new Response();
            response.addContent("<html><body><h1>Hello World!</h1></body></html>");
            response.setOk();
            return response;
          }
        }
    );

The URL parameters are handed to you in their unencoded form in the map. The URL without the parameters included is in the url string. There are also methods for:

    * setting a redirect
    * returning a 404 or a 500
    * setting the content type
    * setting binary, rather than text content

VQEmbWeb also supports SSL (see below)

An example handler is provided - FileBasedHttpRequestHandler - that serves up text and binary content from a directory.


SSL
---
If you specify a keystore as a system property, VQEmbWeb will automatically switch to using an SSL server socket.
E.g. run Java with these VM arguments:

-Djavax.net.ssl.keyStore=/home/me/mykeystore -Djavax.net.ssl.keyStorePassword=password

To generate the keystore with a self-signed certificate you can use this command:

keytool -keystore /home/me/keystore -genkey -alias vqtodo -keyalg RSA


Licence
-------
VQEmbWeb is licensed under the GNU Lesser General Public Licence (the LGPL). See lgpl-3.0.txt for details.