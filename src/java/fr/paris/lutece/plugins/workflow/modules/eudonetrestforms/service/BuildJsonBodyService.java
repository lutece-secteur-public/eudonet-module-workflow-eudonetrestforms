package fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponseHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.forms.service.FormsPlugin;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.EudonetLink;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.EudonetLinkHome;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.EudonetRestData;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFileHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BuildJsonBodyService
{
    private static BuildJsonBodyService _singleton;
    private static final String PROPERTY_ENTRY_TYPE_GEOLOCATION = "directory.entry_type.geolocation";
    private static final String PROPERTY_ENTRY_TYPE_IMAGE = "directory.resource_rss.entry_type_image";

    // Constants
    private static final int CONSTANT_ENTRY_DATE_CREATION = -2;
    private static final int CONSTANT_ENTRY_DATE_MODIFICATION = -3;

    /**
     * The plugin Forms.
     */
    private final Plugin _pluginForms = PluginService.getPlugin( FormsPlugin.PLUGIN_NAME );

    /**
     * @return instance BuildJsonBodyService
     */
    public static BuildJsonBodyService getService( )
    {
        if ( _singleton == null )
        {
            _singleton = new BuildJsonBodyService( );

            return _singleton;
        }

        return _singleton;
    }


    /**
     * get record field value
     * 
     * @param codeQuestion
     * @param nIdResponse
     * @param nIdForm
     * @return record field value
     */
    public String getRecordFieldValue( String codeQuestion, int nIdResponse, int nIdForm )
    {
        String strRecordFieldValue = StringUtils.EMPTY;
        Plugin pluginForms = PluginService.getPlugin( FormsPlugin.PLUGIN_NAME );

//        if ( codeQuestion == CONSTANT_ENTRY_DATE_CREATION || codeQuestion == CONSTANT_ENTRY_DATE_MODIFICATION )
//        {
//            if ( codeQuestion == CONSTANT_ENTRY_DATE_CREATION )
//            {
//                return getDateCreation( nIdRecord );
//            }
//            else
//            {
//                return getDateModification( nIdRecord );
//            }
//        }
//        
        
        Question question = QuestionHome.findByCode(codeQuestion);
        
        List<FormQuestionResponse> questionResponse = FormQuestionResponseHome.findFormQuestionResponseByResponseQuestion(nIdResponse, question.getId());
        
        // FIXME Gerer les differents type de reponse et les iterations (regroupement et fichier)
        if ( CollectionUtils.isNotEmpty(questionResponse) ) {
        	List<Response> responses = questionResponse.get( 0 ).getEntryResponse();
        	if ( CollectionUtils.isNotEmpty(responses)) {
        		strRecordFieldValue = responses.get(0).getResponseValue();
        	}
        }
        

//        IEntry entry = EntryHome.findByPrimaryKey( nIdEntry, pluginForms );
//
//        if ( ( entry != null ) )
//        {
//            RecordFieldFilter recordFieldFilter = new RecordFieldFilter( );
//            recordFieldFilter.setIdForms( nIdFormResponse );
//            recordFieldFilter.setIdEntry( entry.getIdEntry( ) );
//            recordFieldFilter.setIdRecord( nIdRecord );
//
//            List<RecordField> listRecordFields = RecordFieldHome.getRecordFieldList( recordFieldFilter, pluginForms );
//
//            if ( entry.getEntryType( ).getIdType( ) == AppPropertiesService.getPropertyInt( PROPERTY_ENTRY_TYPE_GEOLOCATION, 16 ) )
//            {
//                if ( listRecordFields.size( ) >= 4 )
//                {
//                    return listRecordFields.get( 2 ).getValue( ) + ", " + listRecordFields.get( 3 ).getValue( );
//                }
//                else
//                {
//                    return StringUtils.EMPTY;
//                }
//            }
//
//            if ( entry.getEntryType( ).getIdType( ) == 8 )
//            {
//                RecordField recordFieldIdDemand = listRecordFields.get( 0 );
//                strRecordFieldValue = recordFieldIdDemand.getValue( );
//
//                if ( recordFieldIdDemand.getEntry( ) != null )
//                {
//                    return recordFieldIdDemand.getEntry( ).getTitle( );
//                }
//            }
//
//            if ( entry.getEntryType( ).getIdType( ) == 4 )
//            {
//                RecordField recordFieldIdDemand = listRecordFields.get( 0 );
//                strRecordFieldValue = recordFieldIdDemand.getValue( );
//
//                if ( recordFieldIdDemand.getField( ) != null )
//                {
//                    strRecordFieldValue = recordFieldIdDemand.getField( ).getTitle( );
//                }
//                try
//                {
//                    long times = Long.parseLong( strRecordFieldValue );
//                    Date date = new Date( times );
//                    DateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
//                    String strDate = sdf.format( date );
//                    if ( strDate != null )
//                        return strDate;
//
//                }
//                catch( Exception e )
//                {
//                    // ("NumberFormatException: " + nfe.getMessage());
//                }
//            }
//
//            if ( ( listRecordFields != null ) && !listRecordFields.isEmpty( ) && ( listRecordFields.get( 0 ) != null ) )
//            {
//                RecordField recordFieldIdDemand = listRecordFields.get( 0 );
//                strRecordFieldValue = recordFieldIdDemand.getValue( );
//
//                if ( recordFieldIdDemand.getField( ) != null )
//                {
//                    strRecordFieldValue = recordFieldIdDemand.getField( ).getTitle( );
//                }
//            }
//        }

        return strRecordFieldValue;
    }

    /**
     * get record field value
     * 
     * @param codeQuestion
     * @param nIdResponse
     * @param nIdForm
     * @return record field value
     */
    public File getRecordFileValue( String codeQuestion, int nIdResponse, int nIdForm )
    {
        String strRecordFieldValue = StringUtils.EMPTY;
        Plugin pluginForms = PluginService.getPlugin( FormsPlugin.PLUGIN_NAME );
        
        Question question = QuestionHome.findByCode(codeQuestion);
        
        List<FormQuestionResponse> questionResponse = FormQuestionResponseHome.findFormQuestionResponseByResponseQuestion(nIdResponse, question.getId());

        
        PhysicalFile physicalFile = null;
        File file = FileHome.findByPrimaryKey( -1 );
        if ( file != null )
        {
            physicalFile = PhysicalFileHome.findByPrimaryKey( file.getPhysicalFile( ).getIdPhysicalFile( ) );
        }


//        IEntry entry = EntryHome.findByPrimaryKey( nIdEntry, pluginDirectory );
//
//        if ( ( entry != null ) )
//        {
//            RecordFieldFilter recordFieldFilter = new RecordFieldFilter( );
//            recordFieldFilter.setIdForms( nIdDirectory );
//            recordFieldFilter.setIdEntry( entry.getIdEntry( ) );
//            recordFieldFilter.setIdRecord( nIdRecord );
//
//            List<RecordField> listRecordFields = RecordFieldHome.getRecordFieldList( recordFieldFilter, pluginDirectory );
//
//            if ( entry.getEntryType( ).getIdType( ) == 8 )
//            {
//                RecordField recordFieldIdDemand = listRecordFields.get( 0 );
//                strRecordFieldValue = recordFieldIdDemand.getValue( );
//
//                if ( recordFieldIdDemand.getFile( ) != null )
//                {
//                    return recordFieldIdDemand.getFile( );
//                }
//            }
//        }

        return null;
    }

    private Timestamp getDateCreation( int nIdRecord, int nIdDirectory )
    {
        Plugin pluginForms = PluginService.getPlugin( FormsPlugin.PLUGIN_NAME );

//        RecordFieldFilter recordFieldFilter = new RecordFieldFilter( );
//        recordFieldFilter.setIdForms( nIdDirectory );
//        recordFieldFilter.setIdRecord( nIdRecord );
//
//        List<RecordField> listRecordFields = RecordFieldHome.getRecordFieldList( recordFieldFilter, pluginDirectory );
//
//        if ( ( listRecordFields != null ) && !listRecordFields.isEmpty( ) && ( listRecordFields.get( 0 ) != null ) )
//        {
//            return listRecordFields.get( 0 ).getRecord( ).getDateCreation( );
//        }

        return null;
    }

    private String getDateCreation( int nIdRecord )
    {
        FormResponse record = FormResponseHome.findByPrimaryKey( nIdRecord );
//        Record record = Form.findByPrimaryKey( nIdRecord, pluginForms );
        if ( record != null )
        {
            long times = record.getCreation( ).getTime( );
            Date date = new Date( times );
            DateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
            String strDate = sdf.format( date );
            if ( strDate != null )
                return strDate;
        }

        return "";
    }

    private String getDateModification( int nIdRecord )
    {
    	FormResponse record = FormResponseHome.findByPrimaryKey( nIdRecord );

//        Record record = RecordHome.findByPrimaryKey( nIdRecord, pluginDirectory );
        if ( record != null )
        {
            long times = record.getUpdate( ).getTime( );
            Date date = new Date( times );
            DateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
            String strDate = sdf.format( date );
            if ( strDate != null )
                return strDate;
        }

        return "";
    }

    public String getCreateRecordJsonBody( int nIdTable, List<EudonetRestData> _entries, int nIdRessource, int nIdForm )
    {
        JSONObject jsonObjectFinal = new JSONObject( );
        JSONArray jsonArray = new JSONArray( );

        for ( EudonetRestData entry : _entries )
        {
            String strIdTable = entry.getIdTable( ).split( "-" ) [0];

            if ( strIdTable.equals( "" + nIdTable ) )
            {
                String strIdAtt = entry.getIdAttribut( ).split( "-" ) [0];
                JSONObject jsonObject = new JSONObject( );
                jsonObject.accumulate( "DescId", Integer.parseInt( strIdAtt ) );
                if ( entry.getDefaultValue( ) != null && !entry.getDefaultValue( ).isEmpty( ) )
                    jsonObject.accumulate( "Value", entry.getDefaultValue( ) );
                else
                    jsonObject.accumulate( "Value", getRecordFieldValue( entry.getOrderQuestion( ), nIdRessource, nIdForm ) );

                jsonArray.add( jsonObject );
            }
        }

        jsonObjectFinal.accumulate( "Fields", jsonArray );

        return jsonObjectFinal.toString( );
    }

    public String getCreateRecordJsonBodyLink( int nIdTable, List<EudonetRestData> _entries, int nIdRessource, int nIdForm, List<Integer> listTableLinked )
    {
        JSONObject jsonObjectFinal = new JSONObject( );
        JSONArray jsonArray = new JSONArray( );

        for ( EudonetRestData entry : _entries )
        {
            String strIdTable = entry.getIdTable( ).split( "-" ) [0];

            if ( strIdTable.equals( "" + nIdTable ) )
            {
                String strIdAtt = entry.getIdAttribut( ).split( "-" ) [0];
                JSONObject jsonObject = new JSONObject( );

                jsonObject.accumulate( "DescId", Integer.parseInt( strIdAtt ) );
                if ( entry.getDefaultValue( ) != null && !entry.getDefaultValue( ).isEmpty( ) )
                    jsonObject.accumulate( "Value", entry.getDefaultValue( ) );
                else
                    jsonObject.accumulate( "Value", getRecordFieldValue( entry.getOrderQuestion( ), nIdRessource, nIdForm ) );

                jsonArray.add( jsonObject );
            }
        }

        for ( Integer i : listTableLinked )
        {
            EudonetLink eudonetLink = EudonetLinkHome.findBy( nIdRessource, i );
            if ( eudonetLink != null )
            {
                JSONObject jsonObject = new JSONObject( );

                jsonObject.accumulate( "DescId", eudonetLink.getIdTable( ) );
                jsonObject.accumulate( "Value", "" + eudonetLink.getIdField( ) );

                jsonArray.add( jsonObject );
            }
        }

        jsonObjectFinal.accumulate( "Fields", jsonArray );

        return jsonObjectFinal.toString( );
    }

    public JSONArray getCreateAnnexeJsonBody( int nIdFile, int nIdTable, List<EudonetRestData> _entries, int nIdRessource, int nIdDirectory )
    {
        JSONArray jsonArray = new JSONArray( );

        for ( EudonetRestData entry : _entries )
        {
            String strIdTableLink = entry.getIdTableLink( ).split( "-" ) [0];
            String strIdTable = entry.getIdTable( ).split( "-" ) [0];
            if ( !strIdTableLink.isEmpty( ) && strIdTable.equals( "" + nIdTable ) )
            {
                File file = getRecordFileValue( entry.getOrderQuestion( ), nIdRessource, nIdDirectory );
                if ( file != null )
                {
                	FileHome.findByPrimaryKey( nIdRessource );
                    PhysicalFile physicalFile = PhysicalFileHome.findByPrimaryKey( file.getPhysicalFile( ).getIdPhysicalFile( ) );
                    String strFileName = file.getTitle( );
                    String strContent = "";
                    if ( physicalFile != null && physicalFile.getValue( ) != null )
                    {
                        byte [ ] bytes = physicalFile.getValue( );
                        byte [ ] encoded = Base64.encodeBase64( bytes );
                        strContent = new String( encoded );
                    }

                    JSONObject jsonObject = new JSONObject( );
                    jsonObject.accumulate( "FileId", nIdFile );
                    jsonObject.accumulate( "TabId", nIdTable );
                    jsonObject.accumulate( "FileName", strFileName );
                    jsonObject.accumulate( "Content", strContent );
                    jsonObject.accumulate( "IsUrl", false );

                    jsonArray.add( jsonObject );
                }
            }
        }

        return jsonArray;
    }

    public String getMetaInfosJsonBody( String strIdTable )
    {
        if ( strIdTable != null )
        {
            JSONObject jsonObject = new JSONObject( );
            JSONArray jsonArray = new JSONArray( );
            JSONObject jsonElement = new JSONObject( );
            JSONArray jsonElementArray = new JSONArray( );

            jsonElementArray.add( 0 );

            jsonElement.accumulate( "DescId", Integer.parseInt( strIdTable ) );
            jsonElement.accumulate( "AllFields", true );
            jsonElement.accumulate( "Fields", jsonElementArray );

            jsonArray.add( jsonElement );

            jsonObject.accumulate( "Tables", jsonArray );

            return jsonObject.toString( );
        }

        return null;
    }
}
