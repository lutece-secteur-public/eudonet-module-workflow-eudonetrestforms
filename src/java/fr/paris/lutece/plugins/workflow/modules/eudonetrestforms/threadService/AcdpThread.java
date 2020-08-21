package fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.threadService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.jersey.api.client.ClientResponse;

import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.plugins.forms.service.FormsPlugin;
import fr.paris.lutece.plugins.managewferror.business.Resource;
import fr.paris.lutece.plugins.managewferror.business.service.IProcessTaskErrorService;
import fr.paris.lutece.plugins.managewferror.business.service.ProcessTaskErrorService;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.EudonetLink;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.EudonetLinkHome;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.EudonetRestData;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.service.BuildJsonBodyService;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.service.EudonetClient;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.service.EudonetRestWsService;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.utils.EudonetRestException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AcdpThread extends Thread
{
    private static final String THREAD_NAME = "eudonetRest-export-Acdp-thread";
    private static final String ACTION = "ACDP ACTION";

    private EudonetClient _client;
    private EudonetRestWsService _eudonetRestWsService;
    private List<EudonetRestData> _listEuData;
    private int _nIdResource;
    private int _nIdFormResponse;
    private int _nIdAction;
    private EudonetRestException _eudonetException;
    private boolean _bRunning;

    IProcessTaskErrorService _erroService = ProcessTaskErrorService.getService( );

    /**
     * constructor
     * 
     * @param client
     * @param service
     * @param listEuData
     * @param idFormResponse
     * @param idResource
     */
    public AcdpThread( EudonetClient client, EudonetRestWsService service, List<EudonetRestData> listEuData, int idFormResponse, int idResource, int idAction )
    {
        _client = client;
        _eudonetRestWsService = service;
        _listEuData = listEuData;
        _nIdResource = idResource;
        _nIdFormResponse = idFormResponse;
        _nIdAction = idAction;
        setName( THREAD_NAME + _nIdResource );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void run( )
    {
        boolean bError = false;
        boolean bErrorRecord = false;
        boolean bErrorRecordLink = false;

        FormResponse formResponse = FormResponseHome.findByPrimaryKey( _nIdResource );

        Resource gfaResourceDTO = getResourceDTO( formResponse );
        fr.paris.lutece.plugins.managewferror.business.Error error = gfaResourceDTO.getError( ).get( 0 );

        try
        {
            _bRunning = true;
            _eudonetException = null;

            String strToken = token( );

            if ( strToken != null )
            {
                createRecords( strToken, bErrorRecord );
                createRecordsLink( strToken, bErrorRecordLink );

                bError = bErrorRecord && bErrorRecordLink;
            }
            else
            {
                AppLogService.error( "Erreur d'authentification sur eudonet" );
                bError = true;
                error.setError( "Erreur d'authentification sur eudonet" );
            }

        }
        catch( Exception ex )
        {
            AppLogService.error( "error calling addProjectsInEudonet method : " + ex.getMessage( ), ex );
            _bRunning = false;
            bError = true;
            error.setError( "error calling addProjectsInEudonet method : " + ex.getMessage( ) );
        }

        if ( bError )
        {
            _erroService.saveErrorTrace( gfaResourceDTO );
        }
        else
        {
            _erroService.deleteErrorTrace( formResponse.getId( ) );
        }

        _bRunning = false;
        this.interrupt( );
    }

    public EudonetRestException getEudonetException( )
    {
        return _eudonetException;
    }

    /**
     * Return the running state
     * 
     * @return the running state
     */
    public boolean isRunning( )
    {
        return _bRunning;
    }

    /**
     * @param bool
     */
    public void setRuning( boolean bool )
    {
        this._bRunning = bool;
    }

    public String token( )
    {
        try
        {
            String strAuthenticateJson = _eudonetRestWsService.getAuthenticateJsonBody( );
            ClientResponse response = _client.getTokenAuthenticate( strAuthenticateJson );

            if ( response.getStatus( ) == 200 )
            {
                String strResponse = response.getEntity( String.class );
                JSONObject jsonObject = new JSONObject( );
                jsonObject.accumulate( "object", strResponse );

                String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );

                if ( strStatus.equals( "true" ) )
                {
                    return jsonObject.getJSONObject( "object" ).getJSONObject( "ResultData" ).getString( "Token" );
                }
                else
                {
                    String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                    AppLogService.error( "Error Eudonet : " + strErrorMessage );
                }

            }
        }
        catch( Exception ex )
        {
            AppLogService.error( "Erreur to generate the token", ex );
        }

        return null;
    }

    public void createRecords( String strToken, boolean bError )
    {
        if ( strToken != null )
        {
            List<Integer> idTableList = getTableListNotLink( strToken );
            for ( Integer i : idTableList )
            {
                try
                {
                    String strJsonBody = BuildJsonBodyService.getService( ).getCreateRecordJsonBody( i, _listEuData, _nIdResource, _nIdFormResponse );
                    ClientResponse response = _client.createRecord( strToken, "" + i, strJsonBody );
                    if ( response.getStatus( ) == 200 )
                    {
                        String strResponse = response.getEntity( String.class );
                        JSONObject jsonObject = new JSONObject( );
                        jsonObject.accumulate( "object", strResponse );

                        String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );

                        if ( strStatus.equals( "true" ) )
                        {
                            String strFileId = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultData" ).getString( "FileId" );
                            if ( strFileId != null && !strFileId.isEmpty( ) )
                            {
                                Integer nFileId = Integer.parseInt( strFileId );

                                if ( isAnnexed( i ) )
                                    createAnnexes( strToken, nFileId, i, bError );

                                EudonetLink eudonetLink = new EudonetLink( );
                                eudonetLink.setIdRessource( _nIdResource );
                                eudonetLink.setIdField( "" + nFileId );
                                eudonetLink.setIdTable( "" + i );
                                eudonetLink.setIdTableLink( "" );

                                EudonetLinkHome.create( eudonetLink );
                            }
                            // String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                            AppLogService.info( "Succes Creation - FileId : " + strFileId );
                        }
                        else
                        {
                            String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                            AppLogService.error( "Error Eudonet : " + strErrorMessage );
                            bError = true;
                        }
                    }
                }
                catch( Exception ex )
                {
                    AppLogService.error( "Erreur to create table : " + i, ex );
                    bError = true;
                }
            }
        }
    }

    public void createRecordsLink( String strToken, boolean bError )
    {
        if ( strToken != null )
        {
            List<Integer> idTableList = getTableListLink( strToken );
            for ( Integer i : idTableList )
            {
                List<Integer> idTableListLinked = getTableListLinked( strToken, i );

                try
                {
                    String strJsonBody = BuildJsonBodyService.getService( ).getCreateRecordJsonBodyLink( i, _listEuData, _nIdResource, _nIdFormResponse,
                            idTableListLinked );
                    ClientResponse response = _client.createRecord( strToken, "" + i, strJsonBody );
                    if ( response.getStatus( ) == 200 )
                    {
                        String strResponse = response.getEntity( String.class );
                        JSONObject jsonObject = new JSONObject( );
                        jsonObject.accumulate( "object", strResponse );

                        String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );

                        if ( strStatus.equals( "true" ) )
                        {
                            String strFileId = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultData" ).getString( "FileId" );
                            if ( strFileId != null && !strFileId.isEmpty( ) )
                            {
                                Integer nFileId = Integer.parseInt( strFileId );

                                if ( isAnnexed( i ) )
                                    createAnnexes( strToken, nFileId, i, bError );
                            }

                            AppLogService.info( "Succes Creation - FileId : " + strFileId );
                        }
                        else
                        {
                            String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                            AppLogService.error( "Error Eudonet : " + strErrorMessage );
                            bError = true;
                        }
                    }
                }
                catch( Exception ex )
                {
                    AppLogService.error( "Erreur to create table : " + i, ex );
                    bError = true;
                }
            }

        }
    }

    public void createAnnexes( String strToken, int nIdFile, int nIdTable, boolean bError )
    {
        if ( strToken != null )
        {
            try
            {
                JSONArray jSONArray = BuildJsonBodyService.getService( ).getCreateAnnexeJsonBody( nIdFile, nIdTable, _listEuData, _nIdResource, _nIdFormResponse );
                for ( int index = 0; index < jSONArray.size( ); index++ )
                {
                    JSONObject jObject = jSONArray.getJSONObject( index );
                    String strJObject = jObject.toString( );
                    ClientResponse response = _client.addAnnexes( strToken, strJObject );
                    if ( response.getStatus( ) == 200 )
                    {
                        String strResponse = response.getEntity( String.class );
                        JSONObject jsonObject = new JSONObject( );
                        jsonObject.accumulate( "object", strResponse );

                        String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );

                        if ( strStatus.equals( "true" ) )
                        {
                            String strFileId = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultData" ).getString( "AnnexId" );

                            AppLogService.info( "Succes Add Annexe - FileId : " + strFileId );
                        }
                        else
                        {
                            String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                            AppLogService.error( "Error Eudonet : adding Annexe " + strErrorMessage );
                            bError = true;
                        }
                    }
                }
            }
            catch( Exception ex )
            {
                AppLogService.error( "Erreur to create table : " + nIdTable, ex );
                bError = true;
            }
        }
    }

    public List<Integer> getTableListDistinct( )
    {
        List<Integer> idTableList = new ArrayList<Integer>( );
        for ( EudonetRestData eudonetRestData : _listEuData )
        {
            String strIdTable = eudonetRestData.getIdTable( ).split( "-" ) [0];
            if ( !strIdTable.isEmpty( ) )
            {
                Integer nIdTable = Integer.parseInt( strIdTable );
                if ( !strIdTable.equals( "102000" ) && !idTableList.contains( nIdTable ) )
                {
                    idTableList.add( nIdTable );
                }
            }
        }

        return idTableList;
    }

    public boolean isAnnexed( Integer nIdTable )
    {
        for ( EudonetRestData eudonetRestData : _listEuData )
        {
            String strIdTableLink = eudonetRestData.getIdTableLink( ).split( "-" ) [0];
            String strIdTable = eudonetRestData.getIdTable( ).split( "-" ) [0];
            if ( !strIdTableLink.isEmpty( ) && strIdTable.equals( "" + nIdTable ) )
            {
                return true;
            }
        }

        return false;
    }

    public List<Integer> getTableListNotLink( String strToken )
    {
        List<Integer> idTableListDistinct = getTableListDistinct( );
        List<Integer> idTableList = new ArrayList<Integer>( );
        for ( Integer idTable : idTableListDistinct )
        {
            String strBody = BuildJsonBodyService.getService( ).getMetaInfosJsonBody( "" + idTable );
            if ( strBody != null )
            {
                ClientResponse response = _client.getAttributListMetaInfos( strToken, strBody );
                if ( response.getStatus( ) == 200 )
                {
                    String strResponse = response.getEntity( String.class );
                    JSONObject jsonObject = new JSONObject( );
                    jsonObject.accumulate( "object", strResponse );
                    String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );
                    if ( strStatus.equals( "true" ) )
                    {
                        JSONArray jsonArray = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultMetaData" ).getJSONArray( "Tables" ).getJSONObject( 0 )
                                .getJSONArray( "Links" );
                        if ( jsonArray.isEmpty() && idTableListDistinct.contains( idTable ) )
                        {
                            idTableList.add( idTable );
                        }
                    }
                    else
                    {
                        String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                        AppLogService.error( "Error Eudonet : " + strErrorMessage );
                    }
                }
            }
        }

        return idTableList;
    }

    public List<Integer> getTableListLinked( String strToken, Integer nIdTable )
    {
        List<Integer> idTableList = new ArrayList<Integer>( );
        String strBody = BuildJsonBodyService.getService( ).getMetaInfosJsonBody( "" + nIdTable );
        if ( strBody != null )
        {
            ClientResponse response = _client.getAttributListMetaInfos( strToken, strBody );
            if ( response.getStatus( ) == 200 )
            {
                String strResponse = response.getEntity( String.class );
                JSONObject jsonObject = new JSONObject( );
                jsonObject.accumulate( "object", strResponse );
                String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );
                if ( strStatus.equals( "true" ) )
                {
                    JSONArray jsonArray = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultMetaData" ).getJSONArray( "Tables" ).getJSONObject( 0 )
                            .getJSONArray( "Links" );
                    for ( int i = 0; i < jsonArray.size( ); i++ )
                    {
                        idTableList.add( jsonArray.getInt( i ) );
                    }
                }
                else
                {
                    String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                    AppLogService.error( "Error Eudonet : " + strErrorMessage );
                }
            }
        }

        return idTableList;
    }

    public List<Integer> getTableListLink( String strToken )
    {
        List<Integer> idTableListDistinct = getTableListDistinct( );
        List<Integer> idTableListNotLink = getTableListNotLink( strToken );
        
        idTableListDistinct.removeAll(idTableListNotLink);
        
//        for ( Integer i : idTableListNotLink )
//        {
//            if ( idTableListDistinct.contains( i ) )
//            {
//                idTableListDistinct.remove( i );
//            }
//        }

        return idTableListDistinct;
    }

    public Resource getResourceDTO( FormResponse formResponse )
    {
        Resource resource = new Resource( );
        resource.setIdResource( formResponse.getId( ) );
        resource.setAction( _nIdAction );
        resource.setDescription( ACTION );
        resource.setStatus( Resource.STATUS_KO );

        List<fr.paris.lutece.plugins.managewferror.business.Error> listError = new ArrayList<fr.paris.lutece.plugins.managewferror.business.Error>( );
        fr.paris.lutece.plugins.managewferror.business.Error error = new fr.paris.lutece.plugins.managewferror.business.Error( );
        java.sql.Timestamp date = new java.sql.Timestamp( System.currentTimeMillis( ) );
        error.setDateError( date );
        error.setAction( ACTION );
        listError.add( error );
        resource.setError( listError );

        return resource;
    }
}
