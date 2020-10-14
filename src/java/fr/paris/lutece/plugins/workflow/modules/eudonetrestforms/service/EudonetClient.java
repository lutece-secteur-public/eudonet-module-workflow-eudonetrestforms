package fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;

import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class EudonetClient
{
    private static Client _client;
    private static WebResource _webResource;
    private static EudonetClient _singleton;
    
    /** The Constant PROPERTY_PROXY_HOST. */
    private static final String PROPERTY_PROXY_HOST = "httpAccess.proxyHost";

    /** The Constant PROPERTY_PROXY_PORT. */
    private static final String PROPERTY_PROXY_PORT = "httpAccess.proxyPort";
    
    /** The Constant PROPERTY_NO_PROXY_FOR. */
    private static final String PROPERTY_NO_PROXY_FOR = "httpAccess.noProxyFor";


    /** The Constant SEPARATOR. */
    private static final String SEPARATOR = ",";

    public EudonetClient( String baseUrl )
    {
    	ClientConfig clientConfig = new DefaultClientConfig( );
        clientConfig.getFeatures( ).put( JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE );
        _client = createClient(clientConfig, baseUrl);
        // _client.addFilter( new HTTPBasicAuthFilter( "", "" ) );
        _webResource = _client.resource( baseUrl );
    }
    
    private Client createClient(ClientConfig clientConfig, String baseUrl) {
    	return new Client(new URLConnectionClientHandler(
                new HttpURLConnectionFactory() {
                    Proxy p = null;
                    @Override
                    public HttpURLConnection getHttpURLConnection(URL url)
                            throws IOException {
                        if (p == null) {
                        	
                            String _strProxyHost = AppPropertiesService.getProperty( PROPERTY_PROXY_HOST );
                            String _strProxyPort = AppPropertiesService.getProperty( PROPERTY_PROXY_PORT );
                            String _strNoProxyFor = AppPropertiesService.getProperty( PROPERTY_NO_PROXY_FOR );

                            boolean bNoProxy = false;
                            
                            // If proxy host and port found, set the correponding elements
                            if ( StringUtils.isNotBlank( _strProxyHost ) && StringUtils.isNotBlank( _strProxyPort ) && StringUtils.isNumeric( _strProxyPort ) )
                            {
                                bNoProxy = ( StringUtils.isNotBlank( _strNoProxyFor ) && matchesList( _strNoProxyFor.split( SEPARATOR ), url.getHost() ) );

                                if ( !bNoProxy )
                                {
                                    p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(_strProxyHost, Integer.valueOf(_strProxyPort)));
                                } else {
                                	p = Proxy.NO_PROXY;
                                }
                            }
                            
                        }
                        return (HttpURLConnection) url.openConnection(p);
                    }
                }), clientConfig);
    }
    
    private boolean matchesList( String [ ] listPatterns, String strText )
    {
        if ( listPatterns == null )
        {
            return false;
        }

        return Arrays.asList(listPatterns).contains(strText);
    }

    public static EudonetClient getService( String baseUrl )
    {
        if ( _singleton == null )
        {
            _singleton = new EudonetClient( baseUrl );

            return _singleton;
        }

        return _singleton;
    }

    public EudonetClient( String baseUrl, String userName, String password )
    {
    	ClientConfig clientConfig = new DefaultClientConfig( );
        clientConfig.getFeatures( ).put( JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE );
        _client = createClient(clientConfig, baseUrl);
    	_client.addFilter( new HTTPBasicAuthFilter( userName, password ) );
        _webResource = _client.resource( baseUrl );
    }

    /**
     * return the token
     * 
     * @param strUrlApi
     * @param strAuthenticateJson
     * @return ClientResponse
     */
    public ClientResponse getTokenAuthenticate( String strAuthenticateJson )
    {
        return _webResource.path( "Authenticate" ).path( "Token" ).type( MediaType.APPLICATION_JSON ).accept( MediaType.APPLICATION_JSON )
                .post( ClientResponse.class, strAuthenticateJson );

    }

    /**
     * create a new record in the table strIdTable
     * 
     * @param strUrlApi
     * @param strAuthenticate
     * @param strIdTable
     * @param strBodyJson
     * @return ClientResponse
     */
    public ClientResponse createRecord( String strAuthenticate, String strIdTable, String strBodyJson )
    {
        return _webResource.path( "CUD" ).path( strIdTable ).header( "x-auth", strAuthenticate ).type( MediaType.APPLICATION_JSON )
                .accept( MediaType.APPLICATION_JSON ).post( ClientResponse.class, strBodyJson );
    }

    /**
     * update the record
     * 
     * @param strUrlApi
     * @param strAuthenticate
     * @param strIdTable
     * @param strBodyJson
     * @param strIdRecord
     * @return ClientResponse
     */
    public ClientResponse updateRecord( String strAuthenticate, String strIdTable, String strBodyJson, String strIdRecord )
    {
        return _webResource.path( "CUD" ).path( strIdTable ).path( strIdRecord ).header( "x-auth", strAuthenticate ).type( MediaType.APPLICATION_JSON )
                .accept( MediaType.APPLICATION_JSON ).post( ClientResponse.class, strBodyJson );
    }

    /**
     * delete the record
     * 
     * @param strUrlApi
     * @param strAuthenticate
     * @param strIdTable
     * @param strIdRecord
     * @return ClientResponse
     */
    public ClientResponse deleteRecord( String strAuthenticate, String strIdTable, String strIdRecord )
    {
        return _webResource.path( "CUD" ).path( "Delete" ).path( strIdTable ).path( strIdRecord ).header( "x-auth", strAuthenticate )
                .type( MediaType.APPLICATION_JSON ).accept( MediaType.APPLICATION_JSON ).delete( ClientResponse.class );
    }

    /**
     * search a record by id
     * 
     * @param strUrlApi
     * @param strAuthenticate
     * @param strIdTable
     * @param strIdRecord
     * @return ClientResponse
     */
    public ClientResponse searchRecordById( String strAuthenticate, String strIdTable, String strIdRecord )
    {
        return _webResource.path( "Search" ).path( strIdTable ).path( strIdRecord ).header( "x-auth", strAuthenticate ).type( MediaType.APPLICATION_JSON )
                .accept( MediaType.APPLICATION_JSON ).get( ClientResponse.class );
    }

    /**
     * search records by Crieria
     * 
     * @param strUrlApi
     * @param strAuthenticate
     * @param strIdTable
     * @param strBodyJson
     * @return ClientResponse
     */
    public ClientResponse searchRecordByCriteria( String strAuthenticate, String strIdTable, String strBodyJson )
    {
        return _webResource.path( "Search" ).path( strIdTable ).header( "x-auth", strAuthenticate ).type( MediaType.APPLICATION_JSON )
                .accept( MediaType.APPLICATION_JSON ).post( ClientResponse.class, strBodyJson );
    }

    /**
     * search records
     * 
     * @param strUrlApi
     * @param strAuthenticate
     * @param strIdTable
     * @param strBodyJson
     * @return ClientResponse
     */
    public ClientResponse searchFastRecord( String strAuthenticate, String strIdTable, String strBodyJson )
    {
        return _webResource.path( "Search/Fast" ).path( strIdTable ).header( "x-auth", strAuthenticate ).type( MediaType.APPLICATION_JSON )
                .accept( MediaType.APPLICATION_JSON ).post( ClientResponse.class, strBodyJson );
    }

    /**
     * add a Annexes
     * 
     * @param strUrlApi
     * @param strAuthenticate
     * @param strBodyJson
     * @return ClientResponse
     */
    public ClientResponse addAnnexes( String strAuthenticate, String strBodyJson )
    {
        return _webResource.path( "Annexes/Add" ).header( "x-auth", strAuthenticate ).type( MediaType.APPLICATION_JSON ).accept( MediaType.APPLICATION_JSON )
                .post( ClientResponse.class, strBodyJson );
    }

    /**
     * get a MetaInfos
     * 
     * @param strAuthenticate
     * @param strBodyJson
     * @return ClientResponse
     */
    public ClientResponse getAttributListMetaInfos( String strAuthenticate, String strBodyJson )
    {
        return _webResource.path( "MetaInfos" ).header( "x-auth", strAuthenticate ).type( MediaType.APPLICATION_JSON ).accept( MediaType.APPLICATION_JSON )
                .post( ClientResponse.class, strBodyJson );
    }

    /**
     * get a MetaInfos
     * 
     * @param strAuthenticate
     * @return ClientResponse
     */
    public ClientResponse getTableListMetaInfos( String strAuthenticate )
    {
        return _webResource.path( "MetaInfos" ).path( "ListTabs/" ).header( "x-auth", strAuthenticate ).type( MediaType.APPLICATION_JSON )
                .accept( MediaType.APPLICATION_JSON ).get( ClientResponse.class );
    }

    /**
     * @return the _client
     */
    public static Client getClient( )
    {
        return _client;
    }

    /**
     * @return the _webResource
     */
    public static WebResource getWebResource( )
    {
        return _webResource;
    }

}
