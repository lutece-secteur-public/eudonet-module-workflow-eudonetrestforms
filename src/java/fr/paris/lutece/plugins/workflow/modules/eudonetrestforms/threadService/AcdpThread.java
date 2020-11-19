package fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.threadService;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.sun.jersey.api.client.ClientResponse;

import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
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
import fr.paris.lutece.portal.service.util.AppLogService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AcdpThread extends Thread
{
    private static final String THREAD_NAME = "eudonetRest-export-Acdp-thread";
    private static final String ACTION = "ACDP ACTION";

    private static final Integer INSTALLATIONS_TABLE = 1500; // "1500-Installations"
    private static final String NEXT_INSTALLATION_CODE = "autreInstallation";

    private EudonetClient _client;
    private EudonetRestWsService _eudonetRestWsService;
    private List<EudonetRestData> _listEuData;
    private int _nIdResource;
    private int _nIdForm;
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
     * @param idForm
     * @param idResource
     */
    public AcdpThread( EudonetClient client, EudonetRestWsService service, List<EudonetRestData> listEuData, int idForm, int idResource, int idAction )
    {
        _client = client;
        _eudonetRestWsService = service;
        _listEuData = listEuData;
        _nIdResource = idResource;
        _nIdForm = idForm;
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
                createRecordsLink( strToken, bErrorRecord, false);
                createRecordsLink( strToken, bErrorRecordLink, true );

                bError = bErrorRecord && bErrorRecordLink;

                if (!bError) {
                    EudonetLinkHome.deleteResource(_nIdResource);
                }
            }
            else
            {
                AppLogService.error( "Erreur d'authentification sur eudonet" );
                bError = true;
                error.setError( "Erreur d'authentification sur eudonet" );
//                _eudonetException = new EudonetRestException( String.valueOf( _nIdResource ) , "Erreur d'authentification sur eudonet" );
            }

        }
        catch( Exception ex )
        {
            AppLogService.error( "error calling addProjectsInEudonet method : " + ex.getMessage( ), ex );
            _bRunning = false;
            bError = true;
            error.setError( "error calling addProjectsInEudonet method : " + ex.getMessage( ) );
//            _eudonetException = new EudonetRestException( String.valueOf( _nIdResource ) , ex.getMessage( ) );
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
        interrupt( );
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
        _bRunning = bool;
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
                    AppLogService.error( "Error Authentification Eudonet : " + strErrorMessage );
                }

            }
        }
        catch( Exception ex )
        {
            AppLogService.error( "Erreur to generate the token", ex );
        }

        return null;
    }

    public void createRecordsLink( String strToken, boolean bError, boolean tableLink ) throws EudonetRestException
    {
        if ( strToken != null )
        {
            List<Integer> idTableList = tableLink ? getTableListLink( strToken ) : getTableListNotLink( strToken );
            for ( Integer i : idTableList )
            {
                List<Integer> idTableListLinked = getTableListLinked( strToken, i );

                int iteration = 1;
                boolean hasNextIteration = false;

                do {
                    String prefix = StringUtils.EMPTY;
                    if ( INSTALLATIONS_TABLE.equals(i) ) {
                        prefix = "I" + iteration + "_";
                        String nextInstallationValue = BuildJsonBodyService.getService( ).getRecordFieldValue(prefix + NEXT_INSTALLATION_CODE, _nIdResource, _nIdForm, null);
                        hasNextIteration = "YES".equals(nextInstallationValue) ? true : false;
                    }
                    try
                    {
                        ClientResponse response = null;
                        boolean createRecord = true;

                        // Véfifie l'existence d'une donnee pour la table
                        String strJsonExistingField = BuildJsonBodyService.getService().getExistingFileId(i, _listEuData, _nIdResource, _nIdForm, prefix);
                        if (StringUtils.isNoneBlank(strJsonExistingField) && !StringUtils.equals("{}", strJsonExistingField)) {
                            response = _client.searchRecordByCriteria( strToken, "" + i, strJsonExistingField );

                            if ( response.getStatus( ) == 200 )
                            {
                                String strResponse = response.getEntity( String.class );
                                JSONObject jsonObject = new JSONObject( );
                                jsonObject.accumulate( "object", strResponse );

                                String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );

                                if ( strStatus.equals( "true" ) )
                                {
                                    JSONArray rows = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultData" ).getJSONArray("Rows");
                                    if (rows.size() > 0) {
                                        String strFileId = rows.getJSONObject(0).getString( "FileId" );
                                        if ( ( strFileId != null ) && !strFileId.isEmpty( ) )
                                        {
                                            Integer nFileId = Integer.parseInt( strFileId );
                                            createRecord = false;

                                            EudonetLink eudonetLink = new EudonetLink( );
                                            eudonetLink.setIdRessource( _nIdResource );
                                            eudonetLink.setIdField( "" + nFileId );
                                            eudonetLink.setIdTable( "" + i );
                                            eudonetLink.setIdTableLink( "" );

                                            EudonetLinkHome.create( eudonetLink );

                                            AppLogService.info( String.format("[%s] Use existing FileId %s in TableId %s", _nIdResource, strFileId , i));
                                        }
                                    }
                                }
                                else
                                {
                                    String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                                    AppLogService.error( "Error Eudonet : " + strErrorMessage );
                                    bError = true;
                                    
                                    throw new EudonetRestException(String.valueOf(_nIdResource), "Error sur la recherche de donnees existantes dans Eudonet");
                                }
                            }
                        }

                        if (createRecord) {
                            String strJsonBody = BuildJsonBodyService.getService( ).getCreateRecordJsonBodyLink( i, _listEuData, _nIdResource, _nIdForm,
                                    idTableListLinked, prefix );
                            response = _client.createRecord( strToken, "" + i, strJsonBody );
                            if ( response.getStatus( ) == 200 )
                            {
                                String strResponse = response.getEntity( String.class );
                                JSONObject jsonObject = new JSONObject( );
                                jsonObject.accumulate( "object", strResponse );

                                String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );

                                if ( strStatus.equals( "true" ) )
                                {
                                    String strFileId = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultData" ).getString( "FileId" );
                                    if ( ( strFileId != null ) && !strFileId.isEmpty( ) )
                                    {
                                        Integer nFileId = Integer.parseInt( strFileId );

                                        if ( isAnnexed( i ) )
                                        {
                                            createAnnexes( strToken, nFileId, i, bError, prefix );
                                        }

                                        EudonetLink eudonetLink = new EudonetLink( );
                                        eudonetLink.setIdRessource( _nIdResource );
                                        eudonetLink.setIdField( "" + nFileId );
                                        eudonetLink.setIdTable( "" + i );
                                        eudonetLink.setIdTableLink( "" );

                                        EudonetLinkHome.create( eudonetLink );
                                    }

                                    AppLogService.info( String.format("[%s] Succes Creation FileId %s in TableId %s", _nIdResource, strFileId , i));
                                }
                                else
                                {
                                    String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                                    AppLogService.error( "Error Eudonet : " + strErrorMessage );
                                    bError = true;
                                }
                            }
                        }
                    }
                    catch( Exception ex )
                    {
                        AppLogService.error( "Erreur to create table : " + i, ex );
                        bError = true;
                    }
                    
                    if (bError) {
                        throw new EudonetRestException(String.valueOf(_nIdResource));
                    }

                    iteration++;
                } while (hasNextIteration);
            }
        }
    }

    public boolean createAnnexes( String strToken, int nIdFile, int nIdTable, boolean bError, String prefix ) throws EudonetRestException
    {
        if ( strToken != null )
        {
            try
            {
                JSONArray jSONArray = BuildJsonBodyService.getService( ).getCreateAnnexeJsonBody( nIdFile, nIdTable, _listEuData, _nIdResource, _nIdForm, prefix );
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

                            AppLogService.info( String.format("[%s] Succes Add Annexe FileId %s in TableId %s", _nIdResource, strFileId , nIdTable));
                        }
                        else
                        {
                            String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                            AppLogService.error( String.format("[%s] Error Eudonet : adding Annexe %s", _nIdResource, strErrorMessage));
                            bError = true;
                        }
                    }
                }
            }
            catch( Exception ex )
            {
                AppLogService.error( "Erreur to create annexe table : " + nIdTable, ex );
                bError = true;
            }
        }
        
        if (bError) {
            throw new EudonetRestException(String.valueOf(_nIdResource));
        }
        
        return bError;
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
            if ( !strIdTableLink.isEmpty( ) && strIdTableLink.equals( "" + nIdTable ) )
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

        return idTableListDistinct;
    }

    public Resource getResourceDTO( FormResponse formResponse )
    {
        Resource resource = new Resource( );
        resource.setIdResource( formResponse.getId( ) );
        resource.setAction( _nIdAction );
        resource.setDescription( ACTION );
        resource.setStatus( Resource.STATUS_KO );
        
        Form form = FormHome.findByPrimaryKey(_nIdForm);
        if (form != null) {
        	resource.setIdWorkflow( form.getIdWorkflow() );
        }

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
