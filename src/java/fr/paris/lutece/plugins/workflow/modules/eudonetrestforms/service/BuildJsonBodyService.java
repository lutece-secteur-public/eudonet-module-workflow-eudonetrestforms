package fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponseHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.EudonetLink;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.EudonetLinkHome;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.EudonetRestData;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.utils.EudonetConversion;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFileHome;
import fr.paris.lutece.portal.service.util.AppLogService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BuildJsonBodyService
{
    private static final String         FORMS_ENTRY_TYPE_GEOLOCATION = "forms.entryTypeGeolocation";
    private static final String         FORMS_ENTRY_TYPE_DATE        = "forms.entryTypeDate";

    private static final String         EUDONET_CREATION_DATE_VALUE  = "creation_date";
    private static final Pattern 		patternCodePostal = Pattern.compile("75[0-9]{3}");

    private static BuildJsonBodyService _singleton;

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
    public String getRecordFieldValue( String codeQuestion, int nIdResponse, int nIdForm, EudonetRestData entry )
    {
        String strRecordFieldValue = StringUtils.EMPTY;
        try
        {

            Question question = QuestionHome.findByCode( codeQuestion );

            List<FormQuestionResponse> questionResponse = FormQuestionResponseHome.findFormQuestionResponseByResponseQuestion( nIdResponse, question.getId( ) );

            if ( CollectionUtils.isNotEmpty( questionResponse ) )
            {
                List<Response> responses = questionResponse.get( 0 ).getEntryResponse( );
                if ( CollectionUtils.isNotEmpty( responses ) )
                {
                    strRecordFieldValue = responses.get( 0 ).getResponseValue( );

                    if ( responses.get( 0 ).getEntry( ).getEntryType( ).getBeanName( ).equalsIgnoreCase( FORMS_ENTRY_TYPE_DATE ) )
                    {
                        try
                        {
                            Date date = DateUtils.parseDate( strRecordFieldValue, "dd/MM/yyyy" );
                            DateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
                            String strDate = sdf.format( date );
                            if ( strDate != null )
                            {
                                return strDate;
                            }
                        } catch ( Exception e )
                        {
                            AppLogService.debug( e );
                        }
                    }

                    if ( responses.get( 0 ).getEntry( ).getEntryType( ).getBeanName( ).equalsIgnoreCase( FORMS_ENTRY_TYPE_GEOLOCATION ) )
                    {
                    	if ( ( entry != null ) && entry.getIdAttribut( ).contains( "Géoloc" ) )
                        {
                            // Cas d'un champ de type geolocation. On retourne la valeur du field 'X, Y'
                            Optional<Response> addressX = responses.stream( ).filter( response -> response.getField( ).getCode( ).equals( "X" ) ).findFirst( );
                            Optional<Response> addressY = responses.stream( ).filter( response -> response.getField( ).getCode( ).equals( "Y" ) ).findFirst( );
                            if ( addressX.isPresent( ) && addressY.isPresent( ) )
                            {
                                Double coordX = Double.valueOf( addressX.get( ).getResponseValue( ) );
                                Double coordY = Double.valueOf( addressY.get( ).getResponseValue( ) );

                                strRecordFieldValue = EudonetConversion.lambertToGeographic( coordX, coordY );
                            }
                        } else if ( ( entry != null ) && entry.getIdAttribut( ).contains( "CP" ) )
                        {
                            // Cas d'un champ de type geolocation. On retourne la valeur du field 'X, Y'
                            Optional<Response> additionalAddress = responses.stream( ).filter( response -> response.getField( ).getCode( ).equals( "additionalAddress" ) ).findFirst( );
                            Optional<Response> address = responses.stream( ).filter( response -> response.getField( ).getCode( ).equals( "address" ) ).findFirst( );
                            if ( additionalAddress.isPresent() && StringUtils.isNotEmpty(additionalAddress.get( ).getResponseValue( )) ) {
                            	strRecordFieldValue = additionalAddress.get( ).getResponseValue( );
                            } else if ( address.isPresent( ) )
                            {
                                String fulladress = address.get( ).getResponseValue( );
                                Matcher matcher = patternCodePostal.matcher(fulladress);
                                strRecordFieldValue = matcher.find() ? matcher.group() : "";
                            }
                        } else
                        {
                            // Cas d'un champ de type geolocation. On retourne la valeur du field 'address'
                            Optional<Response> address = responses.stream( ).filter( response -> response.getField( ).getCode( ).equals( "address" ) ).findFirst( );
                            if ( address.isPresent( ) )
                            {
                                strRecordFieldValue = address.get( ).getResponseValue( );
                            }
                        }
                    }

                }
            }
        } catch ( Exception e )
        {
            AppLogService.debug( e );
        }

        return strRecordFieldValue;
    }
    
    /**
     * get record field value
     *
     * @param codeQuestion
     * @param nIdResponse
     * @param nIdForm
     * @return List record field value
     */
    public List<File> getRecordFileValue( String codeQuestion, int nIdResponse, int nIdForm )
    {
        Question question = QuestionHome.findByCode( codeQuestion );

        List<FormQuestionResponse> questionResponse = FormQuestionResponseHome.findFormQuestionResponseByResponseQuestion( nIdResponse, question.getId( ) );

        List<File> listFile = new ArrayList<File>( );

        if ( CollectionUtils.isNotEmpty( questionResponse ) )
        {
            List<Response> responses = questionResponse.get( 0 ).getEntryResponse( );
            for ( Response response : responses )
            {
                listFile.add( FileHome.findByPrimaryKey( response.getFile( ).getIdFile( ) ) );
            }
        }

        return listFile;
    }

    public String getCreateRecordJsonBodyLink( int nIdTable, List<EudonetRestData> entries, int nIdRessource, int nIdForm, List<Integer> listTableLinked, String prefixCode )
    {
        JSONObject jsonObjectFinal = new JSONObject( );
        JSONArray jsonArray = new JSONArray( );

        for ( EudonetRestData entry : entries )
        {
            String strIdTable = entry.getIdTable( ).split( "-" )[0];

            if ( strIdTable.equals( "" + nIdTable ) )
            {
                String strIdAtt = entry.getIdAttribut( ).split( "-" )[0];
                JSONObject jsonObject = new JSONObject( );

                jsonObject.accumulate( "DescId", Integer.parseInt( strIdAtt ) );
                StringBuilder value = new StringBuilder( );
                if ( ( entry.getDefaultValue( ) != null ) && !entry.getDefaultValue( ).isEmpty( ) )
                {
                    value.append( getDefaultValue( entry.getDefaultValue( ), nIdRessource ) );
                } else
                {
                    value.append( getRecordFieldValue( entry.getOrderQuestion( ).replaceFirst( "I1_", prefixCode ), nIdRessource, nIdForm, entry ) );
                }

                if ( !StringUtils.isEmpty( entry.getPrefix( ) ) )
                {
                    value.insert( 0, entry.getPrefix( ) );
                }

                jsonObject.accumulate( "Value", value.toString( ) );
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

    private String getDefaultValue( String defaultValue, int nIdFormResponse )
    {
        if ( EUDONET_CREATION_DATE_VALUE.equals( defaultValue ) )
        {
            FormResponse response = FormResponseHome.findByPrimaryKey( nIdFormResponse );
            DateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
            String strDate = sdf.format( response.getCreation( ) );
            if ( strDate != null )
            {
                return strDate;
            }
        }

        return defaultValue;
    }

    public JSONArray getCreateAnnexeJsonBody( int nIdFile, int nIdTable, List<EudonetRestData> entries, int nIdRessource, int nIdDirectory )
    {
        JSONArray jsonArray = new JSONArray( );

        for ( EudonetRestData entry : entries )
        {
            String strIdTableLink = entry.getIdTableLink( ).split( "-" )[0];
            if ( !strIdTableLink.isEmpty( ) && strIdTableLink.equals( "" + nIdTable ) )
            {
                List<File> listfiles = getRecordFileValue( entry.getOrderQuestion( ), nIdRessource, nIdDirectory );
                for ( File file : listfiles )
                {
                    if ( file != null )
                    {
                        PhysicalFile physicalFile = PhysicalFileHome.findByPrimaryKey( file.getPhysicalFile( ).getIdPhysicalFile( ) );
                        String strFileName = StringUtils.isNotEmpty( entry.getPrefix( ) ) ? entry.getPrefix( )+file.getTitle( ) : file.getTitle( );
                        String strContent = "";
                        if ( ( physicalFile != null ) && ( physicalFile.getValue( ) != null ) )
                        {
                            byte[] bytes = physicalFile.getValue( );
                            byte[] encoded = Base64.encodeBase64( bytes );
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
    
    public String getExistingFileId( int nIdTable, List<EudonetRestData> entries, int nIdRessource, int nIdForm, String prefixCode)
    {
        JSONObject jsonObjectFinal = new JSONObject( );
        
		Optional<EudonetRestData> uniqueEudonetKey = entries.stream()
				.filter(entry -> entry.getIdTable().startsWith(String.valueOf(nIdTable)))
				.filter(entry -> entry.getUniqueField().booleanValue()).findFirst();
		
		if (uniqueEudonetKey.isPresent()) {

	        jsonObjectFinal.accumulate( "ShowMetadata", false );
	        jsonObjectFinal.accumulate( "RowsPerPage", 1 );
	        jsonObjectFinal.accumulate( "NumPage", 0 );
	        
	        JSONArray jsonColsArray = new JSONArray( );
	        jsonColsArray.add(StringUtils.split(uniqueEudonetKey.get().getIdAttribut(), "-")[0]);
	        jsonObjectFinal.accumulate( "ListCols", jsonColsArray );
	        
	        JSONObject jsonCriteriaClause = new JSONObject( );
	        jsonCriteriaClause.accumulate("Operator", 0);
	        jsonCriteriaClause.accumulate("Field", StringUtils.split(uniqueEudonetKey.get().getIdAttribut(), "-")[0]);
			jsonCriteriaClause.accumulate("Value",
					uniqueEudonetKey.get().getPrefix() + getRecordFieldValue(uniqueEudonetKey.get().getOrderQuestion().replaceFirst( "I1_", prefixCode ),
							nIdRessource, nIdForm, uniqueEudonetKey.get()));
			
	        JSONObject jsonWhereClause = new JSONObject( );
	        jsonWhereClause.accumulate("Criteria", jsonCriteriaClause);
	        
	        jsonObjectFinal.accumulate("WhereCustom", jsonWhereClause);
		}
		
        return jsonObjectFinal.toString( );
    }
}
