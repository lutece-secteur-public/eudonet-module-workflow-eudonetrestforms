package fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.TaskEudonetRestConfig;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.threadService.AcdpThread;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.utils.EudonetRestException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import net.sf.json.JSONObject;

public class EudonetRestWsService implements IEudonetRestWsService
{

    public static final String PROPERTY_TELESERVICE_EUDONET_PREFIX = "workflow.eudonetrestforms.teleservice.";
    public static final String PROPERTY_TELESERVICE_ATTRIBUTE_EUDONET_SUFFFIX = ".attributes";
    public static final String PROPERTY_TELESERVICE_NAME_EUDONET_SUFFFIX = ".name";
    public static final String PROPERTY_TELESERVICE_NUMBER_EUDONET_SUFFFIX = "number";

    // Members
    private TaskEudonetRestConfig _config;
    private EudonetClient _client;
    private AcdpThread _acdpThread;

    @Override
    public void init( TaskEudonetRestConfig taskEudonetConfig ) throws EudonetRestException
    {
        _config = new TaskEudonetRestConfig( );

        _config.setIdTask( taskEudonetConfig.getIdTask( ) );
        _config.setIdForms( taskEudonetConfig.getIdForms( ) );

        _config.setBaseUrl( taskEudonetConfig.getBaseUrl( ) );
        _config.setSubscriberLogin( taskEudonetConfig.getSubscriberLogin( ) );
        _config.setSubscriberPassword( taskEudonetConfig.getSubscriberPassword( ) );
        _config.setBaseName( taskEudonetConfig.getBaseName( ) );
        _config.setUserLogin( taskEudonetConfig.getUserLogin( ) );
        _config.setUserPassword( taskEudonetConfig.getUserPassword( ) );
        _config.setUserLang( taskEudonetConfig.getUserLang( ) );
        _config.setProductName( taskEudonetConfig.getProductName( ) );

        _config.setEntry( taskEudonetConfig.getEntry( ) );

        _client = new EudonetClient( _config.getBaseUrl( ) );
    }

    @Override
    public void exportDemand( int idResource, int idAction ) throws EudonetRestException
    {
        _acdpThread = new AcdpThread( _client, this, _config.getEntry( ), _config.getIdForms( ), idResource, idAction );

        _acdpThread.setRuning( true );
        _acdpThread.start( );

        while ( _acdpThread.isRunning( ) && !_acdpThread.isInterrupted( ) )
        {
            if ( ( _acdpThread != null ) && ( _acdpThread.getEudonetException( ) != null ) )
            {
                AppLogService.error( "La réponse " + idResource + " : " + " n'a pas pu être exportée dans Eudonet" + "\n" );
                AppLogService.error( "Exception", _acdpThread.getEudonetException( ) );

                EudonetRestException exception = _acdpThread.getEudonetException( );
                _acdpThread.interrupt( );
                _acdpThread = null;
                throw new EudonetRestException( exception.getIdDemand( ), exception.getMessage( ) );
            }
        }

    }

    @Override
    public void updateDemand( int idResource ) throws EudonetRestException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeDemand( int idResource ) throws EudonetRestException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<String> getTeleserviceEudonet( )
    {
        int nTeleserviceNumber = 0;

        try
        {
            String strNumberValue = AppPropertiesService.getProperty( PROPERTY_TELESERVICE_EUDONET_PREFIX + PROPERTY_TELESERVICE_NUMBER_EUDONET_SUFFFIX );
            nTeleserviceNumber = Integer.parseInt( strNumberValue );
        }
        catch( NumberFormatException e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        
		return Stream.iterate(0, n -> n + 1).limit(nTeleserviceNumber)
				.map(index -> AppPropertiesService.getProperty(
						PROPERTY_TELESERVICE_EUDONET_PREFIX + index + PROPERTY_TELESERVICE_NAME_EUDONET_SUFFFIX))
				.collect(Collectors.toSet());

//        Set<String> listSet = new HashSet<String>( );
//
//        for ( int i = 0, n = i + 1; i < nTeleserviceNumber; i++ )
//        {
//            String strNameValue = AppPropertiesService.getProperty( PROPERTY_TELESERVICE_EUDONET_PREFIX + n + PROPERTY_TELESERVICE_NAME_EUDONET_SUFFFIX );
//
//            listSet.add( strNameValue );
//        }
//
//        return listSet;
    }

    @Override
    public Set<String> getAttributEudonet( int nIdTeleservice )
    {
        String strAttributeValue = AppPropertiesService.getProperty( PROPERTY_TELESERVICE_EUDONET_PREFIX + nIdTeleservice
                + PROPERTY_TELESERVICE_ATTRIBUTE_EUDONET_SUFFFIX );
        
        return Stream.of(strAttributeValue.split( "," )).collect(Collectors.toSet());
    }

    public String getAuthenticateJsonBody( )
    {
        JSONObject jsonObject = new JSONObject( );
        jsonObject.accumulate( "SubscriberLogin", _config.getSubscriberLogin( ) );
        jsonObject.accumulate( "SubscriberPassword", _config.getSubscriberPassword( ) );
        jsonObject.accumulate( "BaseName", _config.getBaseName( ) );
        jsonObject.accumulate( "UserLogin", _config.getUserLogin( ) );
        jsonObject.accumulate( "UserPassword", _config.getUserPassword( ) );
        jsonObject.accumulate( "UserLang", _config.getUserLang( ) );
        jsonObject.accumulate( "ProductName", _config.getProductName( ) );

        return jsonObject.toString( );
    }
}
